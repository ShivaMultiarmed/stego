package mikhail.shell.stego.task7

import mikhail.shell.stego.common.encode
import mikhail.shell.stego.common.times
import mikhail.shell.stego.common.xor
import mikhail.shell.stego.task4.arrange
import mikhail.shell.stego.task4.evaluateNoises
import mikhail.shell.stego.task4.hash
import mikhail.shell.stego.task4.unhash
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO

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

    @Test
    fun testHammingEncoding() {
        val bits = byteArrayOf(1, 0, 0, 0).toTypedArray()
        val parityMatrix = arrayOf(
            byteArrayOf(1, 1, 1, 0, 1, 0, 0),
            byteArrayOf(1, 0, 0, 1, 0, 1, 0),
            byteArrayOf(0, 1, 0, 1, 0, 0, 1)
        )
            .map { it.toTypedArray() }
            .toTypedArray()
        val expectedCode = byteArrayOf(1, 0, 0, 0, 1, 1, 0).toTypedArray()
        val actualCode = encode(parityMatrix, bits)
        Assertions.assertArrayEquals(expectedCode, actualCode)
    }
    @Test
    fun testNoisesEvaluation() {
        val testPath = "D:/Магистратура/2 семестр/Стеганография/containers"
        val file = File(testPath, "38.bmp")
        val image = ImageIO.read(file)
        val imageMatrix = (image.raster.dataBuffer as DataBufferByte)
            .data
            .toTypedArray()
            .arrange(image.width, image.height)
        val noises = evaluateNoises(imageMatrix)
        for (i in noises.indices) {
            for (j in noises[i].indices) {
                print("${noises[i][j]}\t")
            }
            println()
        }
    }

    @Test
    fun testXorEvaluation() {
        val infoBits = byteArrayOf(
            0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1,
            1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0,
            1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0,
            1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1,
            0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0,
            0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1,
            0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1,
            1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0,
            0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1, 0
        ).toTypedArray()

        val keyBits = byteArrayOf(
            0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1,
            0, 1, 1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0, 1,
            1, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0,
            1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0,
            0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1,
            1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0,
            0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1,
            1, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 1, 0,
            1, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0
        ).toTypedArray()

        val xorResult = byteArrayOf(
            0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0,
            1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1,
            0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0,
            0, 1, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1,
            0, 1, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1,
            1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1,
            0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0,
            1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0
        ).toTypedArray()


        val actualResult = infoBits xor keyBits
        Assertions.assertArrayEquals(xorResult, actualResult)
        val actualInitialBits = xorResult xor keyBits
        Assertions.assertArrayEquals(infoBits, actualInitialBits)
    }
}