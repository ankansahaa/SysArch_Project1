package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitOR(n: Int) extends Module {
  val a   = IO(Input(Vec(n, Bool())))
  val out = IO(Output(Bool()))

  val ors = Seq.fill(n)(Module(new ORGate))

  for (i <- 0 until n) {
    ors(i).a := a(i)

    if (i == 0) {
      ors(i).b := false.B
    } else {
      ors(i).b := ors(i - 1).out
    }
  }
  out := ors(n - 1).out

}
