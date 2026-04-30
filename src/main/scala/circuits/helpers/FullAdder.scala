package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class FullAdder extends Module {
  val a    = IO(Input(Bool()))
  val b    = IO(Input(Bool()))
  val cin  = IO(Input(Bool()))
  val sum  = IO(Output(Bool()))
  val cout = IO(Output(Bool()))

val ha1 = Module (new HalfAdder)
 val ha2 = Module (new HalfAdder)

ha1.io.a := io.a
ha1.io.b := io.b 

ha2.io.a := io.cin
ha2.io.b := ha1.sum

io.sum := ha2.sum
io.cout := ha1.io.cout | ha2.io.cout




}
