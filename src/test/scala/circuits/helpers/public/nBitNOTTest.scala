package sysarch.circuits.helpers.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.helpers.nBitNOT

class nBitNOTTest extends AnyFlatSpec with ChiselSim {

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

  behavior of "nBitNOT"

  it should "work correctly with 1-bit input" in {
    simulate(new nBitNOT(1)) { dut =>
      dut.a(0).poke(false.B)
      dut.clock.step()
      dut.out(0).expect(true.B)

      dut.a(0).poke(true.B)
      dut.clock.step()
      dut.out(0).expect(false.B)
    }
  }

  it should "invert each input bit independently" in {
    simulate(new nBitNOT(4)) { dut =>
      pokeVec(dut.a, Seq(true, false, true, false))
      dut.clock.step()
      expectVec(dut.out, Seq(false, true, false, true))
    }
  }
}
