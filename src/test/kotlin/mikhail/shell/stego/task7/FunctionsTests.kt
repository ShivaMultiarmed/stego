package mikhail.shell.stego.task7

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FunctionsTests {
    @Test
    fun testMatricesProducts() {
        val a = arrayOf(
            arrayOf(0.0, 1.0, 2.0),
            arrayOf(3.0, 4.0, 5.0)
        )
        val b = arrayOf(
            arrayOf(5.0, 4.0),
            arrayOf(3.0, 2.0),
            arrayOf(1.0, 0.0)
        )
        val expected = arrayOf(
            arrayOf(5.0, 2.0),
            arrayOf(32.0, 20.0)
        )
        val actual = a * b
        Assertions.assertArrayEquals(expected, actual)
    }

    @Test
    fun testHashFunction() {
        val bits = byteArrayOf(1, 0, 0, 1).toTypedArray()
        val expectedHash = byteArrayOf(0, 0, 1, 0).toTypedArray()
        val actualHash = hash(bits)
        Assertions.assertArrayEquals(expectedHash, actualHash)
        val actualBits = unhash(expectedHash)
        Assertions.assertArrayEquals(bits, actualBits)
    }
}