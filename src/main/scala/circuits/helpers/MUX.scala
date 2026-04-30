package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class Mux(width: Int) extends Module {
  val a   = IO(Input(Vec(width, Bool())))
  val b   = IO(Input(Vec(width, Bool())))
  val sel = IO(Input(Bool()))
  val out = IO(Output(Vec(width, Bool())))

  // We loop through every bit from 0 up to 'width'
  for (i <- 0 until width) {
    // 1. Create the gates we need for this specific bit
    val andA   = Module(new ANDGate)
    val andB   = Module(new ANDGate)
    val orGate = Module(new ORGate)
    val invSel = Module(new NOTGate)

    // 2. Invert the selector for the 'a' side
    invSel.a := sel

    // 3. Logic for 'a': Pass 'a' if sel is NOT true
    andA.a := a(i)
    andA.b := invSel.out

    // 4. Logic for 'b': Pass 'b' if sel IS true
    andB.a := b(i)
    andB.b := sel

    // 5. Combine them: if either andA or andB is true, that's our bit
    orGate.a := andA.out
    orGate.b := andB.out

    // 6. Assign to the output vector
    out(i) := orGate.out
  }
}
