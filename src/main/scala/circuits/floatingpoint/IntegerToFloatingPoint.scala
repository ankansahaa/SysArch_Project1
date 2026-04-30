package sysarch.circuits.floatingpoint

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class IntegerToFloatingPoint extends Module {
  val intInput    = IO(Input(Vec(32, Bool())))
  val floatOutput = IO(Output(Vec(32, Bool())))

  ???
}
