package sysarch.gates

import chisel3._

class ANDGate extends Module {
  val a   = IO(Input(Bool()))
  val b   = IO(Input(Bool()))
  val out = IO(Output(Bool()))

  out := a & b
}
