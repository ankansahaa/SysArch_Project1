package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

// Output: 00 for equal, 01 for floatInput1 > floatInput2, 10 for floatInput1 < floatInput2, 11 for unordered (NaN cases)

class FloatingPointComparison extends Module {
  val a                = IO(Input(Vec(32, Bool())))
  val b                = IO(Input(Vec(32, Bool())))
  val comparisonResult = IO(Output(Vec(2, Bool())))

  val aExpoAllOne = Module(new nBitAND(8))
  aExpoAllOne.a := a.slice(23, 31) 

  val aManistaNonZero = Module(new nBitOR(23))
  aManistaNonZero.a := a.slice(0, 23)

  val bExpoAllOne = Module(new nBitAND(8))
  bExpoAllOne.a := b.slice(23, 31)

  val bManistaNonZero = Module(new nBitOR(23))
  bManistaNonZero.a := b.slice(0, 23)


  val aIsNaN = Module(new ANDGate)
  aIsNaN.a := aExpoAllOne.out
  aIsNaN.b := aManistaNonZero.out
  val bIsNaN = Module(new ANDGate)
  bIsNaN.a := bExpoAllOne.out
  bIsNaN.b := bManistaNonZero.out

  val anyNaN = Module(new ORGate)
  anyNaN.a := aIsNaN.out
  anyNaN.b := bIsNaN.out

  

}
