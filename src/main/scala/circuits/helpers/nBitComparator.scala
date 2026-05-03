package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitComparator(width: Int) extends Module {
  val a  = IO(Input(Vec(width, Bool())))
  val b  = IO(Input(Vec(width, Bool())))
  val gt = IO(Output(Bool()))
  val eq = IO(Output(Bool()))

  val eqChain = Wire(Vec(width, Bool()))

  for (i <- 0 until width) {
    val xorGate = Module(new XORGate)
    xorGate.a  := a(i)
    xorGate.b  := b(i)
    eqChain(i) := xorGate.out
  }

  val nbitor = Module(new nBitOR(width))
  nbitor.a := eqChain
  val notGate = Module(new NOTGate)
  notGate.a := nbitor.out
  eq        := notGate.out

  val nbitsub = Module(new nBitAdderSubtractor(width))

  nbitsub.a          := b
  nbitsub.b          := a
  nbitsub.enable_sub := true.B

  gt := nbitsub.cout
}
