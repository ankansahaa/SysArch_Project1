package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

// Output: 00 for equal, 01 for floatInput1 > floatInput2, 10 for floatInput1 < floatInput2, 11 for unordered (NaN cases)

class FloatingPointComparison extends Module {
  val a                = IO(Input(Vec(32, Bool())))
  val b                = IO(Input(Vec(32, Bool())))
  val comparisonResult = IO(Output(Vec(2, Bool())))

  // NaN Detection: Check if exponent is all 1s AND mantissa is non-zero
  val aExpoAllOne = Module(new nBitAND(8))
  aExpoAllOne.a := a.slice(23, 31)

  val aMantissaNonZero = Module(new nBitOR(23))
  aMantissaNonZero.a := a.slice(0, 23)

  val bExpoAllOne = Module(new nBitAND(8))
  bExpoAllOne.a := b.slice(23, 31)

  val bMantissaNonZero = Module(new nBitOR(23))
  bMantissaNonZero.a := b.slice(0, 23)

  val aIsNaN = Module(new ANDGate)
  aIsNaN.a := aExpoAllOne.out
  aIsNaN.b := aMantissaNonZero.out

  val bIsNaN = Module(new ANDGate)
  bIsNaN.a := bExpoAllOne.out
  bIsNaN.b := bMantissaNonZero.out

  val anyNaN = Module(new ORGate)
  anyNaN.a := aIsNaN.out
  anyNaN.b := bIsNaN.out

  // Equality Check: Bitwise comparison of all 32 bits
  val diffBit = Wire(Vec(32, Bool()))

  for (i <- 0 until 32) {
    val xorGate = Module(new XORGate)
    xorGate.a  := a(i)
    xorGate.b  := b(i)
    diffBit(i) := xorGate.out
  }

  val anyDiff = Module(new nBitOR(32))
  anyDiff.a := diffBit
  val bitWiseNotGate = Module(new NOTGate)
  bitWiseNotGate.a := anyDiff.out

  val bitWiseEqual = bitWiseNotGate.out

  // Zero Detection: Check if all value bits (0-30) are zero
  val aValueBitsNonZero = Module(new nBitOR(31))
  aValueBitsNonZero.a := a.slice(0, 31)

  val bValueBitsNonZero = Module(new nBitOR(31))
  bValueBitsNonZero.a := b.slice(0, 31)

  val aIsZeroNot = Module(new NOTGate)
  aIsZeroNot.a := aValueBitsNonZero.out

  val bIsZeroNot = Module(new NOTGate)
  bIsZeroNot.a := bValueBitsNonZero.out

  val bothZero = Module(new ANDGate)
  bothZero.a := aIsZeroNot.out
  bothZero.b := bIsZeroNot.out

  val equalOrZero = Module(new ORGate)
  equalOrZero.a := bitWiseEqual
  equalOrZero.b := bothZero.out

  // Sign Extraction and Comparison Logic
  val signA = a(31)
  val signB = b(31)

  val notSignA = Module(new NOTGate)
  notSignA.a := signA
  val notSignB = Module(new NOTGate)
  notSignB.a := signB

  // Case 1: a is positive and b is negative
  val aPosBNeg = Module(new ANDGate)
  aPosBNeg.a := notSignA.out
  aPosBNeg.b := signB

  // Case 2: a is negative and b is positive
  val aNegBPos = Module(new ANDGate)
  aNegBPos.a := signA
  aNegBPos.b := notSignB.out

  // Check if signs are the same
  val signDiff = Module(new XORGate)
  signDiff.a := signA
  signDiff.b := signB

  val sameSignNot = Module(new NOTGate)
  sameSignNot.a := signDiff.out
  val sameSign = sameSignNot.out

  // When same sign, compare magnitudes
  val bothPositive = Module(new ANDGate)
  bothPositive.a := notSignA.out
  bothPositive.b := notSignB.out

  val bothNegative = Module(new ANDGate)
  bothNegative.a := signA
  bothNegative.b := signB

  // Magnitude Comparison
  val magCompareAB = Module(new nBitComparator(31))
  magCompareAB.a := a.slice(0, 31)
  magCompareAB.b := b.slice(0, 31)

  val magCompareBA = Module(new nBitComparator(31))
  magCompareBA.a := b.slice(0, 31)
  magCompareBA.b := a.slice(0, 31)

  // Greater than logic
  val posGreater = Module(new ANDGate)
  posGreater.a := bothPositive.out
  posGreater.b := magCompareAB.gt

  val negGreater = Module(new ANDGate)
  negGreater.a := bothNegative.out
  negGreater.b := magCompareBA.gt

  val greaterSameSign = Module(new ORGate)
  greaterSameSign.a := posGreater.out
  greaterSameSign.b := negGreater.out

  val greaterRaw = Module(new ORGate)
  greaterRaw.a := aPosBNeg.out
  greaterRaw.b := greaterSameSign.out

  // Less than logic
  val posSmaller = Module(new ANDGate)
  posSmaller.a := bothPositive.out
  posSmaller.b := magCompareBA.gt

  val negSmaller = Module(new ANDGate)
  negSmaller.a := bothNegative.out
  negSmaller.b := magCompareAB.gt

  val smallerSameSign = Module(new ORGate)
  smallerSameSign.a := posSmaller.out
  smallerSameSign.b := negSmaller.out

  val smallerRaw = Module(new ORGate)
  smallerRaw.a := aNegBPos.out
  smallerRaw.b := smallerSameSign.out

  // Final Result Assembly
  val notEqual = Module(new NOTGate)
  notEqual.a := equalOrZero.out

  val greaterAndNotEqual = Module(new ANDGate)
  greaterAndNotEqual.a := greaterRaw.out
  greaterAndNotEqual.b := notEqual.out

  val smallerAndNotEqual = Module(new ANDGate)
  smallerAndNotEqual.a := smallerRaw.out
  smallerAndNotEqual.b := notEqual.out

  // Include NaN in results
  val resultGreater = Module(new ORGate)
  resultGreater.a := greaterAndNotEqual.out
  resultGreater.b := anyNaN.out

  val resultSmaller = Module(new ORGate)
  resultSmaller.a := smallerAndNotEqual.out
  resultSmaller.b := anyNaN.out

  comparisonResult(0) := resultGreater.out
  comparisonResult(1) := resultSmaller.out

}
