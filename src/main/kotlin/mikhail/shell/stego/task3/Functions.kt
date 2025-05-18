package mikhail.shell.stego.task3

import mikhail.shell.stego.common.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
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

fun BufferedImage.insertData(byteData: Array<Byte>): BufferedImage {
    val outputImage = BufferedImage(width, height, type)
    var currentPairNumber = 0
    val bits = byteData.decompose()
    val dataPairs = pack(bits).group(size = 2)

    val inputBytes = (raster.dataBuffer as DataBufferByte).data
    val outputBytes = (outputImage.raster.dataBuffer as DataBufferByte).data

    for (i in outputBytes.indices) {
        val initialByte = inputBytes[i]
        if (currentPairNumber < dataPairs.size) {
            val Cl = arrayOf(initialByte[0], initialByte[1])
            val Cm = arrayOf(initialByte[1], initialByte[2])
            val Cr = arrayOf(initialByte[2], initialByte[3])
            val Bl = if (Cl.contentEquals(dataPairs[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }.toByte()
            val Bm = if (currentPairNumber < dataPairs.size && Cm.contentEquals(dataPairs[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }.toByte()
            val Br = if (currentPairNumber < dataPairs.size && Cr.contentEquals(dataPairs[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }.toByte()
            val mappingBits = arrayOf(Bl, Bm, Br).implode()
            val newByte = initialByte.modifyByte(mappingBits)
            outputBytes[i] = newByte
        } else {
            outputBytes[i] = initialByte
        }
    }
    return outputImage
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
    return unpack(bits.toTypedArray()).compose()
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

const val START_FLAG: Byte = 0x01
const val END_FLAG: Byte = 0x0F
const val MAX_PAYLOAD_SIZE = 16 * 8

