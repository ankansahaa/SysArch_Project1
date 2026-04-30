package sysarch.circuits.floatingpoint.public.bonus

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.floatingpoint.bonus.nBit.nBitFloatingPointAddition
import sysarch.circuits.util.FloatingPoint

class nBitFloatingPointAdditionTest extends AnyFlatSpec with ChiselSim {

  private def pokeFloat(
      vec: chisel3.Vec[chisel3.Bool],
      value: Double,
      exponentWidth: Int,
      mantissaWidth: Int
  ): Unit = {
    val bits = FloatingPoint.doubleToIEEE754(value, exponentWidth, mantissaWidth)
    for (i <- 0 until (1 + exponentWidth + mantissaWidth)) {
      vec(i).poke(bits(i).B)
    }
  }

  private def expectFloat(
      vec: chisel3.Vec[chisel3.Bool],
      expected: Double,
      exponentWidth: Int,
      mantissaWidth: Int
  ): Unit = {
    val expectedBits = FloatingPoint.doubleToIEEE754(expected, exponentWidth, mantissaWidth)
    for (i <- 0 until (1 + exponentWidth + mantissaWidth)) {
      vec(i).expect(
        if (expectedBits(i)) true.B else false.B,
        s"Bit $i did not match expected value for input $expected.\n" +
          s"Expected ${(for (i <- 0 until (1 + exponentWidth + mantissaWidth))
              yield if (expectedBits(i)) "1" else "0").mkString} \n" +
          s"but got  ${(for (i <- 0 until (1 + exponentWidth + mantissaWidth))
              yield if (vec(i).peek().litToBoolean) "1" else "0").mkString}.\n"
      )
    }
  }

  val testInputs = Seq(
    (2, 2, 1.0, 1.0, 2.0),
    (5, 10, 1.5, 0.5, 2.0),
    (10, 5, 2.5, -1.5, 1.0),
    (5, 10, 1.556, -0.0005, 1.556)
  )

  for ((exponentWidth, mantissaWidth, a, b, expected) <- testInputs) {
    it should s"add $a and $b correctly for $exponentWidth-bit exponent and $mantissaWidth-bit mantissa" in {
      simulate(new nBitFloatingPointAddition(exponentWidth, mantissaWidth)) { dut =>
        pokeFloat(dut.a, a, exponentWidth, mantissaWidth)
        pokeFloat(dut.b, b, exponentWidth, mantissaWidth)

        dut.clock.step()

        expectFloat(dut.out, expected, exponentWidth, mantissaWidth)
      }
    }
  }
}
