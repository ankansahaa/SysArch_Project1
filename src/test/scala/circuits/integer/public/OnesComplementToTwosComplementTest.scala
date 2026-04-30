package sysarch.circuits.integer.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec
import sysarch.circuits.integer._
import sysarch.circuits.util._

class OnesComplementToTwosComplementTest extends AnyFlatSpec with ChiselSim {

  behavior of "OnesComplementToTwosComplement"

  val testInputs = Seq(
    (2, BigInt(0)),
    (2, BigInt(1)),
    (4, BigInt(-7)),
    (32, BigInt(0)),
    (32, BigInt(1)),
    (32, BigInt(-1)),
    (32, (BigInt(1) << 31) - 1),
    (32, -(BigInt(1) << 31) + 1)
  )

  for ((width, value) <- testInputs) {
    it should s"convert $value correctly for width $width" in {
      simulate(new OnesComplementToTwosComplement(width)) { dut =>
        val inputBits    = OnesComplement.bigIntToOnesComplement(value, width)
        val expectedBits = TwosComplement.bigIntToTwosComplement(value, width)

        for (i <- 0 until width) {
          dut.onesComplement(i).poke(inputBits(i))
        }
        dut.clock.step()

        for (i <- 0 until width) {
          dut
            .twosComplement(i)
            .expect(
              if (expectedBits(i)) true.B else false.B,
              s"Bit $i did not match expected value for input $value with width $width.\n" +
                s"Expected ${(for (i <- 0 until width) yield if (expectedBits(i)) "1" else "0").mkString} \n" +
                s"but got  ${(for (i <- 0 until width)
                    yield if (dut.twosComplement(i).peek().litToBoolean) "1" else "0").mkString}.\n"
            )
        }
      }
    }
  }
}
