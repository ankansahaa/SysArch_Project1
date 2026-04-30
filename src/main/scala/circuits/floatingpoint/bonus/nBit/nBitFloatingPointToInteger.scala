package sysarch.circuits.floatingpoint.bonus.nBit

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitFloatingPointToInteger(exponentWidth: Int, mantissaWidth: Int, integerWidth: Int)
    extends Module {
  val floatInput = IO(Input(Vec(exponentWidth + mantissaWidth + 1, Bool())))
  val intOutput  = IO(Output(Vec(integerWidth, Bool())))

  ???
}
