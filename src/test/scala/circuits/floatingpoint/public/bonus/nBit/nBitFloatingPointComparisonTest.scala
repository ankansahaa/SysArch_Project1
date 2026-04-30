package sysarch.circuits.floatingpoint.public.bonus

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import sysarch.circuits.floatingpoint.bonus.nBit.nBitFloatingPointComparison
import sysarch.circuits.util.FloatingPoint
import sysarch.circuits.floatingpoint.FloatingPointComparison

class nBitFloatingPointComparisonTest extends AnyFlatSpec with ChiselSim {

  private def pokeFloat(
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

  private val testInputs = Seq(
    (5, 10, 1.0, 1.0, false, false),
    (5, 10, 2.0, 1.0, true, false),
    (5, 10, 1.0, 2.0, false, true),
    (5, 10, -1.0, 1.0, false, true),
    (5, 1024, -4.0, -2.0, false, true),
    (12, 10, 0.0, -0.0, false, false),
    (32, 32, 1.0, 2.0, false, true)
  )

  for ((exponentWidth, mantissaWidth, a, b, expGt, expLt) <- testInputs) {
    it should s"compare values a=$a b=$b correctly for e=$exponentWidth m=$mantissaWidth" in {
      simulate(new nBitFloatingPointComparison(exponentWidth, mantissaWidth)) { dut =>
        pokeFloat(dut.a, a, exponentWidth, mantissaWidth)
        pokeFloat(dut.b, b, exponentWidth, mantissaWidth)
        dut.clock.step()

        dut.comparisonResult(0).expect(expGt.B)
        dut.comparisonResult(1).expect(expLt.B)
      }
    }
  }

  private val widths = Seq(
    (5, 10),
    (6, 9),
    (8, 23)
  )

  for ((exponentWidth, mantissaWidth) <- widths) {
    it should s"return unordered for NaN for e=$exponentWidth m=$mantissaWidth" in {
      simulate(new nBitFloatingPointComparison(exponentWidth, mantissaWidth)) { dut =>
        val nanBits = (0 until (exponentWidth + mantissaWidth + 1)).map(i =>
          if (i == 0 || (i >= mantissaWidth && i < (mantissaWidth + exponentWidth))) true else false
        )
        for (i <- 0 until (exponentWidth + mantissaWidth + 1)) {
          dut.a(i).poke(nanBits(i).B)
          dut.b(i).poke(nanBits(i).B)
        }
        dut.clock.step()
        dut.comparisonResult(0).expect(true.B)
        dut.comparisonResult(1).expect(true.B)
      }
    }

    it should s"return unordered for NaN (a) for e=$exponentWidth m=$mantissaWidth" in {
      simulate(new nBitFloatingPointComparison(exponentWidth, mantissaWidth)) { dut =>
        val nanBits = (0 until (exponentWidth + mantissaWidth + 1)).map(i =>
          if (i == 0 || (i >= mantissaWidth && i < (mantissaWidth + exponentWidth))) true else false
        )
        for (i <- 0 until (exponentWidth + mantissaWidth + 1)) {
          dut.a(i).poke(nanBits(i).B)
        }
        pokeFloat(dut.b, 1.0, exponentWidth, mantissaWidth)
        dut.clock.step()
        dut.comparisonResult(0).expect(true.B)
        dut.comparisonResult(1).expect(true.B)
      }
    }

    it should s"return unordered for NaN (b) for e=$exponentWidth m=$mantissaWidth" in {
      simulate(new nBitFloatingPointComparison(exponentWidth, mantissaWidth)) { dut =>
        val nanBits = (0 until (exponentWidth + mantissaWidth + 1)).map(i =>
          if (i == 0 || (i >= mantissaWidth && i < (mantissaWidth + exponentWidth))) true else false
        )
        for (i <- 0 until (exponentWidth + mantissaWidth + 1)) {
          dut.b(i).poke(nanBits(i).B)
        }
        pokeFloat(dut.a, 1.0, exponentWidth, mantissaWidth)
        dut.clock.step()
        dut.comparisonResult(0).expect(true.B)
        dut.comparisonResult(1).expect(true.B)
      }
    }
  }
}
