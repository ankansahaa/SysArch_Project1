package sysarch.circuits.floatingpoint.public.bonus

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.floatingpoint.bonus.denormal.DenormalFloatingPointAddition
import sysarch.circuits.util.FloatingPoint

class DenormalFloatingPointAdditionTest extends AnyFlatSpec with ChiselSim {

  private def pokeFloat(vec: chisel3.Vec[chisel3.Bool], value: Double): Unit = {
    val bits = FloatingPoint.doubleToIEEE754(value, 8, 23)
    for (i <- 0 until 32) {
      vec(i).poke(bits(i).B)
    }
  }

  private def expectFloat(vec: chisel3.Vec[chisel3.Bool], expected: Double): Unit = {
    val expectedBits = FloatingPoint.doubleToIEEE754(expected, 8, 23)
    for (i <- 0 until 32) {
      vec(i).expect(
        if (expectedBits(i)) true.B else false.B,
        s"Bit $i did not match expected value for input $expected.\n" +
          s"Expected ${(for (i <- 0 until 32) yield if (expectedBits(i)) "1" else "0").mkString} \n" +
          s"but got  ${(for (i <- 0 until 32) yield if (vec(i).peek().litToBoolean) "1" else "0").mkString}.\n"
      )
    }
  }

  val testInputs = Seq(
    (1.0, -1.0, 0.0),
    (1.5, 0.0, 1.5),
    (1e-45, 3e-45, 4e-45),
    (1.0, 1e-45, 1.0),
    (1e-45, -1e-45, 0.0),
    (1.1754942e-38, 1.1754942e-38, 2.3509884e-38)
  )

  for ((a, b, expected) <- testInputs) {
    it should s"add $a and $b correctly" in {
      simulate(new DenormalFloatingPointAddition) { dut =>
        pokeFloat(dut.a, a)
        pokeFloat(dut.b, b)

        dut.clock.step()

        expectFloat(dut.out, expected)
      }
    }
  }
}
