package mikhail.shell.stego.task4

import mikhail.shell.stego.common.*
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import kotlin.math.*

fun Array<Byte>.arrange(width: Int, height: Int): Array<Array<Float>> {
    return Array(height) { i ->
        Array(width) { j ->
            (this[i * width + j].toInt() and 0xFF).toFloat()
        }
    }
}

fun Int.toByteArray(): ByteArray {
    return ByteArray(4) { i ->
        (this shr ((3 - i) * 8) and 0xFF).toByte()
    }
}

fun Float.getN() = if (this != 0f) log2(abs(this)).toInt() else 0

fun BufferedImage.interpolate(): BufferedImage {
    val K = 2
    val inputBuffer = raster.dataBuffer as DataBufferByte
    val bytes = inputBuffer.data
    val initialBytes = bytes.toTypedArray().arrange(width, height)
    val newWidth = width * K
    val newHeight = height * K
    val newBytes = Array(newHeight) { Array(newWidth) { 0f } }
    for (m in 0 until height) {
        for (n in 0 until width) {
            newBytes[K * m][K * n] = initialBytes[m][n]
        }
    }
    for (m in 0 until height) {
        for (n in 0 until width) {
            val left = initialBytes[m][n]
            if (n < width - 1) {
                val right = initialBytes[m][n + 1]
                newBytes[m * K][n * K + 1] = (left + right) / 2
            } else {
                newBytes[m * K][n * K + 1] = left
            }
        }
    }
    for (m in 0 until height) {
        for (n in 0 until width) {
            val top = initialBytes[m][n]
            if (m < height - 1) {
                val bottom = initialBytes[m + 1][n]
                newBytes[m * K + 1][n * K] = (top + bottom) / 2
            } else {
                newBytes[m * K + 1][n * K] = top
            }
        }
    }
    for (m in 0 until height) {
        for (n in 0 until width) {
            newBytes[m * K + 1][n * K + 1] =
                (
                        initialBytes[m][n]
                                + newBytes[K * m][K * n + 1]
                                + newBytes[K * m + 1][K * n]
                        ) / 3
        }
    }
    val output = BufferedImage(newWidth, newHeight, TYPE_BYTE_GRAY)
    val outputBuffer = output.raster.dataBuffer as DataBufferByte
    outputBuffer.data.forEachIndexed { i, _ ->
        outputBuffer.data[i] = floor(newBytes[i / newWidth][i % newWidth])
            .toInt()
            .coerceIn(0, 255)
            .toByte()
    }
    return output
}

fun encode(bits: Array<Byte>): Array<Byte> {
    val parityMatrix = arrayOf(
        byteArrayOf(1, 1, 1, 0, 1, 0, 0),
        byteArrayOf(1, 0, 0, 1, 0, 1, 0),
        byteArrayOf(0, 1, 0, 1, 0, 0, 1)
    ).map { it.toTypedArray() }.toTypedArray()
    return encode(parityMatrix, bits)
}

fun decode(bits: Array<Byte>): Array<Byte> {
    return decode(4, bits)
}

fun hash(bits: Array<Byte>): Array<Byte> {
    val hashMatrix = arrayOf(
        byteArrayOf(1, 0, 1, 1),
        byteArrayOf(0, 1, 1, 0),
        byteArrayOf(0, 0, 1, 1),
        byteArrayOf(1, 1, 1, 1)
    ).map { it.toTypedArray() }.toTypedArray()
    return hash(hashMatrix, bits)
}

fun unhash(bits: Array<Byte>): Array<Byte> {
    val unhashMatrix = arrayOf(
        byteArrayOf(1, 0, 1, 0),
        byteArrayOf(1, 0, 0, 1),
        byteArrayOf(1, 1, 0, 1),
        byteArrayOf(1, 1, 1, 1)
    ).map { it.toTypedArray() }.toTypedArray()
    return hash(unhashMatrix, bits)
}

fun BufferedImage.insertData(data: Array<Byte>): BufferedImage {
    val inputBuffer = (raster.dataBuffer as DataBufferByte).data
    val formattedInput = inputBuffer.toTypedArray().arrange(width, height)
    var bitNum = 0
    var bits = data.decompose()
    bits = pack(bits)
    bits = bits.toList().windowed(
        size = 4,
        step = 4,
        partialWindows = true
    ) {
        hash(it.toTypedArray()).toList()
    }.flatten().toTypedArray()
    bits = bits.toList().windowed(
        size = 4,
        step = 4,
        partialWindows = false
    ) {
        encode(it.toTypedArray()).toList()
    }.flatten().toTypedArray()
    val blocks = formattedInput.chunk(side = 2)
    for (i in 1 until blocks.size - 1) {
        for (j in 1 until blocks[0].size - 1) {
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
                        val b = slice.implode().toInt()
                        bitNum += n
                        block[r][c] = block[r][c] + b
                    }
                }
            }
        }
    }

    val newImageData = Array(height) { Array(width) { 0f } }
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
            outputBuffer[row * width + col] = floor(newImageData[row][col])
                .toInt()
                .coerceIn(0,255)
                .toByte()
        }
    }
    return outputImage
}

fun BufferedImage.extractData(): Array<Byte> {
    val K = 2
    var bits = mutableListOf<Byte>()
    val arrangedInput = (raster.dataBuffer as DataBufferByte).data
        .toTypedArray()
        .arrange(width, height)
    val blocks = arrangedInput.chunk(side = 2)
    outer@ for (i in 1 until blocks.size - 1) {
        for (j in 1 until blocks[0].size - 1) {
            val block = blocks[i][j]
            val right = blocks[i][j + 1]
            val bottom = blocks[i + 1][j]
            for (r in 0..1) {
                for (c in 0..1) {
                    if (r == 0 && c == 0) {
                        continue
                    }
                    val initialByte = when {
                        r == 1 && c == 1 -> (K * block[0][0] + (bottom[0][0] + right[0][0]) / K.toFloat()) / (K + 1)
                        r == 0 -> (block[0][0] + right[0][0]) / K
                        else -> (block[0][0] + bottom[0][0]) / K
                    }.let { floor(it) }
                    val b = (block[r][c] - initialByte).toInt()
                    val d = initialByte - block[0][0]
                    val n = d.getN()
                    if (n == 0) {
                        continue
                    }
                    val bitPart = Integer.toBinaryString(b)
                        .asSequence().toList()
                        .map { it.digitToInt().toByte() }
                        .let {
                            try {
                                List(n - it.size) { 0.toByte() } + it
                            } catch (_: IllegalArgumentException) {
                                List(n) { 0.toByte() }
                            }
                        }
                    bits.addAll(bitPart)
                }
            }
        }
    }
    bits = bits.toList().windowed(
        size = 7,
        step = 7,
        partialWindows = false
    ) {
        decode(it.toTypedArray()).toList()
    }.flatten().toMutableList()
    bits = bits.toList().windowed(
        size = 4,
        step = 4,
        partialWindows = false
    ) {
        unhash(it.toTypedArray()).toList()
    }.flatten().toMutableList()
    bits = unpack(bits.toTypedArray()).toMutableList()
    return bits.toTypedArray().compose()
}


fun Array<Array<Array<Array<Float>>>>.print(): String {
    val builder = StringBuilder()
    for (i in this.indices) {
        for (j in this[i].indices) {
            builder.append(j).append("\t\t\t\t\t")
        }
        builder.append("\n")
        for (j in this[i].indices) {
            val block = this[i][j]
            builder.append(block[0][0].toString() + "\t")
            builder.append(block[0][1].toString() + "\t")
            builder.append("|\t")
        }
        builder.append("\n")
        for (j in this[i].indices) {
            val block = this[i][j]
            builder.append(block[1][0].toString() + "\t")
            builder.append(block[1][1].toString() + "\t")
            builder.append("|\t")
        }
        builder.append("\n")
        for (j in this[i].indices) {
            builder.append("----")
        }
        builder.append("\n")
    }
    return builder.toString()
}

fun evaluateMSE(
    image1: BufferedImage,
    image2: BufferedImage
): Float {
    val input1 = (image1.raster.dataBuffer as DataBufferByte).data
    val input2 = (image2.raster.dataBuffer as DataBufferByte).data

    var mse = 0f

    for (i in input1.indices) {
        mse += (input1[i] - input2[i]).toDouble().pow(2.0).toFloat()
    }

    mse /= input1.size

    return mse
}

fun evaluatePSNR(max: Float, mse: Float): Float {
    return 10 * log10(max.pow(2) / mse)
}