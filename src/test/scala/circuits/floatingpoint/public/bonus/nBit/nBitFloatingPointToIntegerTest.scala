package sysarch.circuits.floatingpoint.public.bonus

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.floatingpoint.bonus.nBit.nBitFloatingPointToInteger
import sysarch.circuits.util.FloatingPoint
import sysarch.circuits.util.SignedMagnitude

class nBitFloatingPointToIntegerTest extends AnyFlatSpec with ChiselSim {

  private def pokeFloat32(
      vec: chisel3.Vec[chisel3.Bool],
      value: Double,
      exponentWidth: Int,
      mantissaWidth: Int
  ): Unit = {
    val bits = FloatingPoint.doubleToIEEE754(value, exponentWidth, mantissaWidth)
    for (i <- 0 until (exponentWidth + mantissaWidth + 1)) {
      vec(i).poke(bits(i).B)
    }
  }

  private def expectSignedMagnitude(
      vec: chisel3.Vec[chisel3.Bool],
      expected: BigInt,
      integerWidth: Int
  ): Unit = {
    val expectedBits = SignedMagnitude.bigIntToSignedMagnitude(expected, integerWidth)
    for (i <- 0 until integerWidth) {
      vec(i).expect(
        if (expectedBits(i)) true.B else false.B,
        s"Bit $i did not match expected value for input $expected.\n" +
          s"Expected ${(for (i <- 0 until integerWidth) yield if (expectedBits(i)) "1" else "0").mkString} \n" +
          s"but got  ${(for (i <- 0 until integerWidth)
              yield if (vec(i).peek().litToBoolean) "1" else "0").mkString}.\n"
      )
    }
  }

  val testInputs = Seq(
    (8, 23, 32, 1.0, 1),
    (5, 10, 16, -1.0, -1),
    (32, 32, 32, 0.0, 0),
    (8, 32, 64, -1.25, -1),
    (5, 10, 16, -1.5, -1),
    (5, 10, 16, -1.75, -1),
    (5, 10, 16, 0.75, 0),
    (5, 10, 16, 0.5, 0),
    (5, 10, 16, 0.25, 0),
    (8, 23, 4, 2147483648.0, 7)
  )

  for ((exponentWidth, mantissaWidth, integerWidth, floatValue, intValue) <- testInputs) {
    it should s"convert $floatValue to $intValue ($exponentWidth,$mantissaWidth,$integerWidth)" in {
      simulate(new nBitFloatingPointToInteger(exponentWidth, mantissaWidth, integerWidth)) { dut =>
        pokeFloat32(dut.floatInput, floatValue, exponentWidth, mantissaWidth)
        dut.clock.step()
        expectSignedMagnitude(dut.intOutput, intValue, integerWidth)
      }
    }
  }
}
