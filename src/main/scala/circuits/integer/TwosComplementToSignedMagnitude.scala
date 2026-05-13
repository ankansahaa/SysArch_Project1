package sysarch.circuits.integer

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class TwosComplementToSignedMagnitude(width: Int) extends Module {
  val twosComplement  = IO(Input(Vec(width, Bool())))
  val signedMagnitude = IO(Output(Vec(width, Bool())))

  val twosToOnes      = Module(new TwosComplementToOnesComplement(width))
  val onesToSignedMag = Module(new OnesComplementToSignedMagnitude(width))

  twosToOnes.twosComplement      := twosComplement
  onesToSignedMag.onesComplement := twosToOnes.onesComplement

  signedMagnitude := onesToSignedMag.signedMagnitude
}
