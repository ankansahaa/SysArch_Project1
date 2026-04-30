package sysarch.circuits.helpers.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.helpers.nBitComparator

class nBitComparatorTest extends AnyFlatSpec with ChiselSim {

  private def bitsOf(value: Int, width: Int): Seq[Boolean] =
    (0 until width).map(i => ((value >> i) & 1) == 1)

  private def pokeVec(vec: chisel3.Vec[chisel3.Bool], bits: Seq[Boolean]): Unit = {
    for (i <- bits.indices) {
      vec(i).poke(bits(i).B)
    }
  }

  behavior of "nBitComparator"

  val testCases = Seq(
    (2, 10, false, false),
    (10, 2, true, false),
    (5, 5, false, true),
    (0, 0, false, true),
    (9, 8, true, false),
    (8, 9, false, false)
  )

  for ((aValue, bValue, expectedGt, expectedEq) <- testCases) {
    it should s"report gt=$expectedGt and eq=$expectedEq for a=$aValue and b=$bValue" in {
      simulate(new nBitComparator(4)) { dut =>
        pokeVec(dut.a, bitsOf(aValue, 4))
        pokeVec(dut.b, bitsOf(bValue, 4))
        dut.clock.step()
        dut.gt.expect(expectedGt.B)
        dut.eq.expect(expectedEq.B)
      }
    }
  }
}
