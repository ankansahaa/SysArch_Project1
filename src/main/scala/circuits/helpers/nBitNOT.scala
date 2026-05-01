package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitNOT(n: Int) extends Module {
  val a   = IO(Input(Vec(n, Bool())))
  val out = IO(Output(Vec(n, Bool())))

  val nots = Seq.fill(n)(Module(new NOTGate))

  for (i <- 0 until n) {
    nots(i).a := a(i)
    out(i)    := nots(i).out

  }

}
