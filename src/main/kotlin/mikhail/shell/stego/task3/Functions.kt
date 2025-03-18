package mikhail.shell.stego.task3

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

// --- Secret Message Processing ---

/**
 * Decomposes a secret message (as a ByteArray) into an array of two-bit pairs.
 * Each byte contains 8 bits so we obtain 4 two-bit pairs per byte.
 */
fun ByteArray.decompose(): Array<Array<Int>> {
    val totalPairs = this.size * 4  // 4 pairs per byte
    return Array(totalPairs) { i ->
        arrayOf(this.getBit(2 * i), this.getBit(2 * i + 1))
    }
}

/**
 * Returns the bit at the given bit position (0 is the left-most bit in a byte).
 */
fun ByteArray.getBit(bitNumber: Int): Int {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    val byte = this[byteNumber].toInt() and 0xFF
    return (byte shr (7 - specificBitNumber)) and 1
}

// --- Pixel Utilities ---

/**
 * Extension properties to extract individual color channels from an ARGB pixel.
 */
val Int.R: Int
    get() = (this shr 16) and 0xFF

val Int.G: Int
    get() = (this shr 8) and 0xFF

val Int.B: Int
    get() = this and 0xFF

/**
 * Returns the high nibble (4 MSBs) of an 8-bit value.
 */
val Int.nibble: Int
    get() = this shr 4

/**
 * Modifies a blue channel byte by replacing its last 3 bits (LSBs) with mappingBits.
 * The mask 0xF8 (1111 1000) clears the lower 3 bits.
 */
fun Int.modifyByte(mappingBits: Int): Int {
    return (this and 0xF8) or (mappingBits and 0x07)
}

/**
 * Replaces the blue channel (the least significant 8 bits) of an ARGB pixel with newBlue,
 * while keeping the alpha, red and green channels unchanged.
 */
fun Int.modifyBlue(newBlue: Int): Int {
    return (this and 0xFFFFFF00.toInt()) or (newBlue and 0xFF)
}

/**
 * Converts an array of bits (e.g. [b1, b2, b3]) into a single integer.
 * The first element is treated as the most significant bit.
 */
fun Array<Int>.toSequence(): Int {
    var result = 0
    for (bit in this) {
        result = (result shl 1) or bit
    }
    return result
}

/**
 * Operator function to allow extraction of bits from a 4-bit number (a nibble).
 * Here index 0 returns the most significant bit.
 */
operator fun Int.get(index: Int): Int {
    // For a nibble, index 0 gives bit 3, index 1 gives bit 2, etc.
    return (this shr (3 - index)) and 1
}

// --- Embedding Process ---

/**
 * Embeds secret data into the blue channel of a cover image.
 *
 * The secret data (an array of two-bit pairs) is compared sequentially with three overlapping
 * two-bit segments of the blue channel’s high nibble (CL, CM, CR). For each segment:
 *
 * - If the secret two-bit pair matches the segment, the corresponding mapping bit is set to 1 and
 *   the pointer is advanced (using the next secret pair for the next comparison).
 * - If there is no match, the mapping bit is set to 0 and the same secret pair is used for the next comparison.
 *
 * The three mapping bits (one per segment) are then combined into a 3-bit value and stored in the
 * three least significant bits of the blue channel.
 */
fun BufferedImage.insertData(data: Array<Array<Int>>): BufferedImage {
    var currentPairNumber = 0
    outer@ for (x in 0 until width) {
        for (y in 0 until height) {
            if (currentPairNumber >= data.size) break@outer
            val pixel = getRGB(x, y)
            val blue = pixel.B

            // Extract the 4 MSBs (nibble) from the blue channel.
            val nibble = blue.nibble  // value between 0 and 15

            // Form three overlapping two-bit groups.
            val Cl = arrayOf((nibble shr 3) and 1, (nibble shr 2) and 1)
            val Cm = arrayOf((nibble shr 2) and 1, (nibble shr 1) and 1)
            val Cr = arrayOf((nibble shr 1) and 1, nibble and 1)

            // Case 1: Compare with CL.
            val Bl: Int = if (Cl.contentEquals(data[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }
            // Case 2: Compare with CM (if there are still secret pairs left).
            val Bm: Int = if (currentPairNumber < data.size && Cm.contentEquals(data[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }
            // Case 3: Compare with CR (if there are still secret pairs left).
            val Br: Int = if (currentPairNumber < data.size && Cr.contentEquals(data[currentPairNumber])) {
                currentPairNumber++
                1
            } else {
                0
            }

            // Combine mapping bits into a 3-bit integer.
            val mappingBits = arrayOf(Bl, Bm, Br).toSequence()

            // Replace the last 3 bits of blue with mappingBits.
            val newBlue = blue.modifyByte(mappingBits)
            // Construct a new pixel with the modified blue channel.
            val newPixel = pixel.modifyBlue(newBlue)
            // Set the new pixel value.
            setRGB(x, y, newPixel)
        }
    }
    return this
}
