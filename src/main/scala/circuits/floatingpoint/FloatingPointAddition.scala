package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

// You do not need to worry about handling special cases like NaN, infinity, or denormalized numbers for this implementation. Focus on the core addition logic for normalized numbers.

class FloatingPointAddition extends Module {
  val a   = IO(Input(Vec(32, Bool())))
  val b   = IO(Input(Vec(32, Bool())))
  val out = IO(Output(Vec(32, Bool())))

  // Helper function: Create a vector of all zeros
  def zeroVec(width: Int): Vec[Bool] = {
    val v = Wire(Vec(width, Bool()))
    v := Vec.fill(width)(false.B)
    v
  }

  // Helper function: Create a vector with only the first bit set to one
  def oneVec(width: Int): Vec[Bool] = {
    val v = Wire(Vec(width, Bool()))
    v    := Vec.fill(width)(false.B)
    v(0) := true.B
    v
  }

  // Helper function: Multiplexer for vectors
  def muxVec(width: Int, x: Vec[Bool], y: Vec[Bool], sel: Bool): Vec[Bool] = {
    val m = Module(new Mux(width))
    m.a   := x
    m.b   := y
    m.sel := sel
    m.out
  }

  // Helper function: Multiplexer for single bits
  def muxBit(x: Bool, y: Bool, sel: Bool): Bool = {
    val vx = Wire(Vec(1, Bool()))
    val vy = Wire(Vec(1, Bool()))
    val m  = Module(new Mux(1))
    vx(0) := x
    vy(0) := y
    m.a   := vx
    m.b   := vy
    m.sel := sel
    m.out(0)
  }

  // Helper function: Right shift by a dynamic amount (supports 0-255 shifts)
  def rightShiftBy(value: Vec[Bool], amount: Vec[Bool], width: Int): Vec[Bool] = {
    var current = Wire(Vec(width, Bool()))
    current := value
    val distances = Seq(1, 2, 4, 8, 16, 32, 64, 128)
    for (k <- 0 until 8) {
      val shifted  = Wire(Vec(width, Bool()))
      val distance = distances(k)

      for (i <- 0 until width) {
        if (i + distance < width) {
          shifted(i) := current(i + distance)
        } else {
          shifted(i) := false.B
        }
      }

      current = muxVec(width, current, shifted, amount(k))
    }

    current
  }

  // Helper function: Left shift by one position
  def leftShiftOne(value: Vec[Bool], width: Int): Vec[Bool] = {
    val shifted = Wire(Vec(width, Bool()))
    shifted(0) := false.B
    for (i <- 1 until width) {
      shifted(i) := value(i - 1)
    }
    shifted
  }

  // Extract exponent and mantissa from inputs
  val expA = Wire(Vec(8, Bool()))
  val expB = Wire(Vec(8, Bool()))
  expA := a.slice(23, 31)
  expB := b.slice(23, 31)

  // Mantissa with implicit leading 1 bit (25 bits total)
  val mantA = Wire(Vec(25, Bool()))
  val mantB = Wire(Vec(25, Bool()))
  mantA := Vec.fill(25)(false.B)
  mantB := Vec.fill(25)(false.B)

  for (i <- 0 until 23) {
    mantA(i) := a(i)
    mantB(i) := b(i)
  }
  mantA(23) := true.B // Add implicit leading 1
  mantB(23) := true.B

  // Magnitude comparison to determine which operand is larger
  val magCompare = Module(new nBitComparator(31))
  magCompare.a := a.slice(0, 31)
  magCompare.b := b.slice(0, 31)

  val bIsBigger = Module(new NOTGate)
  bIsBigger.a := magCompare.gt

  val equalMag = magCompare.eq

  val notEqualMag = Module(new NOTGate)
  notEqualMag.a := equalMag

  val chooseBAsLarge = Module(new ANDGate)
  chooseBAsLarge.a := bIsBigger.out
  chooseBAsLarge.b := notEqualMag.out

  // Select larger and smaller exponents and mantissas
  val largeExp  = muxVec(8, expA, expB, chooseBAsLarge.out)
  val smallExp  = muxVec(8, expB, expA, chooseBAsLarge.out)
  val largeMant = muxVec(25, mantA, mantB, chooseBAsLarge.out)
  val smallMant = muxVec(25, mantB, mantA, chooseBAsLarge.out)
  val largeSign = muxBit(a(31), b(31), chooseBAsLarge.out)
  val smallSign = muxBit(b(31), a(31), chooseBAsLarge.out)

  // Compute exponent difference and shift smaller mantissa
  val expDiffSub = Module(new nBitAdderSubtractor(8))
  expDiffSub.a          := largeExp
  expDiffSub.b          := smallExp
  expDiffSub.enable_sub := true.B

  val shiftedSmallMant = rightShiftBy(smallMant, expDiffSub.sum, 25)

  // Check if signs are the same
  val signDiff = Module(new XORGate)
  signDiff.a := largeSign
  signDiff.b := smallSign

  val sameSignNot = Module(new NOTGate)
  sameSignNot.a := signDiff.out

  // Path 1: Addition (same signs)
  val addMant = Module(new nBitAdderSubtractor(25))
  addMant.a          := largeMant
  addMant.b          := shiftedSmallMant
  addMant.enable_sub := false.B

  // Adjust exponent if addition caused overflow
  val addExpB = Wire(Vec(8, Bool()))
  addExpB    := Vec.fill(8)(false.B)
  addExpB(0) := addMant.sum(24)

  val addExp = Module(new nBitAdderSubtractor(8))
  addExp.a          := largeExp
  addExp.b          := addExpB
  addExp.enable_sub := false.B

  // Handle carry from addition by right-shifting mantissa
  val addMantNoCarry = Wire(Vec(23, Bool()))
  val addMantCarry   = Wire(Vec(23, Bool()))

  for (i <- 0 until 23) {
    addMantNoCarry(i) := addMant.sum(i)
    addMantCarry(i)   := addMant.sum(i + 1)
  }

  val finalAddMant = muxVec(23, addMantNoCarry, addMantCarry, addMant.sum(24))
  val finalAddExp  = addExp.sum

  // Path 2: Subtraction (different signs) with normalization
  var normMant = Wire(Vec(25, Bool()))
  var normExp  = Wire(Vec(8, Bool()))

  val subMant = Module(new nBitAdderSubtractor(25))
  subMant.a          := largeMant
  subMant.b          := shiftedSmallMant
  subMant.enable_sub := true.B

  normMant := subMant.sum
  normExp  := largeExp

  // Normalize: shift left until hidden bit is set
  for (_ <- 0 until 24) {
    val hiddenBitMissing = Module(new NOTGate)
    hiddenBitMissing.a := normMant(23)

    val shifted = leftShiftOne(normMant, 25)

    val decValue = Wire(Vec(8, Bool()))
    decValue    := Vec.fill(8)(false.B)
    decValue(0) := hiddenBitMissing.out

    val decExp = Module(new nBitAdderSubtractor(8))
    decExp.a          := normExp
    decExp.b          := decValue
    decExp.enable_sub := hiddenBitMissing.out

    normMant = muxVec(25, normMant, shifted, hiddenBitMissing.out)
    normExp = decExp.sum
  }

  val finalSubMant = Wire(Vec(23, Bool()))
  for (i <- 0 until 23) {
    finalSubMant(i) := normMant(i)
  }

  // Select final mantissa and exponent based on whether addition or subtraction
  val finalMant = muxVec(23, finalSubMant, finalAddMant, sameSignNot.out)
  val finalExp  = muxVec(8, normExp, finalAddExp, sameSignNot.out)
  val finalSign = largeSign

  // Assemble output: mantissa (0-22), exponent (23-30), sign (31)
  for (i <- 0 until 23) {
    out(i) := finalMant(i)
  }

  for (i <- 0 until 8) {
    out(i + 23) := finalExp(i)
  }

  out(31) := finalSign
}
