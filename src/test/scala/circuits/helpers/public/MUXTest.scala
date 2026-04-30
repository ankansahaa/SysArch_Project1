package sysarch.circuits.helpers.public

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec
import sysarch.circuits.helpers._
import sysarch.circuits.util._
import chisel3.simulator.PeekPokeAPI.TestableRecord
import chisel3.simulator.PeekPokeAPI.TestableEnum

class MuxTest extends AnyFlatSpec with ChiselSim {

  behavior of "Mux"

  it should "select the first input when selector is false" in {
    simulate(new Mux(2)) { dut =>
      val a   = Seq(true.B, false.B)
      val b   = Seq(false.B, true.B)
      val sel = false.B
      for (i <- 0 until 2) {
        dut.a(i).poke(a(i))
        dut.b(i).poke(b(i))
      }
      dut.sel.poke(sel)
      dut.clock.step()
      for (i <- 0 until 2) {
        dut.out(i).expect(a(i))
      }
    }
  }

  it should "select the second input when selector is true" in {
    simulate(new Mux(2)) { dut =>
      val a   = Seq(true.B, false.B)
      val b   = Seq(false.B, true.B)
      val sel = true.B
      for (i <- 0 until 2) {
        dut.a(i).poke(a(i))
        dut.b(i).poke(b(i))
      }
      dut.sel.poke(sel)
      dut.clock.step()
      for (i <- 0 until 2) {
        dut.out(i).expect(b(i))
      }
    }
  }

  it should "work with one bit inputs" in {
    simulate(new Mux(1)) { dut =>
      dut.a(0).poke(false.B)
      dut.b(0).poke(true.B)
      dut.sel.poke(false.B)
      dut.clock.step()
      dut.out(0).expect(false.B)

      dut.sel.poke(true.B)
      dut.clock.step()
      dut.out(0).expect(true.B)
    }
  }

  it should "work with larger inputs" in {
    simulate(new Mux(4)) { dut =>
      val a = Seq(false.B, false.B, true.B, true.B)
      val b = Seq(true.B, true.B, false.B, false.B)
      for (i <- 0 until 4) {
        dut.a(i).poke(a(i))
        dut.b(i).poke(b(i))
      }
      dut.sel.poke(false.B)
      dut.clock.step()
      for (i <- 0 until 4) {
        dut.out(i).expect(a(i))
      }
      dut.sel.poke(true.B)
      dut.clock.step()
      for (i <- 0 until 4) {
        dut.out(i).expect(b(i))
      }
    }
  }

}
