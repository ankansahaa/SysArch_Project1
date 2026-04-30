package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

// Output: 00 for equal, 01 for floatInput1 > floatInput2, 10 for floatInput1 < floatInput2, 11 for unordered (NaN cases)

class FloatingPointComparison extends Module {
  val a                = IO(Input(Vec(32, Bool())))
  val b                = IO(Input(Vec(32, Bool())))
  val comparisonResult = IO(Output(Vec(2, Bool())))

  ???
}
