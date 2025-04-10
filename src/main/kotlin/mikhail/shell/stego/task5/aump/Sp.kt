package mikhail.shell.stego.task5.aump

import kotlin.math.floor
import kotlin.math.sqrt

fun sp(X: Array<Array<Int>>): Double {
    val xDouble = X.map { row -> row.map { it.toDouble() } }
    val M = X.size
    val N = if (M > 0) X[0].size else 0
    val totalPairs = M * (N - 1)

    val pairs = xDouble.flatMap { row ->
        val u = row.dropLast(1)
        val v = row.drop(1)
        u.zip(v)
    }

    var Xc = 0
    var Zc = 0
    var Wc = 0

    for ((u, v) in pairs) {
        // Count Xc
        if ((v % 2.0 == 0.0 && u < v) || (v % 2.0 == 1.0 && u > v)) {
            Xc++
        }

        // Count Zc
        if (u == v) {
            Zc++
        }

        // Count Wc
        val uFloor = floor(u / 2.0)
        val vFloor = floor(v / 2.0)
        if (uFloor == vFloor && u != v) {
            Wc++
        }
    }

    val Vc = totalPairs - (Xc + Zc + Wc)

    val a = (Wc + Zc) / 2.0
    val b = 2.0 * Xc - totalPairs
    val c = Vc + Wc - Xc.toDouble()

    val betaHat = if (a > 0) {
        val discriminant = b * b - 4 * a * c
        if (discriminant >= 0) {
            val sqrtD = sqrt(discriminant)
            val p1 = (-b + sqrtD) / (2 * a)
            val p2 = (-b - sqrtD) / (2 * a)
            minOf(p1, p2)
        } else {
            // При отрицательном дискриминанте берём вещественную часть корней
            -b / (2 * a)
        }
    } else {
        -1.0
    }

    return betaHat
}