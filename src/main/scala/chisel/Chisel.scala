package sysarch

import chisel3._

object chisel {
  // Type aliases
  type IO                     = chisel3.IO.type
  type Bundle                 = chisel3.Bundle
  type Module                 = chisel3.Module
  type Vec[T <: chisel3.Data] = chisel3.Vec[T]
  type Bool                   = chisel3.Bool

  // Forward values/functions
  val IO     = chisel3.IO
  val Input  = chisel3.Input
  val Output = chisel3.Output
  val Bool   = chisel3.Bool
  val Module = chisel3.Module
  val Wire   = chisel3.Wire

  object Vec {
    def apply[T <: chisel3.Data](length: Int, gen: T): chisel3.Vec[T] = chisel3.Vec(length, gen)

    // Convenience helper to mirror collection-style fill when wiring slices.
    def fill[T <: chisel3.Data](length: Int)(gen: => T): Seq[T] = Seq.fill(length)(gen)
  }

  def printf(pable: chisel3.Printable) = chisel3.printf(pable)

  implicit class PrintableInterpolator(private val sc: StringContext) extends AnyVal {
    def cf(args: Any*): chisel3.Printable = new chisel3.PrintableHelper(sc).cf(args: _*)
  }

  implicit class BoolLit(b: Boolean) {
    def B: chisel3.Bool = if (b) (1.U(1.W)).asBool else (0.U(1.W)).asBool
  }

  implicit class SeqHelper(val seq: Seq[Bits]) {

    /** Promotes a Seq of Bits to a class that supports the connect operator
      */
    def :=(other: Seq[Bits]): Unit = {
      seq.zip(other).foreach { case (a, b) => a := b }
    }
  }

  implicit class VecHelper[T <: Bits](private val vec: chisel3.Vec[T]) extends AnyVal {
    def init: Seq[T] = {
      if (vec.length == 0) throw new UnsupportedOperationException("empty Vec.init")
      vec.slice(0, vec.length - 1)
    }
  }
}
