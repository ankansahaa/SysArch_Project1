package sysarch.circuits.floatingpoint.bonus.nBit

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitFloatingPointAddition(exponentWidth: Int, mantissaWidth: Int) extends Module {
  val a   = IO(Input(Vec(exponentWidth + mantissaWidth + 1, Bool())))
  val b   = IO(Input(Vec(exponentWidth + mantissaWidth + 1, Bool())))
  val out = IO(Output(Vec(exponentWidth + mantissaWidth + 1, Bool())))

  ???
}
