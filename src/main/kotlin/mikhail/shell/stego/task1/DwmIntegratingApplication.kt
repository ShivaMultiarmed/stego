package mikhail.shell.stego.task1

import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Math.pow
import java.util.Random
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.roundToInt

class DwmIntegratingApplication : Stage() {
    private val random = Random()
    private val root = VBox()
    private var inputFile: File? = null
    private var outputFile: File? = null
    private var bytesDWM = ByteArray(0)
    private val sigma = 3

    init {
        scene = Scene(root)
        show()
        createPickerButton()
        createSavingButton()
        createInputField()
        createInsertingButton()
        createExtractingButton()
    }

    private fun createPickerButton() {
        val button = Button("Выбрать файл")
        root.children.add(button)
        button.setOnMouseClicked {
            val inputFileChooser = FileChooser()
            inputFile = inputFileChooser.showOpenDialog(this)
        }
    }

    private fun createSavingButton() {
        val button = Button("Выбрать место сохранения")
        root.children.add(button)
        button.setOnMouseClicked {
            val outputFileChooser = FileChooser()
            outputFile = outputFileChooser.showSaveDialog(this)
        }
    }

    private fun createInputField() {
        val field = TextField()
        root.children.add(field)
        field.textProperty().addListener { _, _, newValue ->
            bytesDWM = newValue.encodeToByteArray()
        }
    }

    private fun createInsertingButton() {
        val button = Button("Встроить ЦВЗ")
        root.children.add(button)
        button.setOnMouseClicked {
            if (inputFile != null && outputFile != null) {
                processImage(inputFile!!, outputFile!!)
                val resultImage = ImageView(outputFile!!.absolutePath).apply {
                    fitWidth = 300.0
                    isPreserveRatio = true
                }
                try {
                    val previousImageView = root.children.last { it is ImageView }
                    root.children.remove(previousImageView)
                    val previousDwmView = root.children.last { it is Text }
                    root.children.remove(previousDwmView)
                } catch (e: NoSuchElementException) {}
                root.children.add(resultImage)
            }
        }
    }

    private fun createExtractingButton() {
        val button = Button("Извлечь ЦВЗ")
        root.children.add(button)
        button.setOnMouseClicked {
            if (inputFile != null && outputFile != null) {
                val extractedDwm = extractDwm(outputFile!!).decodeToString()
                val extractedDwmView = Text(extractedDwm)
                root.children.add(extractedDwmView)
            }
        }
    }

    private fun extractDwm(file: File): ByteArray {
        val dwmBits = mutableListOf<Int>()
        val bufferedImage = ImageIO.read(file)
        val dwmBitLength = bytesDWM.size * 8
        for (i in sigma + 1..<bufferedImage.width - sigma) {
            for (j in sigma + 1..<bufferedImage.height - sigma) {
                val pixel = bufferedImage.getRGB(i, j)
                val B = getBlue(pixel)
                val avgB = evaluateAverageBlue(bufferedImage, i, j)
                dwmBits.add(if (B > avgB) 1 else 0)
                if (dwmBits.size == dwmBitLength) {
                    break
                }
            }
            if (dwmBits.size == dwmBitLength) {
                break
            }
        }
        return ByteArray(dwmBits.size / 8) {
            var byteValue = 0
            for (bit in 0 until 8) {
                byteValue = (byteValue shl 1) or dwmBits[it * 8 + bit]
            }
            byteValue.toByte()
        }
    }

    private fun evaluateAverageBlue(bufferedImage: BufferedImage, x: Int, y: Int): Int {
        val sigma = 3
        var sum = 0
        for (i in 1..sigma) {
            if (x - i >= 0) {
                sum += getBlue(bufferedImage.getRGB(x - i, y))
            }
            if (x + i < bufferedImage.width) {
                sum += getBlue(bufferedImage.getRGB(x + i, y))
            }
            if (y - i >= 0) {
                sum += getBlue(bufferedImage.getRGB(x, y - i))
            }
            if (y + i < bufferedImage.height) {
                sum += getBlue(bufferedImage.getRGB(x, y + i))
            }
        }
        return sum / (4 * sigma)
    }

    private fun getBlue(pixel: Int): Int {
        return pixel and 0xFF
    }

    private fun processImage(inputFile: File, outputFile: File) {
        val bufferedImage = ImageIO.read(inputFile)
        var bitsWritten = 0
        for (i in sigma + 1..<bufferedImage.width - sigma) {
            for (j in sigma + 1..<bufferedImage.height - sigma) {
                val pixel = bufferedImage.getRGB(i, j)
                val bit = bytesDWM.getBit(bitsWritten)
                val newPixel = evaluateNewPixel(pixel, bit, bufferedImage.colorModel.hasAlpha())
                bufferedImage.setRGB(i, j, newPixel)
                bitsWritten++
                if (bitsWritten == bytesDWM.size * 8)
                    break
            }
            if (bitsWritten == bytesDWM.size * 8)
                break
        }
        ImageIO.write(bufferedImage, outputFile.extension, outputFile)
    }

    private fun generateRandomDwm(bytesNumber: Int): ByteArray {
        val bytes = ByteArray(bytesNumber)
        random.nextBytes(bytes)
        return bytes
    }

    private fun evaluateNewPixel(pixel: Int, bit: Int, respectAlpha: Boolean = false): Int {
        val A = if (respectAlpha) (pixel shr 24) and 0xFF else 0xFF
        val R = (pixel shr 16) and 0xFF
        val G = (pixel shr 8) and 0xFF
        val B = pixel and 0xFF
        val Y = 0.3 * R + 0.59 * G + 0.11 * B
        val lambda = 0.1
        val delta = (Y * lambda).roundToInt()
        val newB = (if (bit == 1) B + delta else B - delta).coerceIn(0..255)
        return if (respectAlpha) {
            (A shl 24) or (R shl 16) or (G shl 8) or newB
        } else {
            (R shl 16) or (G shl 8) or newB
        }
    }
}

fun ByteArray.getBit(bitNumber: Int): Int {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    val byte = this[byteNumber].toInt()
    return byte shr (7 - specificBitNumber) and 1
}