package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitAdderSubtractor(width: Int) extends Module {
  val a          = IO(Input(Vec(width, Bool())))
  val b          = IO(Input(Vec(width, Bool())))
  val enable_sub = IO(Input(Bool()))
  val sum        = IO(Output(Vec(width, Bool())))
  val cout       = IO(Output(Bool()))

  ???
}
