package mikhail.shell.stego.task4

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import kotlin.math.abs
import kotlin.math.log2

operator fun Byte.get(index: Int): Byte {
    return ((this.toInt() shr (7 - index)) and 1).toByte()
}

fun ByteArray.getBit(bitNumber: Int): Byte {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    return this[byteNumber][specificBitNumber]
}

fun ByteArray.decompose(): ByteArray {
    return ByteArray(this.size * 8) { i ->
        this.getBit(i)
    }
}

fun Array<Byte>.unite(): Byte {
    var result = 0
    for (x in this) {
        result = (result shl 1) or x.toInt()
    }
    return result.toByte()
}

fun BufferedImage.interpolate(): BufferedImage {
    val K = 2
    val inputBuffer = raster.dataBuffer as DataBufferByte
    val bytes = inputBuffer.data
    val initialBytes = Array(height) { i ->
        Array(width) { j ->
            bytes[i * width + j].toInt() and 0xFF
        }
    }
    val newWidth = width * K
    val newHeight = height * K
    val newBytes = Array(newHeight) { Array(newWidth) { 0 } }
    for (i in 0 until newHeight) {
        for (j in 0 until newWidth) {
            val m = i / K
            val n = j / K
            if (i % K == 0 && j % K == 0) {
                newBytes[i][j] = initialBytes[m][n]
            } else if (i % K == 0) {
                newBytes[i][j] = when {
                    (n in 1..width - 2) -> (initialBytes[m][n - 1] + initialBytes[m][n + 1]) / K
                    (n >= 1) -> initialBytes[m][n - 1]
                    (n <= width - 2) -> initialBytes[m][n + 1]
                    else -> 0
                }
            } else if (j % K == 0) {
                newBytes[i][j] = when {
                    (m in 1..height - 2) -> (initialBytes[m - 1][n] + initialBytes[m + 1][n]) / K
                    (m >= 1) -> initialBytes[m - 1][n]
                    (m <= width - 2) -> initialBytes[m + 1][n]
                    else -> 0
                }
            } else {
                var sum = newBytes[i - 1][j] + newBytes[i][j - 1]
                var count = 2
                if (m > 0 && n > 0) {
                    sum += initialBytes[m - 1][n - 1]
                    count++
                }
                newBytes[i][j] = sum / count
            }
        }
    }
    val output = BufferedImage(newWidth, newHeight, TYPE_BYTE_GRAY)
    val outputBuffer = output.raster.dataBuffer as DataBufferByte
    outputBuffer.data.forEachIndexed { i, _ ->
        outputBuffer.data[i] = newBytes[i / newWidth][i % newWidth].toByte()
    }
    return output
}

fun BufferedImage.insertData(data: ByteArray): BufferedImage {
    val inputBuffer = (raster.dataBuffer as DataBufferByte).data
    val formattedInput = Array(height) { row ->
        Array(width) { col ->
            inputBuffer[row * width + col].toInt() and 0xFF
        }
    }
    var bitNum = 0
    val bits = data.decompose().toTypedArray()
    val blocks = Array(height / 2) { i ->
        Array(width / 2) { j ->
            val m00 = formattedInput[2 * i][2 * j]
            val m01 = formattedInput[2 * i][2 * j + 1]
            val m10 = formattedInput[2 * i + 1][2 * j]
            val m11 = formattedInput[2 * i + 1][2 * j + 1]
            arrayOf(
                arrayOf(m00, m01),
                arrayOf(m10, m11)
            )
        }
    }
    for (i in blocks.indices) {
        for (j in blocks[0].indices) {
            val block = blocks[i][j]
            val m00 = block[0][0]
            for (r in 0..1) {
                for (c in 0..1) {
                    if (r == 0 && c == 0) {
                        continue
                    }
                    val d = block[r][c] - m00
                    val n = d.getN()
                    if (n > 0 && bitNum < bits.size) {
                        val end = (bitNum + n).coerceAtMost(bits.size)
                        val slice = bits.sliceArray(bitNum until end)
                        val b = slice.unite().toInt()
                        bitNum += n
                        block[r][c] = block[r][c] + b
                    }
                }
            }
        }
    }
    val newImageData = Array(height) { Array(width) { 0 } }
    for (i in 0 until height / 2) {
        for (j in 0 until width / 2) {
            val block = blocks[i][j]
            newImageData[2 * i][2 * j] = block[0][0]
            newImageData[2 * i][2 * j + 1] = block[0][1]
            newImageData[2 * i + 1][2 * j] = block[1][0]
            newImageData[2 * i + 1][2 * j + 1] = block[1][1]
        }
    }
    val outputImage = BufferedImage(width, height, TYPE_BYTE_GRAY)
    val outputBuffer = (outputImage.raster.dataBuffer as DataBufferByte).data
    for (row in 0 until height) {
        for (col in 0 until width) {
            outputBuffer[row * width + col] = newImageData[row][col].toByte()
        }
    }
    return outputImage
}

inline fun <reified T> Array<Array<T>>.flatten(): Array<T> {
    val rows = this.size
    val cols = this[0].size
    return Array( rows * cols) {
        this[it / cols][it % cols]
    }
}

inline fun <reified A, reified B> Array<Array<A>>.transform(transformation: (x: A) -> B): Array<Array<B>> {
    return this.map { row ->
        row.map { initial ->
            transformation(initial)
        }.toTypedArray()
    }.toTypedArray()
}

inline fun <reified A, reified B> Array<Array<A>>.transformIndexed(transformation: (i: Int, j: Int, x: A) -> B): Array<Array<B>> {
    return this.mapIndexed { i, row ->
        row.mapIndexed { j, initial ->
            transformation(i, j, initial)
        }.toTypedArray()
    }.toTypedArray()
}

fun Int.getN() = if (this != 0) log2(abs(this).toFloat()).toInt() else 0