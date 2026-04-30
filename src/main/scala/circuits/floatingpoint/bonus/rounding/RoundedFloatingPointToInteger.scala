package sysarch.circuits.floatingpoint.bonus.rounding

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class RoundedFloatingPointToInteger extends Module {
  val floatInput = IO(Input(Vec(32, Bool())))
  val intOutput  = IO(Output(Vec(32, Bool())))

  ???
}
