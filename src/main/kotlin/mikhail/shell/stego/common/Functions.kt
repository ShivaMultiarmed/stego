package mikhail.shell.stego.common

import mikhail.shell.stego.task3.END_FLAG
import mikhail.shell.stego.task3.MAX_PAYLOAD_SIZE
import mikhail.shell.stego.task3.START_FLAG
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import kotlin.experimental.and
import kotlin.experimental.xor

fun Byte.explode(): Array<Byte> {
    return Array(8) { i -> this[i] }
}

fun Array<Byte>.implode(): Byte {
    var result = 0.toByte()
    for (bit in this) {
        result = (result * 2 + bit).toByte()
    }
    return result
}

operator fun Byte.get(index: Int): Byte {
    return ((this.toInt() and 0xFF) shr (7 - index)).toByte() and 1
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
    return this.toList().windowed(
        size = 8,
        step = 8,
        partialWindows = false
    ) {
        it.toTypedArray().implode()
    }.toTypedArray()
}

fun Array<Byte>.decompose(): Array<Byte> {
    return this.toList().map {
        it.explode()
    }.toTypedArray().flatten().toTypedArray()
}

fun pack(bits: Array<Byte>): Array<Byte> {
    return bits.toList().windowed(
        size = MAX_PAYLOAD_SIZE,
        step = MAX_PAYLOAD_SIZE,
        partialWindows = true
    ) {
        START_FLAG.explode().toList() + it + END_FLAG.explode().toList()
    }.flatten().toTypedArray()
}

fun unpack(bits: Array<Byte>): Array<Byte> {
    val flagSize = 8
    val packetSize = flagSize + MAX_PAYLOAD_SIZE + flagSize
    val result = mutableListOf<Byte>()

    var i = 0
    while (i + packetSize <= bits.size) {
        val startFlagBits = bits.slice(i until i + flagSize).toTypedArray()
        val startFlag = startFlagBits.implode()

        if (startFlag == START_FLAG) {
            val endFlagStart = i + flagSize + MAX_PAYLOAD_SIZE
            val endFlagBits = bits.slice(endFlagStart until endFlagStart + flagSize).toTypedArray()
            val endFlag = endFlagBits.implode()

            if (endFlag == END_FLAG) {
                // Добавляем полезную нагрузку в результат
                val payload = bits.slice(i + flagSize until endFlagStart)
                result.addAll(payload)
                i += packetSize
                continue
            } else {
                // Если конечный флаг не совпал, сдвигаемся на 1 бит, чтобы не пропустить пакет
                i += 1
            }
        } else {
            // Если стартовый флаг не совпал, сдвигаемся на 1 бит
            i += 1
        }
    }

    return result.toTypedArray()
}

fun Array<Byte>.getBit(bitNumber: Int): Byte {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    return this[byteNumber][specificBitNumber]
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

inline operator fun <reified T : Number> Array<Array<T>>.times(other: Array<Array<T>>): Array<Array<T>> {
    require(this.isNotEmpty() && other.isNotEmpty()) { "Одна или обе матрицы пустые" }
    require(this[0].size == other.size) { "Количество столбцов в первой матрице не совпадает с количеством строк во второй." }
    val result = Array(this.size) { Array(other[0].size) { 0f } }
    for (i in this.indices) {
        for (j in other[0].indices) {
            for (k in this[0].indices) {
                result[i][j] += this[i][k].toFloat() * other[k][j].toFloat()
            }
        }
    }
    return result.map { row ->
        Array(row.size) { i ->
            row[i].let {
                when(T::class) {
                    Byte::class -> it.toInt().toByte()
                    Int::class -> it.toInt()
                    Long::class -> it.toLong()
                    Float::class -> it
                    Double::class -> it.toDouble()
                    else -> it
                }
            } as T
        }
    }.toTypedArray()
}

fun hash(functionMatrix: Array<Array<Byte>>, bits: Array<Byte>): Array<Byte> {
    val bitsVector = bits.map { bit -> Array(1) { bit } }.toTypedArray()
    val resultVector = functionMatrix * bitsVector
    return resultVector.flatMap { it.asList() }.map { (it % 2).toByte() }.toTypedArray()
}

fun encode(parityMatrix: Array<Array<Byte>>, bits: Array<Byte>): Array<Byte> {
    val verificationBitsCount = parityMatrix[0].size - bits.size
    val verificationBits = ByteArray(verificationBitsCount).toTypedArray()
    for (i in parityMatrix.indices) {
        for (j in bits.indices) {
            if (parityMatrix[i][j].toInt() == 1) {
                verificationBits[i] = bits[j] xor verificationBits[i]
            }
        }
    }
    return bits + verificationBits
}

fun decode(informationalBitsCount: Int, bits: Array<Byte>) = bits.sliceArray(0 until informationalBitsCount)

inline fun <reified T> Array<T>.toVector(): Array<Array<T>> {
    return Array(size) { index ->
        Array(1) {
            this[index]
        }
    }
}