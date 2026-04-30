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

  // We use a var to act as the "wire" that ripples the carry from bit to bit
  // In subtraction mode, the first cin is 1. In addition, it's 0.

  val ha1    = Module(new HalfAdder)
  val ha2    = Module(new HalfAdder)
  val orGate = Module(new ORGate)

  // 2. Connect First Half Adder
  ha1.a := a
  ha1.b := b

  // 3. Connect Second Half Adder
  // It takes the partial sum from HA1 and the incoming Carry-in
  ha2.a := ha1.sum
  ha2.b := cin

  // 4. Output the Final Sum
  sum := ha2.sum

  // 5. Calculate Final Carry-out
  // It's 1 if HA1 generated a carry OR HA2 generated a carry
  orGate.a := ha1.cout
  orGate.b := ha2.cout
  cout     := orGate.out
}
