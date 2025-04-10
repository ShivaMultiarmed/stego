package mikhail.shell.stego.task5.aump

import kotlin.math.abs

fun triples(X: Array<Array<Int>>): Double {
    val M = X.size
    val N = if (M > 0) X[0].size else 0
    val LCR = prepareTriples(X, M, N)
    val (T1, T2, L) = LCR

    val e = Array(25) { IntArray(25) }
    val o = Array(25) { IntArray(25) }

    // Заполнение матриц e и o
    for (m1 in -11..13) {
        for (m2 in -11..13) {
            val indices = T1.indices.filter { i -> T1[i] == m1 && T2[i] == m2 }
            val sumEven = indices.sumOf { i -> 1 - (L[i] % 2) }
            val sumOdd = indices.sumOf { i -> L[i] % 2 }
            val idx1 = m1 + 12
            val idx2 = m2 + 12
            if (idx1 in 0..24 && idx2 in 0..24) {
                e[idx1][idx2] = sumEven
                o[idx1][idx2] = sumOdd
            }
        }
    }

    // Вычисление d0-d3
    val c0 = calculateCoefficients(e, o, 0)
    val c1 = calculateCoefficients(e, o, 1)
    val c2 = calculateCoefficients(e, o, 2)
    val c3 = calculateCoefficients(e, o, 3)

    // Решение квинтического уравнения
    var betaHat = -1.0
    val epsilon = 1e-9
    var left = 0.6
    var right = 7.0
    var fLeft = quintic(left, c0, c1, c2, c3)
    var fRight = quintic(right, c0, c1, c2, c3)

    if (fLeft * fRight <= 0) {
        while (abs(right - left) > epsilon) {
            val mid = (left + right) / 2
            val fMid = quintic(mid, c0, c1, c2, c3)
            if (fMid * fLeft <= 0) {
                right = mid
                fRight = fMid
            } else {
                left = mid
                fLeft = fMid
            }
        }
        val qHat = (left + right) / 2
        betaHat = 0.5 * (1 - 1 / qHat)
    }

    return betaHat
}

private fun prepareTriples(
    X: Array<Array<Int>>,
    M: Int,
    N: Int
): Triple<List<Int>, List<Int>, List<Int>> {
    val L = mutableListOf<Int>()
    val C = mutableListOf<Int>()
    val R = mutableListOf<Int>()

    for (i in 0 until M) {
        for (j in 0 until N - 2) {
            L.add(X[i][j])
            C.add(X[i][j + 1])
            R.add(X[i][j + 2])
        }
    }

    val T1 = C.zip(L).map { (c, l) -> c - l }
    val T2 = R.zip(C).map { (r, c) -> r - c }
    return Triple(T1, T2, L)
}

private fun calculateCoefficients(
    e: Array<IntArray>,
    o: Array<IntArray>,
    mode: Int
): Array<DoubleArray> {
    val coeff = Array(11) { DoubleArray(11) }
    for (m in -5..5) {
        for (n in -5..5) {
            // Расчет d0
            val d0 = e[2*m + 1 + 12][2*n + 1 + 12] - o[2*m + 1 + 12][2*n + 1 + 12].toDouble()

            // Расчет d1 (полная версия из MATLAB)
            val d1 = e[2*m + 1 + 12][2*n + 2 + 12] +
                    e[2*m + 12][2*n + 2 + 12] +
                    o[2*m + 12][2*n + 1 + 12] -
                    o[2*m + 1 + 12][2*n + 12] -
                    o[2*m + 2 + 12][2*n + 12] -
                    e[2*m + 2 + 12][2*n + 1 + 12].toDouble()

            // Расчет d2
            val d2 = e[2*m + 12][2*n + 3 + 12] +
                    o[2*m - 1 + 12][2*n + 2 + 12] +
                    o[2*m + 12][2*n + 2 + 12] -
                    o[2*m + 2 + 12][2*n - 1 + 12] -
                    e[2*m + 2 + 12][2*n + 12] -
                    e[2*m + 3 + 12][2*n + 12].toDouble()

            // Расчет d3
            val d3 = o[2*m - 1 + 12][2*n + 3 + 12] -
                    e[2*m + 3 + 12][2*n - 1 + 12].toDouble()

            coeff[m + 5][n + 5] = when (mode) {
                0 -> d0 + d1 + d2 + d3
                1 -> 3 * d0 + d1 - d2 - 3 * d3
                2 -> 3 * d0 - d1 - d2 + 3 * d3
                3 -> d0 - d1 + d2 - d3
                else -> 0.0
            }
        }
    }
    return coeff
}

private fun quintic(q: Double, vararg coeffs: Array<DoubleArray>): Double {
    var y = 0.0
    for (m in 0..10) {
        for (n in 0..10) {
            val c0 = coeffs[0][m][n]
            val c1 = coeffs[1][m][n]
            val c2 = coeffs[2][m][n]
            val c3 = coeffs[3][m][n]

            y += 2 * c0 * c1 +
                    q * (4 * c0 * c2 + 2 * c1 * c1) +
                    q * q * (6 * c0 * c3 + 6 * c1 * c2) +
                    q * q * q * (4 * c2 * c2 + 8 * c1 * c3) +
                    q * q * q * q * 10 * c2 * c3 +
                    q * q * q * q * q * 6 * c3 * c3
        }
    }
    return y
}