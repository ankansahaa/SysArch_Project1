package sysarch.circuits.floatingpoint.public.bonus.rounding

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.floatingpoint.bonus.rounding.RoundedIntegerToFloatingPoint
import sysarch.circuits.util.SignedMagnitude
import sysarch.circuits.util.FloatingPoint

class RoundedIntegerToFloatingPointTest extends AnyFlatSpec with ChiselSim {

  private def pokeSignedMagnitude(vec: chisel3.Vec[chisel3.Bool], value: BigInt): Unit = {
    val bits = SignedMagnitude.bigIntToSignedMagnitude(value, 32)
    for (i <- 0 until 32) {
      vec(i).poke(bits(i).B)
    }
  }

  private def expectDouble(vec: chisel3.Vec[chisel3.Bool], expected: Double): Unit = {
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
    (1, 1.0),
    (16777218, 16777218.0),
    (128, 128.0),
    // Note: 17277777 cannot be exactly represented in IEEE 754 single precision, we expect round towards zero in this task.
    (17277777, 17277776.0),
    (17277779, 17277780.0),
    (-17277777, -17277776.0),
    (-17277779, -17277780.0)
  )

  for ((intValue, floatValue) <- testInputs) {
    it should s"convert $intValue to $floatValue" in {
      simulate(new RoundedIntegerToFloatingPoint) { dut =>
        pokeSignedMagnitude(dut.intInput, intValue)
        dut.clock.step()
        expectDouble(dut.floatOutput, floatValue)
      }
    }
  }
}
