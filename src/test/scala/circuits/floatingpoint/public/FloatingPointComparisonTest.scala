package sysarch.circuits.floatingpoint.public

import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec

import chisel3._
import chisel3.util._
import sysarch.circuits.util._
import sysarch.circuits.floatingpoint._

class FloatingPointComparisonTest extends AnyFlatSpec with ChiselSim {

  private def pokeFloat(vec: chisel3.Vec[chisel3.Bool], value: Double): Unit = {
    val bits = FloatingPoint.doubleToIEEE754(value, 8, 23)
    for (i <- 0 until 32) {
      vec(i).poke(bits(i).B)
    }
  }

  val testInputs = Seq(
    // Equal
    (1.0, 1.0, false.B, false.B),
    // Different signs
    (1.0, -1.0, true.B, false.B), // floatInput1 > floatInput2
    // Different exponents
    (1.0, 2.0, false.B, true.B),       // floatInput2 > floatInput1
    (2.0, 1.0, true.B, false.B),       // floatInput1 > floatInput2
    (4096.0, 1.0, true.B, false.B),    // floatInput1 > floatInput2
    (4096.0, 4095.0, true.B, false.B), // floatInput1 > floatInput2
    // Different mantissas
    (1.0, 1.1, false.B, true.B),                     // floatInput2 > floatInput1
    (426391340000.0, 1.9661024e30, false.B, true.B), // floatInput2 > floatInput1
    (-1.1639989e12, -0.010260136, false.B, true.B)   // floatInput2 > floatInput1
  )

  for ((float1, float2, expectedGreater, expectedNotGreater) <- testInputs) {
    it should s"compare $float1 and $float2 correctly" in {
      simulate(new FloatingPointComparison) { dut =>
        val floatBits1 = FloatingPoint.doubleToIEEE754(float1, 8, 23).toVector
        val floatBits2 = FloatingPoint.doubleToIEEE754(float2, 8, 23).toVector
        for (i <- 0 until 32) {
          pokeFloat(dut.a, float1)
          pokeFloat(dut.b, float2)
        }
        dut.clock.step()
        dut.comparisonResult(0).expect(expectedGreater)    // floatInput1 > floatInput2
        dut.comparisonResult(1).expect(expectedNotGreater) // ! (floatInput2 > floatInput1)
      }
    }
  }

  it should "return unordered for NaN" in {
    simulate(new FloatingPointComparison) { dut =>
      val nanBits = (0 until 32).map(i => if (i == 0 || (i >= 23 && i < 32)) true else false)
      for (i <- 0 until 32) {
        dut.a(i).poke(nanBits(i).B)
        dut.b(i).poke(nanBits(i).B)
      }
      dut.clock.step()
      dut.comparisonResult(0).expect(true.B)
      dut.comparisonResult(1).expect(true.B)
    }
  }

  it should "return unordered for NaN (a)" in {
    simulate(new FloatingPointComparison) { dut =>
      val nanBits = (0 until 32).map(i => if (i == 22 || (i >= 23 && i < 32)) true else false)
      for (i <- 0 until 32) {
        dut.a(i).poke(nanBits(i).B)
      }
      pokeFloat(dut.b, 0.0)
      dut.clock.step()
      dut.comparisonResult(0).expect(true.B)
      dut.comparisonResult(1).expect(true.B)
    }
  }

  it should "return unordered for NaN (b)" in {
    simulate(new FloatingPointComparison) { dut =>
      val nanBits = (0 until 32).map(i => if (i == 22 || (i >= 23 && i < 32)) true else false)
      for (i <- 0 until 32) {
        dut.b(i).poke(nanBits(i).B)
      }
      pokeFloat(dut.a, 0.0)
      dut.clock.step()
      dut.comparisonResult(0).expect(true.B)
      dut.comparisonResult(1).expect(true.B)
    }
  }

}
