package mikhail.shell.stego.task5

import org.apache.commons.math3.distribution.ChiSquaredDistribution
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.math.pow

val Int.B get() = this and 0xFF

fun Int.lastBits(n: Int) = this and (2.0.pow(n) - 1).toInt()

fun BufferedImage.getBytes(): Array<Int> {
	val bytes = (raster.dataBuffer as DataBufferByte).data
	return bytes.map { it.toInt() and 0xFF }.toTypedArray()
} 

fun Array<Int>.toMatrix(w: Int, h: Int): Array<Array<Int>> {
    return Array(h) { i -> Array(w) { j -> this[i * w + j] } }
}

fun Array<Array<Int>>.chunk(m: Int): Array<Array<Array<Array<Int>>>> {
    val n = this.size
    return Array(n / m) { i ->
        Array(n / m) { j ->
            Array(m) { x ->
                Array(m) { y ->
                    this[i * m + x][j * m + y]
                }
            }
        }
    }
}

fun Array<Array<Int>>.evaluateActualColorFrequencies(lastBits: Int = 1): IntArray {
    val result = IntArray(2.0.pow(lastBits).toInt()) { 0 }

	val rowsNumbers = this[0].size
	val colsNumber = size

    for (y in 0 until rowsNumbers) {
        for (x in 0 until colsNumber) {
            val pixel = this[y][x]
            val lsb = pixel.B.lastBits(lastBits)
            result[lsb]++
        }
    }
    return result
}

fun Array<Array<Int>>.evaluateExpectedColorFrequencies(lastBits: Int = 1): IntArray {
	val rowsNumbers = this[0].size
	val colsNumber = size
	return IntArray(2.0.pow(lastBits).toInt()) { 
		colsNumber * rowsNumbers / 2.0.pow(lastBits).toInt()
	}
} 

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