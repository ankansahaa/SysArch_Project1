package sysarch.circuits.integer

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class OnesComplementToTwosComplement(width: Int) extends Module {
  val onesComplement = IO(Input(Vec(width, Bool())))
  val twosComplement = IO(Output(Vec(width, Bool())))

  ???
}
