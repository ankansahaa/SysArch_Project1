package fix

import scalafix.v1._
import scala.meta._

class NoChiselBoolOperators extends SemanticRule("NoChiselBoolOperators") {
  override def fix(implicit doc: SemanticDocument): Patch = {
    if (isInGatesPackage || isInTestSources) return Patch.empty

    doc.tree.collect {
      case tree @ Term.ApplyUnary(Name(opName), arg)
          if isBoolType(arg) && !isAllowedOperation(opName) =>
        lintDisallowedOperation(tree, opName)

      case tree: Term.ApplyInfix
          if isBoolType(tree.lhs) && !isAllowedOperation(tree.op.value) =>
        lintDisallowedOperation(tree, tree.op.value)

      case tree: Term.Apply =>
        tree.fun match {
          case Term.Select(qual, Name(opName))
              if isBoolType(qual) && isDisallowedMethodOperation(opName) =>
            lintDisallowedOperation(tree, opName)
          case _ =>
            Patch.empty
        }
    }.asPatch
  }

  private val allowedOperations: Set[String] =
    Set("##", ":=", "<>", ":#=", ":<=", ":>=", ":<>=", ":+", "+:")

  private val disallowedNamedOperations: Set[String] = Set(
    "andR",
    "orR",
    "xorR",
    "implies",
    "abs",
    "min",
    "max",
    "head",
    "tail",
    "take",
    "pad",
    "rotateLeft",
    "rotateRight",
    "bitSet",
    "asBool",
    "asBools",
    "asUInt",
    "asSInt",
    "asClock",
    "asReset",
    "asAsyncReset",
    "asDisable",
    "asTypeOf",
    "zext"
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
        "NoChiselBoolOperators",
        s"Disallowed Bool operation '$opName'. Allowed operations are: ${allowedOperations.mkString(", ")}. " +
        s"Use the provided gates in the 'gates' package for combinational logic instead of Chisel's built-in operators.",
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

  private def isBoolType(term: Term)(implicit doc: SemanticDocument): Boolean = {
    val info = term.symbol.info
    info.exists { symbolInfo =>
      val sigStr = symbolInfo.signature.toString
      sigStr.contains("Bool") || sigStr.contains("chisel3.Bool")
    }
  }
}
