package sysarch.circuits.util

object SignedMagnitude {

  def bigIntToSignedMagnitude(value: BigInt, bitWidth: Int): Seq[Boolean] = {
    val signBit   = value < 0
    val magnitude = if (signBit) -value else value
    (0 until bitWidth - 1).map(i => ((magnitude >> i) & 1) == 1) ++ Seq(signBit)
  }

  def signedMagnitudeToBigInt(bits: Seq[Boolean]): BigInt = {
    val bitWidth = bits.length
    val signBit  = bits.last
    val magnitude = bits
      .dropRight(1)
      .zipWithIndex
      .map { case (bit, i) =>
        if (bit) BigInt(1) << i else BigInt(0)
      }
      .sum

    if (signBit) -magnitude else magnitude
  }
}
