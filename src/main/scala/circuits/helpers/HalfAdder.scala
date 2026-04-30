package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class HalfAdder extends Module {
  val a    = IO(Input(Bool()))
  val b    = IO(Input(Bool()))
  val sum  = IO(Output(Bool()))
  val cout = IO(Output(Bool()))

  // 1. Summon the gates
  val xorGate = Module(new XORGate)
  val andGate = Module(new ANDGate)

  // 2. Wire up the XOR gate for the Sum
  xorGate.a := a
  xorGate.b := b
  sum       := xorGate.out

  // 3. Wire up the AND gate for the Carry-out (cout)
  andGate.a := a
  andGate.b := b
  cout      := andGate.out
}
