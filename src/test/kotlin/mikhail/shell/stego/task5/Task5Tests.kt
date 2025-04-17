package mikhail.shell.stego.task5

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Task5Tests {
    @Test
    fun findRSThreshold() {
        val clean = listOf(0.348,	0.24,	0.274,	0.289,	0.547,	0.158,	0.0764,	0.263,	0.119,	0.517)
        val stego = listOf(0.267,	0.215,	0.255,	0.253,	0.493,	0.153,	0.0593,	0.239,	0.0792,	0.422)
        val threshold = findOptimalThreshold(clean, stego)
        println(threshold)
        Assertions.assertTrue(threshold > 0)
    }
    @Test
    fun findKhi2Threshold() {
        val clean = listOf(0.0,	0.257,	0.0216,	0.0,	0.833,	0.0369,	0.0,	0.00323,	0.0,	0.337)
        val stego = listOf(0.0, 0.7,	0.179,	0.0,	0.0,	0.00375,	0.0,	0.0006,	0.0,	0.00009)
        val threshold = findOptimalThreshold(clean, stego)
        println(threshold)
        Assertions.assertTrue(threshold > 0)
    }

    @Test
    fun findAumpThreshold() {
        val clean = listOf(0.24, 0.0305, 0.05, 0.179, 0.0212, 0.0682, 0.233, 0.0532, 0.353, 0.0011)
        val stego = listOf(0.28, 0.063, 0.058, 0.214, 0.0093, 0.0376, 0.252, 0.075)
        val threshold = findOptimalThreshold(clean, stego, direction = Comparison.GREATER)
        println(threshold)
        Assertions.assertTrue(threshold > 0)
    }

    enum class Comparison {
        LESS,
        GREATER
    }

    fun findOptimalThreshold(
        clean: List<Double>,
        stego: List<Double>,
        steps: Int = 10_000,
        direction: Comparison = Comparison.LESS
    ): Double {
        val allCandidates = (0..steps).map { it.toDouble() / steps }  // 0.0, 0.0001, ..., 1.0

        var bestT = 0.0
        var bestJ = Double.NEGATIVE_INFINITY

        for (t in allCandidates) {
            val tpr = when (direction) {
                Comparison.LESS -> stego.count { it < t }.toDouble() / stego.size
                Comparison.GREATER -> stego.count { it > t }.toDouble() / stego.size
            }

            val fpr = when (direction) {
                Comparison.LESS -> clean.count { it < t }.toDouble() / clean.size
                Comparison.GREATER -> clean.count { it > t }.toDouble() / clean.size
            }

            val j = tpr - fpr  // Youden's J statistic
            if (j > bestJ) {
                bestJ = j
                bestT = t
            }
        }

        return bestT
    }
}