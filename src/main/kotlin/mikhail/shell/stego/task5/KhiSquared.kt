package mikhail.shell.stego.task5

import org.apache.commons.math3.distribution.ChiSquaredDistribution
import java.awt.image.BufferedImage
import kotlin.math.pow

val Int.B get() = this and 0xFF

fun BufferedImage.evaluateColorFrequencies(): IntArray {
    val result = IntArray(256) { 0 }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = getRGB(x, y)
            val blue = pixel.B
            result[blue]++
        }
    }
    return result
}

fun IntArray.evaluateKhiSquared(): Pair<Double, Int> {
    var khiSquare = 0.0
    var validPairs = 0

    for (i in 0 until 128) {
        val n1 = this[2 * i].toDouble()
        val n2 = this[2 * i + 1].toDouble()
        val sum = n1 + n2

        if (sum > 0) {
            val expected = sum / 2.0
            khiSquare += (n1 - expected).pow(2) / expected
            khiSquare += (n2 - expected).pow(2) / expected
            validPairs++
        }
    }

    val df = validPairs - 1
    khiSquare /= 2 * validPairs
    return Pair(khiSquare, df)
}

fun evaluateP(chi2Stat: Double, degreesOfFreedom: Int): Double {
    if (degreesOfFreedom <= 0 || chi2Stat < 0) return 1.0
    return try {
        val chi2Dist = ChiSquaredDistribution(degreesOfFreedom.toDouble())
        1.0 - chi2Dist.cumulativeProbability(chi2Stat)
    } catch (e: Exception) {
        1.0
    }
}