package mikhail.shell.stego.task5.aump

import kotlin.math.pow

fun ws(S: Array<Array<Int>>, bias: String): Double {
    val M = S.size
    val N = if (M > 0) S[0].size else 0
    val SDouble = S.map { row -> row.map { it.toDouble() }.toTypedArray() }.toTypedArray()

    // Создание Sbar с инвертированными LSB
    val Sbar = SDouble.map { row ->
        row.map { pixel ->
            pixel + 1 - 2 * (pixel % 2)
        }.toTypedArray()
    }.toTypedArray()

    // Вычисление локальной дисперсии (3x3 окно)
    val varS = localVar(SDouble, 3)

    // Веса и нормализация
    val w = Array(M - 2) { DoubleArray(N - 2) }
    var sumW = 0.0
    for (i in 0 until M - 2) {
        for (j in 0 until N - 2) {
            w[i][j] = 1.0 / (5 + varS[i + 1][j + 1])
            sumW += w[i][j]
        }
    }
    for (i in w.indices) {
        for (j in w[i].indices) {
            w[i][j] /= sumW
        }
    }

    // Вычисление X_hat с ядром KB
    val X_hat = Array(M - 2) { DoubleArray(N - 2) }
    for (i in 1 until M - 1) {
        for (j in 1 until N - 1) {
            X_hat[i - 1][j - 1] = 0.25 * (
                    -SDouble[i - 1][j - 1] - SDouble[i + 1][j - 1]
                            - SDouble[i + 1][j + 1] - SDouble[i - 1][j + 1]
                            + 2 * (SDouble[i][j - 1] + SDouble[i][j + 1]
                            + SDouble[i - 1][j] + SDouble[i + 1][j])
                    )
        }
    }

    // Основная оценка beta_hat
    var betaHat = 0.0
    for (i in 0 until M - 2) {
        for (j in 0 until N - 2) {
            betaHat += w[i][j] *
                    (SDouble[i + 1][j + 1] - X_hat[i][j]) *
                    (SDouble[i + 1][j + 1] - Sbar[i + 1][j + 1])
        }
    }

    // Коррекция смещения
    if (bias.equals("yes", ignoreCase = true)) {
        val D = Array(M) { i -> DoubleArray(N) { j -> Sbar[i][j] - SDouble[i][j] } }
        val FD = Array(M - 2) { DoubleArray(N - 2) }

        for (i in 1 until M - 1) {
            for (j in 1 until N - 1) {
                FD[i - 1][j - 1] = 0.25 * (
                        -D[i - 1][j - 1] - D[i + 1][j - 1]
                                - D[i + 1][j + 1] - D[i - 1][j + 1]
                                + 2 * (D[i][j - 1] + D[i][j + 1]
                                + D[i - 1][j] + D[i + 1][j])
                        )
            }
        }

        var b = 0.0
        for (i in 0 until M - 2) {
            for (j in 0 until N - 2) {
                b += w[i][j] * FD[i][j] *
                        (SDouble[i + 1][j + 1] - Sbar[i + 1][j + 1])
            }
        }
        betaHat += betaHat * b
    }

    return betaHat
}

private fun localVar(X: Array<Array<Double>>, K: Int): Array<DoubleArray> {
    var kernelSize = K
    if (kernelSize % 2 == 0) kernelSize++
    val half = kernelSize / 2

    val M = X.size
    val N = if (M > 0) X[0].size else 0
    val result = Array(M) { DoubleArray(N) }

    for (i in 0 until M) {
        for (j in 0 until N) {
            var sum = 0.0
            var sumSq = 0.0
            var count = 0

            for (di in -half..half) {
                for (dj in -half..half) {
                    val ni = i + di
                    val nj = j + dj
                    if (ni in 0 until M && nj in 0 until N) {
                        val value = X[ni][nj]
                        sum += value
                        sumSq += value * value
                        count++
                    }
                }
            }

            result[i][j] = if (count > 0) {
                sumSq / count - (sum / count).pow(2)
            } else {
                0.0
            }
        }
    }

    return result
}