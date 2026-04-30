package fix

import scalafix.v1._
import scala.meta._

class RestrictedImports extends SemanticRule("RestrictedImports") {
  private val allowedImports: Set[String] = Set(
    "sysarch.chisel._",
    "sysarch.gates._",
    "sysarch.circuits.helpers._"
  )

  override def fix(implicit doc: SemanticDocument): Patch = {
    if (isExemptSource || isInTestSources) return Patch.empty

    doc.tree.collect {
      case importer @ Importer(ref, importees) =>
        val importText = s"${ref.syntax}.${renderImportees(importees)}"
        if (allowedImports.contains(importText)) Patch.empty
        else lintDisallowedImport(importer, importText)
    }.asPatch
  }

  private def renderImportees(importees: List[Importee]): String =
    importees match {
      case List(Importee.Wildcard()) => "_"
      case other => other.map(_.syntax).mkString("{", ", ", "}")
    }

  private def lintDisallowedImport(tree: Tree, importText: String): Patch = {
    Patch.lint(
      Diagnostic(
        "RestrictedImports",
        s"Disallowed import '$importText'. Allowed imports are: ${allowedImports.mkString(", ")}",
        tree.pos,
        "",
        scalafix.lint.LintSeverity.Error
      )
    )
  }

  private def isExemptSource(implicit doc: SemanticDocument): Boolean =
    isInGatesPackage || isInChiselWrapper

  private def isInGatesPackage(implicit doc: SemanticDocument): Boolean = {
    doc.tree.collect {
      case pkg: Pkg => pkg.ref.syntax
    }.headOption.exists(pkg => pkg == "sysarch.gates" || pkg.startsWith("sysarch.gates."))
  }

  private def isInChiselWrapper(implicit doc: SemanticDocument): Boolean = {
    val isSysarchPackage = doc.tree.collect {
      case pkg: Pkg => pkg.ref.syntax == "sysarch"
    }.headOption.getOrElse(false)

    val hasChiselObject = doc.tree.collect {
      case Defn.Object(_, Term.Name("chisel"), _) => true
    }.headOption.getOrElse(false)

    val isInChiselPath = doc.input match {
      case Input.VirtualFile(path, _) =>
        path.contains("/src/main/scala/chisel/") || path.startsWith("src/main/scala/chisel/")
      case _ => false
    }

    (isSysarchPackage && hasChiselObject) || isInChiselPath
  }

  private def isInTestSources(implicit doc: SemanticDocument): Boolean = {
    doc.input match {
      case Input.VirtualFile(path, _) =>
        path.contains("/src/test/") || path.startsWith("src/test/")
      case _ => false
    }
  }
}
