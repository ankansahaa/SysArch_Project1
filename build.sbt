import _root_.scalafix.sbt.{BuildInfo => ScalafixBuildInfo}

scalaVersion := "2.13.17"
organization := "SysArch"
name := "Circuits"

val chiselVersion = "7.7.0"
val scalaVersionMajor = "2.13"

// Chisel and related dependencies
libraryDependencies += "org.chipsalliance" %% "chisel" % chiselVersion

// Testing dependencies
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

// Scala format and fix options
scalafmtOnCompile := true
Compile / scalafixOnCompile := true
scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xcheckinit",
      "-Ymacro-annotations",
      "-Yrangepos"
    )

addCompilerPlugin(
  "org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full
)

// Enable scalafix
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixCaching := false

// Scalafix custom rules
libraryDependencies += "org.chipsalliance" %% "chisel" % chiselVersion % ScalafixConfig

libraryDependencies +=
  ("ch.epfl.scala" %% "scalafix-core" % ScalafixBuildInfo.scalafixVersion)
    .cross(CrossVersion.for3Use2_13) % ScalafixConfig

libraryDependencies ++= {
  if (scalaBinaryVersion.value == "3")
    Seq("org.scala-lang" %% "scala3-library" % scalaVersion.value % ScalafixConfig)
  else
    Nil
}

lazy val listClasses = taskKey[List[String]]("List all classes")

listClasses := {
  val targetDirectory = (Compile / classDirectory).value
  val classFiles = Files
    .walk(targetDirectory.toPath)
    .iterator()
    .asScala
    .filter(path => path.toString.endsWith(".class"))
    .toList

  classFiles.map { path =>
    targetDirectory.toPath
      .relativize(path)
      .iterator()
      .asScala
      .map(_.toString())
      .mkString(".")
      .stripSuffix(".class")
  }
}


import java.nio.file.{Files, Paths, StandardCopyOption}
import java.text.SimpleDateFormat
import java.util.Date
import sbt.IO

import java.nio.file._
import scala.collection.JavaConverters._

lazy val check = taskKey[Unit]("check")

check := {
  (Compile / compile).value
  (Compile / scalafix).toTask("").value

  val definedClasses = listClasses.value
  val neededClasses = Set(
    "sysarch.circuits.helpers.Mux",
    "sysarch.circuits.helpers.HalfAdder",
    "sysarch.circuits.helpers.FullAdder",
    "sysarch.circuits.helpers.nBitAND",
    "sysarch.circuits.helpers.nBitOR",
    "sysarch.circuits.helpers.nBitNOT",
    "sysarch.circuits.helpers.nBitAdderSubtractor",
    "sysarch.circuits.helpers.nBitComparator",
    "sysarch.circuits.integer.SignedMagnitudeToOnesComplement",
    "sysarch.circuits.integer.SignedMagnitudeToTwosComplement",
    "sysarch.circuits.integer.OnesComplementToTwosComplement",
    "sysarch.circuits.integer.TwosComplementToOnesComplement",
    "sysarch.circuits.integer.OnesComplementToSignedMagnitude",
    "sysarch.circuits.integer.TwosComplementToSignedMagnitude",
    "sysarch.circuits.floatingpoint.FloatingPointComparison",
    "sysarch.circuits.floatingpoint.IntegerToFloatingPoint",
    "sysarch.circuits.floatingpoint.FloatingPointToInteger",
    "sysarch.circuits.floatingpoint.FloatingPointAddition",
    "sysarch.circuits.floatingpoint.bonus.nBit.nBitIntegerToFloatingPoint",
    "sysarch.circuits.floatingpoint.bonus.nBit.nBitFloatingPointToInteger",
    "sysarch.circuits.floatingpoint.bonus.nBit.nBitFloatingPointAddition",
    "sysarch.circuits.floatingpoint.bonus.denormal.DenormalFloatingPointAddition",
    "sysarch.circuits.floatingpoint.bonus.rounding.RoundedIntegerToFloatingPoint",
    "sysarch.circuits.floatingpoint.bonus.rounding.RoundedFloatingPointToInteger",
    "sysarch.circuits.floatingpoint.bonus.rounding.RoundedFloatingPointAddition"
  )
  val allClassesDefined = neededClasses.subsetOf(definedClasses.toSet) match {
    case true => println("All classes defined")
    case false =>
      throw new Exception(
        "Some classes are not defined:\n" + (neededClasses.toSet -- definedClasses.toSet)
          .mkString("\n")
      )
  }
}


lazy val createSubmission = taskKey[Unit]("Create a submission zip file")

createSubmission := {
  val baseDirectoryValue = baseDirectory.value
  val srcDirectory = baseDirectoryValue / "src" / "main" / "scala" / "circuits"
  val targetDirectory = baseDirectoryValue / "submissions"
  val contributionsFile = baseDirectoryValue / "CONTRIBUTIONS.md"
  val timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
  val zipFileName = s"submission_$timestamp.zip"

  // Create the submissions directory
  IO.createDirectory(targetDirectory)

  // Zip the main folder
  val zipFile = targetDirectory / zipFileName
  val allFiles = (srcDirectory ** "*").get
  val mappings = allFiles.map(file =>
    (file, baseDirectoryValue.toPath.relativize(file.toPath).toString)
  ) :+ (contributionsFile, baseDirectoryValue.toPath.relativize(contributionsFile.toPath).toString)
  IO.zip(mappings, zipFile, None)

  println(s"Submission created: ${zipFile.getPath}")
}


lazy val checkAndSubmission =
  taskKey[Unit]("Create a submission zip file after checking the project")

checkAndSubmission := {
  createSubmission.value
}

checkAndSubmission := checkAndSubmission.dependsOn(check).value
