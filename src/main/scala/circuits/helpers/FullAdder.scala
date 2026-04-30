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

// We use a var to act as the "wire" that ripples the carry from bit to bit
  // In subtraction mode, the first cin is 1. In addition, it's 0.
  var carryChain = enable_sub

  for (i <- 0 until width) {
    // 1. Instantiate the two Half Adders for this bit
    val ha1 = Module(new HalfAdder)
    val ha2 = Module(new HalfAdder)
    val orGate = Module(new ORGate)
    val xorGate = Module(new XORGate) // For the B input subtraction logic

    // 2. Logic to handle Subtraction (A + ~B + 1)
    // If enable_sub is 1, b(i) is flipped. If 0, b(i) stays same.
    xorGate.a := b(i)
    xorGate.b := enable_sub
    val b_actual = xorGate.out

    // 3. Connect Half Adder 1 (adds A and modified B)
    ha1.a := a(i)
    ha1.b := b_actual

    // 4. Connect Half Adder 2 (adds HA1 sum and the carry from the previous bit)
    ha2.a := carryChain
    ha2.b := ha1.sum

    // 5. Connect the bit sum to the output Vec
    sum(i) := ha2.sum

    // 6. Calculate the carry-out for this bit using an OR gate
    orGate.a := ha1.cout
    orGate.b := ha2.cout
    
    // 7. Ripple the carry to the next iteration
    carryChain = orGate.out
  }

  // Final carry output from the last bit
  cout := carryChain



}
