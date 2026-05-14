package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class IntegerToFloatingPoint extends Module {
  val intInput    = IO(Input(Vec(32, Bool())))
  val floatOutput = IO(Output(Vec(32, Bool())))

  val zero = Vec.fill(32)(false.B) // Default to 0.0

  val ans = Wire(Vec(32, Bool()))

  // The sign bit is the same as the input's sign bit
  ans(31) := intInput(31)

  // We need to find the position of the most significant bit (MSB) that is set to 1 in the input
  val pos = Wire(Vec(31, Vec(8, Bool())))

  // Making 8 bit representation of 1
  val one = Wire(Vec(8, Bool()))
  one       := (true.B +: Vec.fill(7)(false.B))
  pos(0)    := Vec.fill(8)(true.B)
  pos(0)(7) := false.B

  // Creating 8 bit wire for each position of the input, which represent the position
  // pos(0) = 0, pos(1) = 1, pos(2) = 2, ..., pos(30) = 30
  for (i <- 1 until 31) {
    val adder = Module(new nBitAdderSubtractor(8))
    adder.a          := pos(i - 1)
    adder.b          := one
    adder.enable_sub := false.B
    pos(i)           := adder.sum
  }

  val chain = Wire(Vec(32, Vec(8, Bool())))
  chain(0) := pos(0)

  for (i <- 0 until 31) {
    val mux = Module(new Mux(8))
    mux.a        := chain(i)
    mux.b        := pos(i)
    mux.sel      := intInput(i)
    chain(i + 1) := mux.out
  }

  val exponent = Wire(Vec(8, Bool()))
  exponent := chain(31)
  for (i <- 0 until 8) ans(23 + i) := exponent(i)

  val wire1 = Wire(Vec(54, Bool()))
  for (i <- 0 until 31) {
    wire1(i) := intInput(i)
  }
  for (i <- 31 until 54) {
    wire1(i) := false.B // Pad the rest with zeros!
  }

  val pos1   = Wire(Vec(31, Vec(23, Bool())))
  val chain1 = Wire(Vec(32, Vec(23, Bool())))
  chain1(0) := Vec.fill(23)(false.B)
  for (i <- 0 until 31) {
    for (b <- 0 until 23) {
      // We want the 23 bits immediately BELOW the leading 1 (which is at 'i')
      val sourceIndex = i - 1 - b

      if (sourceIndex >= 0) {
        // Wire the data bit to the mantissa
        pos1(i)(22 - b) := wire1(sourceIndex)
      } else {
        // We ran out of input bits, pad the bottom of the mantissa with 0
        pos1(i)(22 - b) := false.B
      }
    }
  }

  for (i <- 0 until 31) {
    val mux = Module(new Mux(23))
    mux.a         := chain1(i)
    mux.b         := pos1(i)
    mux.sel       := intInput(i)
    chain1(i + 1) := mux.out
  }

  for (i <- 0 until 23) ans(i) := chain1(31)(i)

  val nbitorgate = Module(new nBitOR(32))
  nbitorgate.a := intInput
  val m = Module(new Mux(32))
  m.a         := zero
  m.b         := ans
  m.sel       := nbitorgate.out
  floatOutput := m.out

}
