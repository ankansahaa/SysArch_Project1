package sysarch.circuits.util

object OnesComplement {

  def bigIntToOnesComplement(_value: BigInt, bitWidth: Int): Seq[Boolean] = {
    var value = _value
    if (value < 0) {
      value = value - 1
    }
    (0 until bitWidth).map(i => ((value >> i) & 1) == 1)
  }

  def onesComplementToBigInt(bits: Seq[Boolean]): BigInt = {
    val bitWidth = bits.length
    val value = bits.zipWithIndex.map { case (bit, i) =>
      if (bit) BigInt(1) << i else BigInt(0)
    }.sum

    // Check if the number is negative (if the most significant bit is 1)
    if (bits.last) {
      value - ((BigInt(1) << bitWidth) - 1)
    } else {
      value
    }
  }
}
