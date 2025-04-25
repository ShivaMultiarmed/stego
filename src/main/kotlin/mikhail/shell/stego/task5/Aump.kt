package mikhail.shell.stego.task5

import org.apache.commons.math3.linear.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


fun aump(image: BufferedImage, m: Int = 16, d: Int = 2): Double {
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
            beta += w[i][j] * (XDouble[i][j] - Xbar[i][j]) * (Xpred[i][j] - XDouble[i][j])
        }
    }

    return abs(beta)
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

    // 1. Формируем матрицу H (как в MATLAB)
    val H = Array(m) { DoubleArray(q) }
    val xNorm = (1..m).map { it.toDouble() / m }
    for (i in 0 until m) {
        for (j in 0 until q) {
            H[i][j] = xNorm[i].pow(j)
        }
    }

    // 2. Формируем Y правильно (по столбцам, как в MATLAB)
    val blocksPerCol = cols / m
    val Kn = rows * blocksPerCol
    val Y = Array(m) { DoubleArray(Kn) }

    for (i in 0 until m) {
        var blockIdx = 0
        for (block in 0 until blocksPerCol) {
            val currentCol = i + block * m // Столбцы: i, i+m, i+2m...
            for (row in 0 until rows) {
                Y[i][blockIdx] = X[row][currentCol]
                blockIdx++
            }
        }
    }

    // 3. Решаем СЛАУ
    val solver = SingularValueDecomposition(Array2DRowRealMatrix(H.to2DArray())).solver
    val p = solver.solve(Array2DRowRealMatrix(Y.to2DArray())).data

    // 4. Предсказание Ypred
    val Ypred = Array(m) { DoubleArray(Kn) }
    for (i in 0 until m) {
        for (k in 0 until Kn) {
            Ypred[i][k] = H[i].indices.sumOf { j -> H[i][j] * p[j][k] }
        }
    }

    // 5. Восстанавливаем Xpred (правильное отображение блоков)
    val Xpred = Array(rows) { Array(cols) { 0.0 } }
    for (i in 0 until m) {
        var blockIdx = 0
        for (block in 0 until blocksPerCol) {
            val currentCol = i + block * m
            for (row in 0 until rows) {
                Xpred[row][currentCol] = Ypred[i][blockIdx]
                blockIdx++
            }
        }
    }

    // 6. Вычисляем дисперсии sig2
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

    // 7. Формируем матрицу S
    val S = Array(rows) { Array(cols) { 0.0 } }
    for (i in 0 until m) {
        var blockIdx = 0
        for (block in 0 until blocksPerCol) {
            val currentCol = i + block * m
            for (row in 0 until rows) {
                S[row][currentCol] = sig2[blockIdx]
                blockIdx++
            }
        }
    }

    // 8. Вычисляем веса w
    val sumInv = sig2.sumOf { 1.0 / it }
    val sN2 = Kn / sumInv
    val common = sqrt(sN2 / (Kn * (m - q)))
    val w = Array(rows) { Array(cols) { 0.0 } }
    for (i in 0 until m) {
        var blockIdx = 0
        for (block in 0 until blocksPerCol) {
            val weight = common / sig2[blockIdx]
            val currentCol = i + block * m
            for (row in 0 until rows) {
                w[row][currentCol] = weight
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
