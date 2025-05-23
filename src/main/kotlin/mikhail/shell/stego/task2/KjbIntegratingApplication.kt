package mikhail.shell.stego.task2

import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import mikhail.shell.stego.common.compose
import mikhail.shell.stego.common.decompose
import mikhail.shell.stego.common.pack
import mikhail.shell.stego.common.unpack
import java.awt.image.BufferedImage
import java.io.File
import java.util.Random
import javax.imageio.ImageIO
import kotlin.math.roundToInt

class KjbIntegratingApplication : Stage() {
    private val random = Random()
    private val root = VBox()
    private var inputFile: File? = null
    private var outputFile: File? = null
    private var dataBytes = ByteArray(0)
    private val sigma = 3

    init {
        scene = Scene(root)
        show()
        createPickerButton()
        createInputField()
        createInsertingButton()
        createExtractingButton()
    }

    private fun createPickerButton() {
        val button = Button("Выбрать файл")
        root.children.add(button)
        button.setOnMouseClicked {
            val inputFileChooser = FileChooser()
            inputFileChooser.initialDirectory = inputFile?.parentFile
            inputFile = inputFileChooser.showOpenDialog(this)
            outputFile = File(inputFile?.parentFile?.absolutePath, inputFile?.nameWithoutExtension + "-output." + inputFile?.extension)
        }
    }

    private fun createInputField() {
        val field = TextArea().apply {
            prefHeight = 500.0
            prefWidth = 600.0
            prefColumnCount = 600
            isWrapText = true
            maxWidth = 600.0
        }
        root.children.add(field)
        field.textProperty().addListener { _, _, newValue ->
            dataBytes = newValue.encodeToByteArray()
        }
    }

    private fun createInsertingButton() {
        val button = Button("Встроить данные")
        root.children.add(button)
        button.setOnMouseClicked {
            if (inputFile != null && outputFile != null) {
                insertData(inputFile!!, outputFile!!)
                val resultImage = ImageView(outputFile!!.absolutePath).apply {
                    fitWidth = 300.0
                    isPreserveRatio = true
                }
                try {
                    val previousImageView = root.children.last { it is ImageView }
                    root.children.remove(previousImageView)
                    val previousDwmView = root.children.last { it is Text }
                    root.children.remove(previousDwmView)
                } catch (_: NoSuchElementException) {}
                root.children.add(resultImage)
            }
        }
    }

    private fun createExtractingButton() {
        val button = Button("Извлечь данные")
        root.children.add(button)
        button.setOnMouseClicked {
            if (inputFile != null && outputFile != null) {
                val extractedDwm = extractData(outputFile!!).decodeToString()
                val extractedDwmView = Text(extractedDwm)
                root.children.add(extractedDwmView)
            }
        }
    }

    private fun extractData(file: File): ByteArray {
        val dwmBits = mutableListOf<Int>()
        val bufferedImage = ImageIO.read(file)
        for (i in sigma + 1..<bufferedImage.width - sigma) {
            for (j in sigma + 1..<bufferedImage.height - sigma) {
                val pixel = bufferedImage.getRGB(i, j)
                val B = getBlue(pixel)
                val avgB = evaluateAverageBlue(bufferedImage, i, j)
                dwmBits.add(if (B > avgB) 1 else 0)
            }
        }
        return unpack(dwmBits.map { it.toByte() }.toTypedArray()).compose().toByteArray()
    }

    private fun evaluateAverageBlue(bufferedImage: BufferedImage, x: Int, y: Int): Int {
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

    private fun insertData(inputFile: File, outputFile: File) {
        val bufferedImage = ImageIO.read(inputFile)
        val bits = pack(dataBytes.toTypedArray().decompose())
        var bitsWritten = 0
        for (i in sigma + 1..<bufferedImage.width - sigma) {
            for (j in sigma + 1..<bufferedImage.height - sigma) {
                val pixel = bufferedImage.getRGB(i, j)
                val bit = bits[bitsWritten].toInt()
                val newPixel = evaluateNewPixel(pixel, bit, bufferedImage.colorModel.pixelSize == 32)
                bufferedImage.setRGB(i, j, newPixel)
                bitsWritten++
                if (bitsWritten == dataBytes.size * 8)
                    break
            }
            if (bitsWritten == dataBytes.size * 8)
                break
        }
        ImageIO.write(bufferedImage, outputFile.extension, outputFile)
    }

    private fun generateRandomBytes(bytesNumber: Int): ByteArray {
        val bytes = ByteArray(bytesNumber)
        random.nextBytes(bytes)
        return bytes
    }

    private fun evaluateNewPixel(pixel: Int, bit: Int, respectAlpha: Boolean = false): Int {
        val R = (pixel shr 16) and 0xFF
        val G = (pixel shr 8) and 0xFF
        val B = pixel and 0xFF
        val Y = 0.298 * R + 0.586 * G + 0.114 * B
        val lambda = 0.1
        val delta = (Y * lambda).roundToInt()
        val newB = (if (bit == 1) B + delta else B - delta).coerceIn(0..255)
        return if (respectAlpha) {
            val A = (pixel shr 24) and 0xFF
            (A shl 24) or (R shl 16) or (G shl 8) or newB
        } else {
            (R shl 16) or (G shl 8) or newB
        }
    }
    private fun ByteArray.getBit(bitNumber: Int): Int {
        val byteNumber = bitNumber / 8
        val specificBitNumber = bitNumber % 8
        val byte = this[byteNumber].toInt()
        return byte shr (7 - specificBitNumber) and 1
    }
}
