package mikhail.shell.stego.task3

import java.awt.image.BufferedImage

// Все биты внутри каждого байта обрабатываются справа налево

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
    return (byte shr (7 - specificBitNumber)) and 1
}

val Int.R: Int
    get() = (this shr 16) and 0xFF

val Int.G: Int
    get() = (this shr 8) and 0xFF

val Int.B: Int
    get() = this and 0xFF


val Int.half: Int
    get() = this shr 4


fun Int.modifyByte(mappingBits: Int): Int {
    return (this and 0xF8) or (mappingBits and 0x07)
}

fun Int.modifyBlue(newBlue: Int): Int {
    return (this and 0xFFFFFF00.toInt()) or (newBlue and 0xFF)
}

// Приводит [BL, BM, Br] к единому числу
fun Array<Int>.unite(): Int {
    var result = 0
    for (bit in this) {
        result = (result shl 1) or bit
    }
    return result
}

// Задаю оператор для индекса по числу
operator fun Int.get(index: Int): Int {
    return (this shr (3 - index)) and 1
}

fun BufferedImage.insertData(data: Array<Array<Int>>): BufferedImage {
    var currentPairNumber = 0
    outer@ for (x in 0 until width) {
        for (y in 0 until height) {
            if (currentPairNumber >= data.size) {
                break@outer
            }
            val pixel = getRGB(x, y)
            val blue = pixel.B

            val half = blue.half

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

            val newBlue = blue.modifyByte(mappingBits)
            val newPixel = pixel.modifyBlue(newBlue)
            setRGB(x, y, newPixel)
        }
    }
    return this
}


fun Int.getBit(n: Int): Int {
    return (this shr (7 - n)) and 1
}