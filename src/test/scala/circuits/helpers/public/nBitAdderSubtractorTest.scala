package sysarch.circuits.helpers.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.helpers.nBitAdderSubtractor

class nBitAdderSubtractorTest extends AnyFlatSpec with ChiselSim {

  private def bitsOf(value: Int, width: Int): Seq[Boolean] =
    (0 until width).map(i => ((value >> i) & 1) == 1)

  private def pokeVec(vec: chisel3.Vec[chisel3.Bool], bits: Seq[Boolean]): Unit = {
    for (i <- bits.indices) {
      vec(i).poke(bits(i).B)
    }
  }

  private def expectVec(vec: chisel3.Vec[chisel3.Bool], bits: Seq[Boolean]): Unit = {
    for (i <- bits.indices) {
      vec(i).expect(bits(i).B)
    }
  }

  behavior of "nBitAdderSubtractor"

  val testInputs = Seq(
    (4, 3, 5, false, 8, false),
    (4, 0, 0, false, 0, false),
    (4, 15, 0, false, 15, false),
    (4, 15, 1, false, 0, true),
    (4, 7, 3, true, 4, false),
    (4, 3, 7, true, 12, true),
    (4, 15, 15, true, 0, false)
  )

  for ((width, aValue, bValue, enableSub, expectedSum, expectedCout) <- testInputs) {
    it should s"compute sum=${expectedSum} and cout=${expectedCout} for a=$aValue, b=$bValue, enable_sub=$enableSub with width=$width" in {
      simulate(new nBitAdderSubtractor(width)) { dut =>
        pokeVec(dut.a, bitsOf(aValue, width))
        pokeVec(dut.b, bitsOf(bValue, width))
        dut.enable_sub.poke(enableSub.B)
        dut.clock.step()
        expectVec(dut.sum, bitsOf(expectedSum, width))
        dut.cout.expect(expectedCout.B)
      }
    }
  }
}
