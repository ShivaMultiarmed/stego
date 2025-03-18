package mikhail.shell.stego.task3

import java.awt.image.BufferedImage

fun ByteArray.decompose(): Array<Array<Int>> {
    return Array(this.size / 2) { i ->
        arrayOf(this.getBit(2 * i), this.getBit(2 * i + 1))
    }
}

val Int.msb: Int
    get() = this shr 4

fun Int.modifyByte(mappingBits: Int): Int {
    return 0xF8 and this or mappingBits
}

fun BufferedImage.createMappingBits(insertedData: Array<Array<Int>>): Array<Int> {
    var currentPairNumber = 0
    val result = Array(insertedData.size * 2) { 0 }
    for (x in 0..<width) {
        for (y in 0..<height) {
            val pixel = getRGB(x, y)
            val Cl = arrayOf(pixel.msb[0], pixel.msb[1])
            val Cm = arrayOf(pixel.msb[1], pixel.msb[2])
            val Cr = arrayOf(pixel.msb[2], pixel.msb[3])
            val Bl = if (!Cl.contentEquals(insertedData[currentPairNumber])) { 0 } else {
                currentPairNumber++
                1
            }
            val Bm = if (!Cm.contentEquals(insertedData[currentPairNumber])) { 0 } else {
                currentPairNumber++
                1
            }
            val Br = if (!Cr.contentEquals(insertedData[currentPairNumber])) {0} else {
                currentPairNumber++
                1
            }
            if (currentPairNumber == insertedData.size - 1) {
                break
            }
        }
        if (currentPairNumber == insertedData.size - 1) {
            break
        }
    }
    return result
}

fun Array<Int>.toSequence(): Int {
    return "0x" + this.
}

operator fun Int.get(bitNumber: Int): Int {
    return this shr (countOneBits() - bitNumber) and 1
}

fun ByteArray.getBit(bitNumber: Int): Int {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    val byte = this[byteNumber].toInt()
    return byte shr (7 - specificBitNumber) and 1
}