package mikhail.shell.stego.task1

import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.util.Random
import javax.imageio.ImageIO

class DwmIntegratingApplication : Stage() {
    private val random = Random()
    private val root = VBox()
    private var inputFile: File? = null
    private var outputFile: File? = null
    private var bytesDWM = ByteArray(0)

    init {
        scene = Scene(root)
        createPickerButton()
        createSavingButton()
        createInputField()
        createInsertingButton()
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
                processImage(inputFile!!, outputFile!!, bytesDWM)
            }
        }
    }

    private fun processImage(inputFile: File, outputFile: File, dwmBytes: ByteArray) {
        val bufferedImage = ImageIO.read(inputFile)
        var bitsWritten = 0
        for (i in 0..<bufferedImage.width) {
            for (j in 0..<bufferedImage.height) {
                val pixel = bufferedImage.getRGB(i, j)
                val bit = bytesDWM.getBit(bitsWritten)
                val newPixel = evaluateNewPixel(pixel, bit)
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

    private fun evaluateNewPixel(pixel: Int, bit: Int): Int {
        val R = pixel and 0xFF0000 shr 16
        val G = pixel and 0x00FF00 shr 8
        val B = pixel and 0x0000FF
        val Y = 0.3 * R + 0.59 * G + 0.11 * B
        val lambda = 0.1
        val newB = if (bit == 1) B + lambda * Y else B - lambda * Y
        return (R shl 16) or (G shl 8) or newB.toInt()
    }
}

fun ByteArray.getBit(bitNumber: Int): Int {
    val byteNumber = bitNumber / 8
    val specificBitNumber = bitNumber % 8
    val byte = this[byteNumber].toInt()
    return byte shr (8 - specificBitNumber) and 1
}