package sysarch.circuits.integer

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class OnesComplementToSignedMagnitude(width: Int) extends Module {
  val onesComplement  = IO(Input(Vec(width, Bool())))
  val signedMagnitude = IO(Output(Vec(width, Bool())))

  val msb = onesComplement(width - 1)

  for (i <- 0 until (width - 1)) {
    val myXOR = Module(new XORGate)

    myXOR.a            := onesComplement(i)
    myXOR.b            := msb
    signedMagnitude(i) := myXOR.out
  }

  signedMagnitude(width - 1) := msb
}
