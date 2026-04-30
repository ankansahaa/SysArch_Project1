package fix

import scalafix.v1._
import scala.meta._

class NoChiselVecOperators extends SemanticRule("NoChiselVecOperators") {
  override def fix(implicit doc: SemanticDocument): Patch = {
    if (isInGatesPackage || isInTestSources) return Patch.empty

    doc.tree.collect {
      case tree @ Term.ApplyUnary(Name(opName), arg)
          if isVecType(arg) && !isAllowedOperation(opName) =>
        lintDisallowedOperation(tree, opName)

      case tree: Term.ApplyInfix
          if isVecType(tree.lhs) && !isAllowedOperation(tree.op.value) =>
        lintDisallowedOperation(tree, tree.op.value)

      case tree: Term.Apply =>
        tree.fun match {
          case Term.Select(qual, Name(opName))
              if isVecType(qual) && isDisallowedMethodOperation(opName) =>
            lintDisallowedOperation(tree, opName)
          case qual: Term
              if isVecType(qual) && !isAllowedOperation("apply") =>
            lintDisallowedOperation(tree, "apply")
          case _ =>
            Patch.empty
        }
    }.asPatch
  }

  private val allowedOperations: Set[String] =
    Set(":=", "<>", ":#=", ":<=", ":>=", ":<>=", "apply", ":+", "+:")

  private val disallowedNamedOperations: Set[String] = Set(
    "contains",
    "count",
    "exists",
    "forall",
    "indexWhere",
    "lastIndexWhere",
    "onlyIndexWhere",
    "reduceTree",
    "asUInt",
    "asTypeOf",
    "readOnly",
    "getElements",
    "getWidth",
    "widthOption",
    "suggestName",
    "typeName",
    "as",
    "unsafe",
    "exclude",
    "excludeAs",
    "excludeEach",
    "excludeProbes",
    "squeeze",
    "squeezeAll",
    "squeezeAllAs",
    "squeezeEach",
    "waive",
    "waiveAll",
    "waiveAllAs",
    "waiveAs",
    "waiveEach"
  )

  private def isAllowedOperation(opName: String): Boolean =
    allowedOperations.contains(opName)

  private def isDisallowedMethodOperation(opName: String): Boolean =
    !isAllowedOperation(opName) &&
      (isSymbolicOperation(opName) || disallowedNamedOperations.contains(opName))

  private def isSymbolicOperation(opName: String): Boolean =
    opName.exists(ch => !ch.isLetterOrDigit && ch != '_')

  private def lintDisallowedOperation(tree: Tree, opName: String): Patch = {
    Patch.lint(
      Diagnostic(
        "NoChiselVecOperators",
        s"Disallowed Vec operation '$opName'. Allowed operations are: ${allowedOperations.mkString(", ")}. " +
          "Most named IndexedSeq-style methods are allowed, but specific Vec hardware/Data/connectable methods are restricted.",
        tree.pos,
        "",
        scalafix.lint.LintSeverity.Error
      )
    )
  }

  private def isInGatesPackage(implicit doc: SemanticDocument): Boolean = {
    doc.tree.collect {
      case pkg: Pkg => pkg.ref.syntax
    }.headOption.exists(pkg => pkg == "sysarch.gates" || pkg.startsWith("sysarch.gates."))
  }

  private def isInTestSources(implicit doc: SemanticDocument): Boolean = {
    doc.input match {
      case Input.VirtualFile(path, _) =>
        path.contains("/src/test/") || path.startsWith("src/test/")
      case _ => false
    }
  }

  private def isVecType(term: Term)(implicit doc: SemanticDocument): Boolean = {
    val info = term.symbol.info
    info.exists { symbolInfo =>
      val sigStr = symbolInfo.signature.toString
      sigStr.contains("Vec") || sigStr.contains("chisel3.Vec")
    }
  }
}