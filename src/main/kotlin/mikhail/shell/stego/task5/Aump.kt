package mikhail.shell.stego.task5

import org.apache.commons.math3.linear.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.math.pow
import kotlin.math.sqrt


fun aump(image: BufferedImage, m: Int = 4, d: Int = 1): Double {
    val pixelMatrix = (image.raster.dataBuffer as DataBufferByte).data
        .map { it.toInt() and 0xFF }
        .toIntArray()
        .to2DArray(image.height, image.width)
    return aump(pixelMatrix, m, d)
}

fun aump(X: Array<Array<Int>>, m: Int, d: Int): Double {
    val XDouble = X.map { row -> row.map { it.toDouble() }.toTypedArray() }.toTypedArray()
    val (Xpred, S, w) = Pred_aump(XDouble, m, d)

    // Calculate Xbar with flipped LSBs
    val Xbar = XDouble.map { row ->
        row.map { pixel ->
            pixel + 1 - 2 * (pixel % 2)
        }.toTypedArray()
    }.toTypedArray()

    // Compute beta statistic
    var beta = 0.0
    for (i in X.indices) {
        for (j in X[i].indices) {
            beta += w[i][j] * (XDouble[i][j] - Xbar[i][j]) * (XDouble[i][j] - Xpred[i][j])
        }
    }

    return beta
}

private fun Pred_aump(
    X: Array<Array<Double>>,
    m: Int,
    d: Int
): Triple<Array<Array<Double>>, Array<Array<Double>>, Array<Array<Double>>> {
    val q = d + 1
    val rows = X.size
    val cols = X[0].size
    require(cols % m == 0) { "m must divide the number of columns" }

    // Create Vandermonde matrix H
    val H = Array(m) { DoubleArray(q) }
    val xNorm = (1..m).map { it.toDouble() / m }
    for (i in 0 until m) {
        for (j in 0 until q) {
            H[i][j] = xNorm[i].pow(j)
        }
    }

    // Create Y matrix with pixel blocks
    val Kn = cols / m * rows
    val Y = Array(m) { DoubleArray(Kn) }
    var blockIdx = 0
    for (row in X.indices) {
        for (startCol in 0 until cols step m) {
            for (i in 0 until m) {
                Y[i][blockIdx] = X[row][startCol + i]
            }
            blockIdx++
        }
    }

    // Solve least squares problem
    val solver = SingularValueDecomposition(Array2DRowRealMatrix(H.to2DArray())).solver
    val p = solver.solve(Array2DRowRealMatrix(Y.to2DArray())).data

    // Compute predicted Y
    val Ypred = Array(m) { DoubleArray(Kn) }
    for (i in 0 until m) {
        for (k in 0 until Kn) {
            Ypred[i][k] = H[i].indices.sumOf { j -> H[i][j] * p[j][k] }
        }
    }

    // Reconstruct Xpred
    val Xpred = Array(rows) { Array(cols) { 0.0 } }
    blockIdx = 0
    for (row in X.indices) {
        for (startCol in 0 until cols step m) {
            for (i in 0 until m) {
                Xpred[row][startCol + i] = Ypred[i][blockIdx]
            }
            blockIdx++
        }
    }

    // Calculate variances
    val sigTh = 1.0
    val sig2 = DoubleArray(Kn)
    for (k in 0 until Kn) {
        var sum = 0.0
        for (i in 0 until m) {
            sum += (Y[i][k] - Ypred[i][k]).pow(2)
        }
        sig2[k] = sum / (m - q)
        sig2[k] = maxOf(sigTh.pow(2), sig2[k])
    }

    // Create S matrix
    val S = Array(rows) { Array(cols) { 0.0 } }
    blockIdx = 0
    for (row in X.indices) {
        for (startCol in 0 until cols step m) {
            for (i in 0 until m) {
                S[row][startCol + i] = sig2[blockIdx]
            }
            blockIdx++
        }
    }

    // Calculate weights
    val sumInvSig2 = sig2.sumOf { 1.0 / it }
    val sN2 = Kn / sumInvSig2
    val w = Array(rows) { Array(cols) { 0.0 } }
    blockIdx = 0
    for (row in X.indices) {
        for (startCol in 0 until cols step m) {
            val weight = sqrt(sN2 / (Kn * (m - q))) / sig2[blockIdx]
            for (i in 0 until m) {
                w[row][startCol + i] = weight
            }
            blockIdx++
        }
    }

    return Triple(Xpred, S, w)
}

// Helper extensions
private fun Array<DoubleArray>.to2DArray(): Array<DoubleArray> = this
private fun RealMatrix.to2DArray(): Array<DoubleArray> =
    Array(rowDimension) { i -> getRow(i) }

fun IntArray.to2DArray(rows: Int, cols: Int): Array<Array<Int>> {
    require(rows * cols == size) { "Dimensions don't match array size" }
    return Array(rows) { i ->
        Array(cols) { j -> this[i * cols + j] }
    }
}
