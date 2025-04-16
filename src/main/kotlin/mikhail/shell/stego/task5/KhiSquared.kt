package mikhail.shell.stego.task5

import org.apache.commons.math3.distribution.ChiSquaredDistribution
import java.awt.image.BufferedImage
import kotlin.math.pow

val Int.B get() = this and 0xFF

fun Int.lastBits(n: Int) = this and (2.0.pow(n) - 1).toInt()

fun BufferedImage.evaluateActualColorFrequencies(lastBits: Int = 1): IntArray {
    val result = IntArray(2.0.pow(lastBits).toInt()) { 0 }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = getRGB(x, y)
            val lsb = pixel.B.lastBits(lastBits)
            result[lsb]++
        }
    }
    return result
}

fun BufferedImage.evaluateExpectedColorFrequencies(lastBits: Int = 1) = IntArray(2.0.pow(lastBits).toInt()) { width * height / 2.0.pow(lastBits).toInt() }

fun evaluateKhiSquared(expected: IntArray, actual: IntArray): Pair<Double, Int> {
    var khiSquare = 0.0

    for (i in expected.indices) {
        khiSquare += (expected[i] - actual[i]).toDouble().pow(2) / expected[i]
    }

    val df = expected.size - 1
    return Pair(khiSquare, df)
}

fun evaluateP(chi2Stat: Double, degreesOfFreedom: Int): Double {
    if (degreesOfFreedom <= 0 || chi2Stat < 0) return 1.0
    return try {
        val chi2Dist = ChiSquaredDistribution(degreesOfFreedom.toDouble())
        1 - chi2Dist.cumulativeProbability(chi2Stat)
    } catch (e: Exception) {
        1.0
    }
}