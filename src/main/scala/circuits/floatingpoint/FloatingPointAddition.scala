package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

// You do not need to worry about handling special cases like NaN, infinity, or denormalized numbers for this implementation. Focus on the core addition logic for normalized numbers.

class FloatingPointAddition extends Module {
  val a   = IO(Input(Vec(32, Bool())))
  val b   = IO(Input(Vec(32, Bool())))
  val out = IO(Output(Vec(32, Bool())))

  ???
}
