package mikhail.shell.stego.task3

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import kotlin.experimental.and
import kotlin.math.log10
import kotlin.math.pow

fun Array<Byte>.group(size: Int = 2): Array<Array<Byte>> {
    val totalGroups = this.size / size
    return Array(totalGroups) { i ->
        this.toList()
            .subList(i * size, (i + 1) * size)
            .toTypedArray()
    }
}

fun Byte.modifyByte(mappingBits: Byte): Byte {
    return ((this.toInt() and 0xF8) or (mappingBits.toInt() and 0x07)).toByte()
}

fun Array<Byte>.unite(): Byte {
    var result = 0.toByte()
    for (bit in this) {
        result = (result * 2 + bit).toByte()
    }
    return result
}

fun Byte.separate(): Array<Byte> {
    return Array(8) { i ->
        this[i]
    }
}

fun BufferedImage.insertData(byteData: Array<Byte>): BufferedImage {
    val outputImage = BufferedImage(width, height, type)
    var currentPairNumber = 0
    val bits = byteData.decompose()
    val data = pack(bits).group(size = 2)

    val inputBytes = (raster.dataBuffer as DataBufferByte).data
    val outputBytes = (outputImage.raster.dataBuffer as DataBufferByte).data

    for (i in outputBytes.indices) {
        val initialByte = inputBytes[i]
        if (currentPairNumber < data.size) {
            val half = ((initialByte.toInt() and 0xFF) shr 4).toByte().toInt() and 0xFF
            val Cl = arrayOf((half shr 3) and 1, (half shr 2) and 1)
            val Cm = arrayOf((half shr 2) and 1, (half shr 1) and 1)
            val Cr = arrayOf((half shr 1) and 1, half and 1)
            val Bl = if (Cl.contentEquals(data[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }.toByte()
            val Bm = if (currentPairNumber < data.size && Cm.contentEquals(data[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }.toByte()
            val Br = if (currentPairNumber < data.size && Cr.contentEquals(data[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }.toByte()
            val mappingBits = arrayOf(Bl, Bm, Br).unite()
            val newByte = initialByte.modifyByte(mappingBits)
            outputBytes[i] = newByte
        } else {
            outputBytes[i] = initialByte
        }
    }
    return outputImage
}

operator fun Byte.get(index: Int): Byte {
    return ((this.toInt() and 0xFF) shr (7 - index)).toByte() and 1
}

fun BufferedImage.extractData(): Array<Byte> {
    val bits = mutableListOf<Byte>()
    val inputBytes = (raster.dataBuffer as DataBufferByte).data
    inputBytes.forEach { byte ->
        val Bl = byte[5]
        if (Bl == 1.toByte()) {
            bits.addAll(listOf(byte[0], byte[1]))
        }
        val Bm = byte[6]
        if (Bm == 1.toByte()) {
            bits.addAll(listOf(byte[1], byte[2]))
        }
        val Br = byte[7]
        if (Br == 1.toByte()) {
            bits.addAll(listOf(byte[2], byte[3]))
        }
    }
    return unpack(bits).toTypedArray()
}

fun BufferedImage.getSafeImage(): BufferedImage {
    return if (type == BufferedImage.TYPE_BYTE_GRAY) {
        this
    } else {
        val initialBytes = (raster.dataBuffer as DataBufferInt).data
        BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY).apply {
            val outputBytes = (raster.dataBuffer as DataBufferByte).data
            for (i in initialBytes.indices) {
                outputBytes[i] = initialBytes[i].toByte()
            }
        }
    }
}

fun Array<Byte>.compose(): Array<Byte> {
    return Array(this.size / 8) { i ->
        this.toList().subList(i * 8, (i + 1) * 8).toTypedArray().unite()
    }
}

fun Array<Byte>.decompose(): Array<Byte> {
    return this.toList().map {
        it.separate()
    }.toTypedArray().flatten().toTypedArray()
}

fun evaluateMSE(
    inputImage: BufferedImage,
    outputImage: BufferedImage
): Float {
    val inputBytes = (inputImage.raster.dataBuffer as DataBufferByte).data.map { it.toInt() and 0xFF }
    val outputBytes = (outputImage.raster.dataBuffer as DataBufferByte).data.map { it.toInt() and 0xFF }

    var mse = 0.0f

    for (i in inputBytes.indices) {
        mse += (inputBytes[i] - outputBytes[i]).toDouble().pow(2.0).toFloat()
    }

    mse /= inputBytes.size

    return mse
}

fun evaluatePSNR(max: Float, mse: Float): Float {
    return 10 * log10(max.pow(2) / mse)
}

const val START_FLAG: Byte = 0b01111111
const val END_FLAG: Byte = 0b00000000
const val PAYLOAD_SIZE = 16 * 8

fun pack(bits: Array<Byte>): Array<Byte> {
    return bits.toList().windowed(
        size = PAYLOAD_SIZE,
        step = PAYLOAD_SIZE,
        partialWindows = true
    ) {
        listOf(START_FLAG) + it + listOf(END_FLAG)
    }.flatten().toTypedArray()
}

fun unpack(bits: List<Byte>): List<Byte> {
    val resultBits = mutableListOf<Byte>()

    var startFlagIndex = -1
    var endFlagIndex = -1

    for (i in bits.indices) {
        try {
            val currentByte = bits.subList(i, i + 8).toTypedArray().unite()
            if (currentByte == START_FLAG) {
                startFlagIndex = i
            }
            if (currentByte == END_FLAG) {
                endFlagIndex = i
            }
            if ((endFlagIndex - startFlagIndex - 8) <= 128) {
                val payload = bits.subList(startFlagIndex + 8, endFlagIndex)
                resultBits.addAll(payload)
            } else {
                startFlagIndex = -1
                endFlagIndex = -1
            }
        } catch (e: IndexOutOfBoundsException) {
            break
        }
    }

    return resultBits
}