package sysarch.circuits.floatingpoint.public

import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import chisel3._
import sysarch.circuits.util._
import sysarch.circuits.floatingpoint._

class FloatingPointAdditionTest extends AnyFlatSpec with ChiselSim {

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
    (1.0, 1.0, 2.0),
    (1.5, 0.5, 2.0),
    (2.5, -1.5, 1.0),
    (-2.0, -2.0, -4.0),
    (8.0, 0.5, 8.5),
    (4096.0, 1.0, 4097.0),
    (17277776.0, 1.0, 17277776.0),
    (-17277776.0, -1.0, -17277776.0),
    (16777218.0, 1.0, 16777219.0)
  )

  for ((a, b, expected) <- testInputs) {
    it should s"add $a and $b correctly" in {
      simulate(new FloatingPointAddition) { dut =>
        pokeFloat(dut.a, a)
        pokeFloat(dut.b, b)

        dut.clock.step()

        expectFloat(dut.out, expected)
      }
    }
  }
}
