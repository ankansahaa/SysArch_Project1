package sysarch.circuits.util

object FloatingPoint {
  // Convert a double to its IEEE 754 binary representation as a string

  def doubleToIEEE754(x: Double, eBits: Int, mBits: Int): Seq[Boolean] = {
    if (x == 0.0) {
      return Seq.fill(1 + eBits + mBits)(false)
    }

    val signBit  = if (x < 0) true else false
    val absValue = math.abs(x)
    var value    = absValue

    // Normalize value to 1.xxx * 2^exp
    var exp = 0
    while (value >= 2.0) {
      value /= 2.0
      exp += 1
    }
    while (value < 1.0) {
      value *= 2.0
      exp -= 1
    }

    val bias      = (BigInt(1) << (eBits - 1)) - 1
    val storedExp = BigInt(exp) + bias

    // Handle overflow (Infinity)
    if (storedExp >= ((BigInt(1) << eBits) - 1)) {
      return Seq.fill(mBits)(false) ++ Seq.fill(eBits)(true) ++ Seq(signBit)
    }

    val minSubnormalExponent = (BigInt(1) - bias - BigInt(mBits)).toDouble
    val minSubnormal         = math.pow(2, minSubnormalExponent)
    if (absValue < minSubnormal) {
      return Seq.fill(mBits)(false) ++ Seq.fill(eBits)(false) ++ Seq(signBit)
    }

    // Handle underflow (denormals)
    if (storedExp <= 0) {
      val exponentBits = Seq.fill(eBits)(false)

      val shift    = (BigInt(1) - bias).toDouble
      var fraction = value / math.pow(2, shift)

      var mantissaBits = Seq[Boolean]()

      for (_ <- 0 until mBits) {
        fraction *= 2
        if (fraction >= 1.0) {
          mantissaBits = mantissaBits :+ true
          fraction -= 1.0
        } else {
          mantissaBits = mantissaBits :+ false
        }
      }

      return mantissaBits.reverse ++ exponentBits.reverse ++ Seq(signBit)
    }

    // Convert exponent to binary
    val exponentBits = storedExp.toString(2).reverse.padTo(eBits, '0').reverse.map(_ == '1')

    // Extract mantissa
    var fraction     = value - 1.0
    var mantissaBits = Seq[Boolean]()

    for (_ <- 0 until mBits) {
      fraction *= 2
      if (fraction >= 1.0) {
        mantissaBits = mantissaBits :+ true
        fraction -= 1.0
      } else {
        mantissaBits = mantissaBits :+ false
      }
    }

    mantissaBits.reverse ++ exponentBits.reverse ++ Seq(signBit)
  }

  def ieee754ToDouble(_bits: Seq[Boolean], eBits: Int, mBits: Int): Double = {
    val bits = _bits.map(b => if (b) '1' else '0').reverse.mkString
    require(bits.length == 1 + eBits + mBits, "Invalid bit string length")

    val sign = if (bits(0) == '1') -1.0 else 1.0

    val expBits  = bits.slice(1, 1 + eBits)
    val mantBits = bits.slice(1 + eBits, bits.length)

    val expInt  = BigInt(expBits, 2)
    val mantInt = BigInt(mantBits, 2)

    val bias = (BigInt(1) << (eBits - 1)) - 1

    // Convert mantissa to fraction
    var fraction = 0.0
    for (i <- 0 until mBits) {
      if (mantBits(i) == '1') {
        fraction += math.pow(2, -(i + 1))
      }
    }

    // Case 1: exponent all zeros
    if (expInt == BigInt(0)) {
      if (mantInt == BigInt(0)) {
        return sign * 0.0 // zero
      } else {
        // subnormal
        return sign * fraction * math.pow(2, (BigInt(1) - bias).toDouble)
      }
    }

    // Case 2: exponent all ones
    if (expInt == ((BigInt(1) << eBits) - 1)) {
      if (mantInt == BigInt(0)) {
        return sign * Double.PositiveInfinity
      } else {
        return Double.NaN
      }
    }

    // Case 3: normal number
    val exponent = (expInt - bias).toDouble
    sign * (1.0 + fraction) * math.pow(2, exponent)
  }

  def doubleTo32BitIEEE754(x: Double): Seq[Boolean]    = doubleToIEEE754(x, 8, 23)
  def doubleTo64BitIEEE754(x: Double): Seq[Boolean]    = doubleToIEEE754(x, 11, 52)
  def ieee754To32BitDouble(bits: Seq[Boolean]): Double = ieee754ToDouble(bits, 8, 23)
  def ieee754To64BitDouble(bits: Seq[Boolean]): Double = ieee754ToDouble(bits, 11, 52)
}
