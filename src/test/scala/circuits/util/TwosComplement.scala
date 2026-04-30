package sysarch.circuits.util

object TwosComplement {

  def bigIntToTwosComplement(value: BigInt, bitWidth: Int): Seq[Boolean] = {
    val mask                = (BigInt(1) << bitWidth) - 1
    val twosComplementValue = value & mask
    (0 until bitWidth).map(i => ((twosComplementValue >> i) & 1) == 1)
  }

  def twosComplementToBigInt(bits: Seq[Boolean]): BigInt = {
    val bitWidth = bits.length
    val value = bits.zipWithIndex.map { case (bit, i) =>
      if (bit) BigInt(1) << i else BigInt(0)
    }.sum

    // Check if the number is negative (if the most significant bit is 1)
    if (bits.last) {
      value - (BigInt(1) << bitWidth)
    } else {
      value
    }
  }
}
