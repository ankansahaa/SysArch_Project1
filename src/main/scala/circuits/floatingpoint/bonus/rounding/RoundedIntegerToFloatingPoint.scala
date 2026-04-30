package sysarch.circuits.floatingpoint.bonus.rounding

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class RoundedIntegerToFloatingPoint extends Module {
  val intInput    = IO(Input(Vec(32, Bool())))
  val floatOutput = IO(Output(Vec(32, Bool())))

  ???
}
