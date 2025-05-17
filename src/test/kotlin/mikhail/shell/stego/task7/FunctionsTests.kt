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
        val hashMatrix = arrayOf(
            arrayOf(1, 0, 1, 1),
            arrayOf(0, 1, 1, 0),
            arrayOf(0, 0, 1, 1),
            arrayOf(1, 1, 1, 1)
        ).map {
            row -> row.map { it.toByte() }.toTypedArray()
        }.toTypedArray()
        val expected = byteArrayOf(0, 0, 1, 0).toTypedArray()
        val actual = createHash(hashMatrix, bits)
        Assertions.assertArrayEquals(expected, actual)
    }
}