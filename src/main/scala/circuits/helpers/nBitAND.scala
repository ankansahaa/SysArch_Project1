package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitAND(n: Int) extends Module {
  val a   = IO(Input(Vec(n, Bool())))
  val out = IO(Output(Bool()))

  val chain = Wire(Vec(n, Bool()))
  chain(0) := a(0)

  for (i <- 1 until n) {
    val andGate = Module(new ANDGate)
    andGate.a := chain(i - 1)
    andGate.b := a(i)
    chain(i)  := andGate.out
  }
  out := chain(n - 1)
}
