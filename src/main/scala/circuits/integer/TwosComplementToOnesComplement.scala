package sysarch.circuits.integer

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class TwosComplementToOnesComplement(width: Int) extends Module {
  val twosComplement = IO(Input(Vec(width, Bool())))
  val onesComplement = IO(Output(Vec(width, Bool())))

  val sign = twosComplement(width - 1)

  val subVal = Wire(Vec(width, Bool()))
  subVal    := Vec.fill(width)(false.B)
  subVal(0) := sign
  val subOne = Module(new nBitAdderSubtractor(width))

  subOne.a          := twosComplement
  subOne.b          := subVal
  subOne.enable_sub := sign

  onesComplement := subOne.sum

}
