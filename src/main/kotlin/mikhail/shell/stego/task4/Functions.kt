package mikhail.shell.stego.task4

import mikhail.shell.stego.task7.*
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import kotlin.math.*

operator fun Byte.get(index: Int): Byte {
    return ((this.toInt() shr (7 - index)) and 1).toByte()
}

fun ByteArray.getBit(bitNumber: Int): Byte {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    return this[byteNumber][specificBitNumber]
}

fun List<Byte>.compose(): ByteArray {
    return ByteArray(this.size / 8) { i ->
        this.subList(i * 8, (i + 1) * 8).toTypedArray().unite()
    }
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

fun ByteArray.arrange(width: Int, height: Int): Array<Array<Float>> {
    return Array(height) { i ->
        Array(width) { j ->
            (this[i * width + j].toInt() and 0xFF).toFloat()
        }
    }
}

fun Array<Array<Float>>.chunk(side: Int = 2): Array<Array<Array<Array<Float>>>> {
    return Array(this.size / side) { i ->
        Array(this[0].size / side) { j ->
            Array(side) { m ->
                Array(side) { n ->
                    this[i * side + m][j * side + n]
                }
            }
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
    val initialBytes = bytes.arrange(width, height)
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

fun BufferedImage.insertData(data: ByteArray): BufferedImage {
    val dataLength = data.size.toByteArray()
    val inputBuffer = (raster.dataBuffer as DataBufferByte).data
    val formattedInput = inputBuffer.arrange(width, height)
    var bitNum = 0
    var bits = (dataLength + data).decompose().toTypedArray()
    bits = bits.toList().windowed(
        size = 4,
        step = 4,
        partialWindows = true
    ) {
        hash(it.toTypedArray()).toList()
    }.flatten().toTypedArray()
//    val encodedBits = bits.toList().windowed(
//        size = 4,
//        step = 1,
//        partialWindows = true
//    ) {
//        encode(it.toTypedArray())
//    }
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
                        val b = slice.unite().toInt()
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

fun BufferedImage.extractData(): ByteArray {
    val K = 2
    var bits = mutableListOf<Byte>()
    val arrangedInput = (raster.dataBuffer as DataBufferByte).data
        .arrange(width, height)
    var bitNum = 0
    var dataLength: Int? = null
    val blocks = arrangedInput.chunk(side = 2)
    outer@ for (i in 1 until blocks.size - 1) {
        for (j in 1 until blocks[0].size - 1) {
            val block = blocks[i][j]
            val right = blocks[i][j + 1]
            val bottom = blocks[i + 1][j]
            if (dataLength == null && bitNum >= 8 * 4) {
                val dataLengthBits = bits.subList(0, 8 * 4).windowed(
                    size = 4,
                    step = 4,
                    partialWindows = false
                ) {
                    unhash(it.toTypedArray()).toList()
                }.flatten().toMutableList()
                dataLength = dataLengthBits.joinToString("").toInt(2)
                bits.subList(0, 4 * 8).clear()
                bitNum -= 4 * 8
            }
            if (dataLength != null) {
                if (bitNum >= dataLength * 8) {
                    break@outer
                }
            }
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
                        .let { List(n - it.size) { 0.toByte() } + it }
                    bitNum += bitPart.size
                    bits.addAll(bitPart)
                }
            }
        }
    }
    bits = bits.subList(0, dataLength!! * 8)
    bits = bits.toList().windowed(
        size = 4,
        step = 4,
        partialWindows = true
    ) {
        unhash(it.toTypedArray()).toList()
    }.flatten().toMutableList()
    return bits.compose()
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