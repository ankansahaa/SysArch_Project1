package sysarch.circuits.helpers.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.helpers.FullAdder

class FullAdderTest extends AnyFlatSpec with ChiselSim {

  behavior of "FullAdder"

  val testCases = Seq(
    (false, false, false, false, false),
    (false, false, true, true, false),
    (false, true, false, true, false),
    (false, true, true, false, true),
    (true, false, false, true, false),
    (true, false, true, false, true),
    (true, true, false, false, true),
    (true, true, true, true, true)
  )

  for ((a, b, cin, expectedSum, expectedCarry) <- testCases) {
    it should s"compute sum=${expectedSum} and carry=${expectedCarry} for a=$a, b=$b, cin=$cin" in {
      simulate(new FullAdder) { dut =>
        dut.a.poke(a.B)
        dut.b.poke(b.B)
        dut.cin.poke(cin.B)
        dut.clock.step()
        dut.sum.expect(expectedSum.B)
        dut.cout.expect(expectedCarry.B)
      }
    }
  }
}
