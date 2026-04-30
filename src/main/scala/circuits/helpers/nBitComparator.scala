package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitComparator(width: Int) extends Module {
  val a  = IO(Input(Vec(width, Bool())))
  val b  = IO(Input(Vec(width, Bool())))
  val gt = IO(Output(Bool()))
  val eq = IO(Output(Bool()))

  ???
}
