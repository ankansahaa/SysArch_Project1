package sysarch.gates

import chisel3._

class NOTGate extends Module {
  val a   = IO(Input(Bool()))
  val out = IO(Output(Bool()))

  out := !a
}
