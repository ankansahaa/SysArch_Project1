package sysarch.circuits.integer

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class OnesComplementToTwosComplement(width: Int) extends Module {
  val onesComplement = IO(Input(Vec(width, Bool())))
  val twosComplement = IO(Output(Vec(width, Bool())))

  val nAddSub = Module(new nBitAdderSubtractor(width))
  val b_in    = Wire(Vec(width, Bool()))
  val msb     = onesComplement(width - 1)
  for (i <- 0 until width) {
    if (i == 0) {
      b_in(i) := msb // Add 1 only if MSB is 1 (negative)
    } else {
      b_in(i) := false.B // All other bits are 0
    }
  }

  nAddSub.a          := onesComplement
  nAddSub.b          := b_in
  nAddSub.enable_sub := false.B

  for (i <- 0 until width) {
    twosComplement(i) := nAddSub.sum(i)
  }

}
