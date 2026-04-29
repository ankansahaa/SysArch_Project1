package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class Mux(width: Int) extends Module {
  val a   = IO(Input(Vec(width, Bool())))
  val b   = IO(Input(Vec(width, Bool())))
  val sel = IO(Input(Bool()))
  val out = IO(Output(Vec(width, Bool())))

  ???
}
