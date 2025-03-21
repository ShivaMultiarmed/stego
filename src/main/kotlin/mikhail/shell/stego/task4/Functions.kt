package mikhail.shell.stego.task4

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import java.io.File
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

fun ByteArray.arrange(width: Int, height: Int): Array<Array<Int>> {
    return Array(height) { i ->
        Array(width) { j ->
            this[i * width + j].toInt() and 0xFF
        }
    }
}

fun Array<Array<Int>>.chunk(): Array<Array<Array<Array<Int>>>> {
    return Array(this.size / 2) { i ->
        Array(this[0].size / 2) { j ->
            val m00 = this[2 * i][2 * j]
            val m01 = this[2 * i][2 * j + 1]
            val m10 = this[2 * i + 1][2 * j]
            val m11 = this[2 * i + 1][2 * j + 1]
            arrayOf(
                arrayOf(m00, m01),
                arrayOf(m10, m11)
            )
        }
    }
}

fun Int.toByteArray(): ByteArray {
    return ByteArray(4) { i ->
        (this shr ((3 - i) * 8) and 0xFF).toByte()
    }
}

fun Int.getN() = if (this != 0) log2(abs(this).toFloat()).toInt() else 0

fun BufferedImage.interpolate(): BufferedImage {
    val K = 2
    val inputBuffer = raster.dataBuffer as DataBufferByte
    val bytes = inputBuffer.data
    val initialBytes = bytes.arrange(width, height)
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
    val resourcesPath = "src/main/resources/"
    val dataLength = data.size.toByteArray()
    val inputBuffer = (raster.dataBuffer as DataBufferByte).data
    val formattedInput = inputBuffer.arrange(width, height)
    var bitNum = 0
    val bits = (dataLength + data).decompose().toTypedArray()
    val blocks = formattedInput.chunk()
    File(resourcesPath,"before.txt").printWriter().use {
        val content = blocks.print()
        it.print(content)
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
            block
        }
    }
    File(resourcesPath,"after.txt").printWriter().use {
        val content = blocks.print()
        it.print(content)
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

fun BufferedImage.extractData(): ByteArray {
    val K = 2
    val bits = mutableListOf<Byte>()
    val arrangedInput = (raster.dataBuffer as DataBufferByte).data
        .arrange(width, height)
    var bitNum = 0
    var dataLength: Int? = null
    val blocks = arrangedInput.chunk()
    for (i in 0 until blocks.size - 1) {
        for (j in 0 until blocks[0].size - 1) {
            val block = blocks[i][j]
            val rightBlock = blocks[i][j + 1]
            val bottomBlock = blocks[i + 1][j]
//            if (dataLength == null && bitNum >= 8 * 4) {
//                dataLength = bits.subList(0, 8 * 4).map { it.toString() }.joinToString("").toInt(2)
//                bits.subList(0, dataLength).clear()
//                bitNum -= dataLength
//            }
            for (r in 0..1) {
                for (c in 0..1) {
                    if (r == 0 && c == 0) {
                        continue
                    }
                    val b = when {
                        r == 1 && c == 1 -> {
                            (block[0][0] - (K * block[0][0] + bottomBlock[0][0].toFloat() / K + rightBlock[0][0].toFloat() / K) / (K + 1)).toInt()
                        }
                        r == 0 -> {
                            (block[0][1] - (block[0][0] + rightBlock[0][0]).toFloat() / K).toInt()
                        }
                        else -> { // c == 0
                            (block[1][0] - (block[0][0] + bottomBlock[0][0]).toFloat() / K).toInt()
                        }
                    }.let { abs(it) }
                    val bitPart = Integer.toBinaryString(b).asSequence().toList().map { it.digitToInt().toByte() }
                    bitNum += bitPart.size
                    bits.addAll(bitPart)
                }
            }
        }
    }
    return bits.compose()
}


fun Array<Array<Array<Array<Int>>>>.print(): String {
    val builder = StringBuilder()
    for (i in this.indices) {
        for (j in this[i].indices) {
            builder.append(j + 1).append("\t\t\t")
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