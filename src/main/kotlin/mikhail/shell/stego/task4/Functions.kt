package mikhail.shell.stego.task4

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import kotlin.math.log2

operator fun Byte.get(index: Int): Byte {
    return ((this.toInt() shr (7 - index)) and 1).toByte()
}

fun ByteArray.getBit(bitNumber: Int): Byte {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    val byte = (this[byteNumber].toInt() and 0xFF).toByte()
    return byte[specificBitNumber]
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

fun BufferedImage.insertData(data: ByteArray) {
    val dataBuffer = raster.dataBuffer as DataBufferByte
    val inputData = dataBuffer.data
    val formattedInputData = inputData.map { it.toInt() and 0xFF }.toTypedArray()
    var bitNum = 0
    val bits = data.decompose().toTypedArray()
    val partitionedData = Array(height / 2) { i ->
        Array(width / 2) { j ->
            val m00 = formattedInputData[2 * i * width + 2 * j]
            val m01 = formattedInputData[2 * i * width + 2 * j + 1]
            val m10 = formattedInputData[(2 * i + 1) * width + 2 * j]
            val m11 = formattedInputData[(2 * i + 1) * width + 2 * j + 1]
            val block = arrayOf(
                arrayOf(m00, m01),
                arrayOf(m10, m11)
            )
            val d = block.transform { it - m00 }
            val n = d.transform { it.getN() }
            val b = block.transformIndexed { i, j, _ ->
                if (n[i][j] == 0) { 0 } else {
                    bits
                        .sliceArray(bitNum..<bitNum + n[i][j])
                        .unite().toInt()
                        .also { bitNum += n[i][j] }
                }
            }
            block.transformIndexed { i, j, x ->
                x + b[i][j]
            }
        }
    }
}

fun Array<Array<Int>>.transform(transformation: (x: Int) -> Int): Array<Array<Int>> {
    return this.map { row ->
        row.map { initial ->
            transformation(initial)
        }.toTypedArray()
    }.toTypedArray()
}

fun Array<Array<Int>>.transformIndexed(transformation: (i: Int, j: Int, x: Int) -> Int): Array<Array<Int>> {
    return this.mapIndexed { i, row ->
        row.mapIndexed { j, initial ->
            transformation(i, j, initial)
        }.toTypedArray()
    }.toTypedArray()
}

fun Int.getN() = if (this != 0) log2(this.toFloat()).toInt() else 0