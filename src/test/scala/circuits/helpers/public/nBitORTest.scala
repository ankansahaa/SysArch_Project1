package sysarch.circuits.helpers.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.helpers.nBitOR

class nBitORTest extends AnyFlatSpec with ChiselSim {

  private def pokeVec(vec: chisel3.Vec[chisel3.Bool], bits: Seq[Boolean]): Unit = {
    for (i <- bits.indices) {
      vec(i).poke(bits(i).B)
    }
  }

  behavior of "nBitOR"

  it should "work correctly with 1-bit input" in {
    simulate(new nBitOR(1)) { dut =>
      dut.a(0).poke(false.B)
      dut.clock.step()
      dut.out.expect(false.B)

      dut.a(0).poke(true.B)
      dut.clock.step()
      dut.out.expect(true.B)
    }
  }

  it should "compute the disjunction of all input bits" in {
    simulate(new nBitOR(4)) { dut =>
      pokeVec(dut.a, Seq(false, false, false, false))
      dut.clock.step()
      dut.out.expect(false.B)

      pokeVec(dut.a, Seq(false, true, false, false))
      dut.clock.step()
      dut.out.expect(true.B)
    }
  }
}
