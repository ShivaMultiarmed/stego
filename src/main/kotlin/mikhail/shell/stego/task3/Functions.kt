package mikhail.shell.stego.task3

import java.io.File
import java.nio.ByteBuffer
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.pow

val offset = 200

// Преобразует массив из байтов в массив из пар битов
fun ByteArray.decompose(): Array<Array<Int>> {
    val totalPairs = this.size * 8 / 2
    return Array(totalPairs) { i ->
        arrayOf(this.getBit(2 * i), this.getBit(2 * i + 1))
    }
}

fun ByteArray.getBit(bitNumber: Int): Int {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    val byte = this[byteNumber].toInt() and 0xFF
    return byte[specificBitNumber]
}

val Int.half: Int
    get() = this shr 4


fun Int.modifyByte(mappingBits: Int): Int {
    return (this and 0xF8) or (mappingBits and 0x07)
}

// Приводит [BL, BM, Br] к единому числу
fun Array<Int>.unite(): Int {
    var result = 0
    for (bit in this) {
        result = (result shl 1) or bit
    }
    return result
}

fun File.insertData(byteData: ByteArray): File {
    var currentPairNumber = 0
    val dataLength = byteData.size.let { size ->
        ByteArray(4) { i ->
            (size shr ((3 - i) * 8) and 0xFF).toByte()
        }.decompose()
    }
    val decomposedData = byteData.decompose()
    val data = dataLength + decomposedData
    val outputFile = File(parentFile, "$nameWithoutExtension-output.$extension")

    inputStream().use { input ->
        outputFile.outputStream().use { output ->
            var initialByte: Int
            var readByteNumber = 0
            while (input.read().also { initialByte = it } != -1) {
                if (currentPairNumber < data.size && readByteNumber >= offset) { // оступ от метаданных
                    val half = initialByte.half
                    val Cl = arrayOf((half shr 3) and 1, (half shr 2) and 1)
                    val Cm = arrayOf((half shr 2) and 1, (half shr 1) and 1)
                    val Cr = arrayOf((half shr 1) and 1, half and 1)
                    val Bl = if (Cl.contentEquals(data[currentPairNumber])) {
                        currentPairNumber++
                        1
                    } else {
                        0
                    }
                    val Bm = if (currentPairNumber < data.size && Cm.contentEquals(data[currentPairNumber])) {
                        currentPairNumber++
                        1
                    } else {
                        0
                    }
                    val Br = if (currentPairNumber < data.size && Cr.contentEquals(data[currentPairNumber])) {
                        currentPairNumber++
                        1
                    } else {
                        0
                    }
                    val mappingBits = arrayOf(Bl, Bm, Br).unite()
                    val newByte = initialByte.modifyByte(mappingBits)
                    output.write(newByte)
                } else {
                    output.write(initialByte)
                }
                readByteNumber++
            }
        }
    }
    return outputFile
}

// Задаю оператор для индекса по числу
operator fun Int.get(index: Int): Int {
    return (this shr (7 - index)) and 1
}

fun File.extractData(): ByteArray {
    val bits = mutableListOf<Int>()
    var size = 0
    var byte: Int
    var readByteNumber = 0
    inputStream().use { input ->
        while (input.read().also { byte = it } != -1) {
            if (readByteNumber >= offset) {
                if (size != 0 && bits.size / 8 >= size) {
                    break
                }
                if (bits.size / 8 >= 4 && size == 0) {
                    bits.subList(0, 4 * 8).compose().let {
                        size = (ByteBuffer.wrap(it).int.toLong() and 0xFFFFFFFFL).toInt()
                    }
                    bits.subList(0, 4 * 8).clear()
                }
                val Bl = byte[5]
                if (Bl == 1) {
                    bits.addAll(listOf(byte[0], byte[1]))
                }
                val Bm = byte[6]
                if (Bm == 1) {
                    bits.addAll(listOf(byte[1], byte[2]))
                }
                val Br = byte[7]
                if (Br == 1) {
                    bits.addAll(listOf(byte[2], byte[3]))
                }
            }
            readByteNumber++
        }
    }
    return bits.take(size * 8).compose()
}

fun List<Int>.compose(): ByteArray {
    return ByteArray(this.size / 8) { i ->
        this.subList(i * 8, (i + 1) * 8).toTypedArray().unite().toByte()
    }
}

fun evaluateMSE(
    file1: File,
    file2: File
): Float {
    val input1 = file1.inputStream()
    val input2 = file2.inputStream()

    var currentByte1: Int
    var currentByte2: Int
    var mse = 0.0f
    while(input1.read().also { currentByte1 = it; } != -1) {
        currentByte2 = input2.read()
        mse += (currentByte1 - currentByte2).toDouble().pow(2.0).toFloat()
    }
    mse /= file1.length()

    input1.close()
    input2.close()

    return mse
}

fun evaluatePSNR(max: Float, mse: Float): Float {
    return 10 * log10(max.pow(2) / mse)
}