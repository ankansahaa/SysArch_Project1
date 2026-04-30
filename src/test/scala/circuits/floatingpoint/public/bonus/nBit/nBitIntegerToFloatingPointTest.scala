package sysarch.circuits.floatingpoint.public.bonus

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.floatingpoint.bonus.nBit.nBitIntegerToFloatingPoint
import sysarch.circuits.util.FloatingPoint
import sysarch.circuits.util.SignedMagnitude

class nBitIntegerToFloatingPointTest extends AnyFlatSpec with ChiselSim {

  private def pokeSignedMagnitude(
      vec: chisel3.Vec[chisel3.Bool],
      value: BigInt,
      integerWidth: Int
  ): Unit = {
    val bits = SignedMagnitude.bigIntToSignedMagnitude(value, integerWidth)
    for (i <- 0 until integerWidth) {
      vec(i).poke(bits(i).B)
    }
  }

  private def expectDouble(
      vec: chisel3.Vec[chisel3.Bool],
      expected: Double,
      exponentWidth: Int,
      mantissaWidth: Int
  ): Unit = {
    val expectedBits = FloatingPoint.doubleToIEEE754(expected, exponentWidth, mantissaWidth)
    for (i <- 0 until (exponentWidth + mantissaWidth + 1)) {
      vec(i).expect(
        if (expectedBits(i)) true.B else false.B,
        s"Bit $i did not match expected value for input $expected.\n" +
          s"Expected ${(for (i <- 0 until (exponentWidth + mantissaWidth + 1))
              yield if (expectedBits(i)) "1" else "0").mkString} \n" +
          s"but got  ${(for (i <- 0 until (exponentWidth + mantissaWidth + 1))
              yield if (vec(i).peek().litToBoolean) "1" else "0").mkString}.\n"
      )
    }
  }

  val testInputs = Seq(
    (9, 10, 16, 1, 1.0),
    (9, 10, 16, 32736, 32736.0),
    (9, 10, 16, 128, 128.0),
    // Note: 7545 cannot be exactly represented in IEEE 754 16-Bit, we expect round towards zero in this task.
    (9, 10, 16, 7545, 7544.0),
    (11, 52, 64, 17277779, 17277779.0)
  )

  for ((exponentWidth, mantissaWidth, integerWidth, intValue, floatValue) <- testInputs) {
    it should s"convert $intValue to $floatValue ($exponentWidth-$mantissaWidth-$integerWidth)" in {
      simulate(new nBitIntegerToFloatingPoint(exponentWidth, mantissaWidth, integerWidth)) { dut =>
        pokeSignedMagnitude(dut.intInput, intValue, integerWidth)
        dut.clock.step()
        expectDouble(dut.floatOutput, floatValue, exponentWidth, mantissaWidth)
      }
    }
  }
}
