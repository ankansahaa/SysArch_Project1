package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class FloatingPointToInteger extends Module {
  val floatInput = IO(Input(Vec(32, Bool())))
  val intOutput  = IO(Output(Vec(32, Bool())))

  // Helper function: Create a constant vector from an integer value
  def constVec(width: Int, value: Int): Vec[Bool] = {
    val v = Wire(Vec(width, Bool()))
    for (i <- 0 until width) {
      if (((value >> i) & 1) == 1) {
        v(i) := true.B
      } else {
        v(i) := false.B
      }
    }
    v
  }

  // Helper function: Check if a vector equals a constant value
  def equalConst(x: Vec[Bool], width: Int, value: Int): Bool = {
    val sameBits = Wire(Vec(width, Bool()))
    for (i <- 0 until width) {
      val xorGate = Module(new XORGate)
      val notGate = Module(new NOTGate)
      xorGate.a := x(i)
      if (((value >> i) & 1) == 1) {
        xorGate.b := true.B
      } else {
        xorGate.b := false.B
      }
      notGate.a   := xorGate.out
      sameBits(i) := notGate.out
    }
    val allSame = Module(new nBitAND(width))
    allSame.a := sameBits
    allSame.out
  }

  // Extract components from IEEE 754 single-precision format
  val sign     = floatInput(31)
  val exponent = Wire(Vec(8, Bool()))
  exponent := floatInput.slice(23, 31)

  val mantissa = Wire(Vec(23, Bool()))
  mantissa := floatInput.slice(0, 23)

  // Detect overflow: exponent > 157 (max value for 32-bit int is 2^31-1)
  val maxExp     = constVec(8, 157)
  val expCompare = Module(new nBitComparator(8))
  expCompare.a := exponent
  expCompare.b := maxExp
  val overflow = expCompare.gt

  // Magnitude calculation: Convert exponent and mantissa to integer representation
  val exponentCases = Seq.tabulate(31)(i => equalConst(exponent, 8, i + 127))
  val magnitude     = Wire(Vec(31, Bool()))

  for (bit <- 0 until 31) {
    val terms = Wire(Vec(31, Bool()))
    terms := Vec.fill(31)(false.B)

    for (e <- 0 until 31) {
      if (bit == e) {
        // Implicit leading 1 from mantissa
        terms(e) := exponentCases(e)
      } else if (bit < e && e - bit <= 23) {
        // Contribution from explicit mantissa bits
        val andGate = Module(new ANDGate)
        andGate.a := exponentCases(e)
        andGate.b := mantissa(23 - e + bit)
        terms(e)  := andGate.out
      }
    }

    val termOr = Module(new nBitOR(31))
    termOr.a := terms

    val finalBit = Module(new ORGate)
    finalBit.a     := termOr.out
    finalBit.b     := overflow
    magnitude(bit) := finalBit.out
  }

  // Assemble output: apply sign to magnitude
  val magnitudeNonZero = Module(new nBitOR(31))
  magnitudeNonZero.a := magnitude

  val signAndNonZero = Module(new ANDGate)
  signAndNonZero.a := sign
  signAndNonZero.b := magnitudeNonZero.out

  for (i <- 0 until 31) {
    intOutput(i) := magnitude(i)
  }

  intOutput(31) := signAndNonZero.out
}
