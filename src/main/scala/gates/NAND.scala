package sysarch.gates

import chisel3._

class NANDGate extends Module {
  val a   = IO(Input(Bool()))
  val b   = IO(Input(Bool()))
  val out = IO(Output(Bool()))

  out := ~(a & b)
}
