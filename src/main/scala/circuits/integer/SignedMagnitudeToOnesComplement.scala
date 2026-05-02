package sysarch.circuits.integer

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class SignedMagnitudeToOnesComplement(width: Int) extends Module {
  val signedMagnitude = IO(Input(Vec(width, Bool())))
  val onesComplement  = IO(Output(Vec(width, Bool())))

  val con1  = Wire(Vec(width, Bool()))
  val MyMux = Module(new Mux(width))
  val nNot  = Module(new nBitNOT(width))
  val msb   = signedMagnitude(width - 1)

  nNot.a := signedMagnitude

  for (i <- 0 until width) {
    if (i == width - 1) {
      // msb of rsult vector = msb of given
      con1(i) := msb
    } else {
      // store the inverted result bit in con vectore
      con1(i) := nNot.out(i)
    }
  }

  MyMux.a   := signedMagnitude
  MyMux.b   := con1
  MyMux.sel := msb

  onesComplement := MyMux.out
  /*
  val msb = signedMagnitude(width - 1)

  for (i <- 0 until (width - 1)) {
    val myXOR = Module(new XORGate)

    myXOR.a           := signedMagnitude(i)
    myXOR.b           := msb
    onesComplement(i) := myXOR.out
  }

  onesComplement(width - 1) := msb
   */
}
