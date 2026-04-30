package sysarch.circuits.helpers.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.helpers._

class HalfAdderTest extends AnyFlatSpec with ChiselSim {

  behavior of "HalfAdder"

  val testCases = Seq(
    (false, false, false, false),
    (false, true, true, false),
    (true, false, true, false),
    (true, true, false, true)
  )

  for ((a, b, expectedSum, expectedCarry) <- testCases) {
    it should s"compute sum=${expectedSum} and carry=${expectedCarry} for a=$a and b=$b" in {
      simulate(new HalfAdder) { dut =>
        dut.a.poke(a.B)
        dut.b.poke(b.B)
        dut.clock.step()
        dut.sum.expect(expectedSum.B)
        dut.cout.expect(expectedCarry.B)
      }
    }
  }
}
