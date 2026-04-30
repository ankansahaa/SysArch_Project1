package sysarch.circuits.floatingpoint.bonus.nBit

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitIntegerToFloatingPoint(exponentWidth: Int, mantissaWidth: Int, integerWidth: Int)
    extends Module {
  val intInput    = IO(Input(Vec(integerWidth, Bool())))
  val floatOutput = IO(Output(Vec(exponentWidth + mantissaWidth + 1, Bool())))

  ???
}
