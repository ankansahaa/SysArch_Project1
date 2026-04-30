package sysarch.circuits.floatingpoint.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.util._
import sysarch.circuits.floatingpoint._

class FloatingPointToIntegerTest extends AnyFlatSpec with ChiselSim {

  private def pokeFloat32(vec: chisel3.Vec[chisel3.Bool], value: Double): Unit = {
    val bits = FloatingPoint.doubleTo32BitIEEE754(value)
    for (i <- 0 until 32) {
      vec(i).poke(bits(i).B)
    }
  }

  private def expectSignedMagnitude(vec: chisel3.Vec[chisel3.Bool], expected: BigInt): Unit = {
    val expectedBits = SignedMagnitude.bigIntToSignedMagnitude(expected, 32)
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
    (1.0, 1),
    (-1.0, -1),
    (0.0, 0),
    (-1.25, -1),
    (-1.5, -1),
    (-1.75, -1),
    (0.75, 0),
    (0.5, 0),
    (0.25, 0),
    (2147483648.0, 2147483647)
  )

  for ((floatValue, intValue) <- testInputs) {
    it should s"convert $floatValue to $intValue" in {
      simulate(new FloatingPointToInteger) { dut =>
        pokeFloat32(dut.floatInput, floatValue)
        dut.clock.step()
        expectSignedMagnitude(dut.intOutput, intValue)
      }
    }
  }
}
