package mikhail.shell.stego.task1

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Spinner
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import javax.imageio.ImageIO

class InputStage : Stage() {

    private val root = VBox()
    private val resultContainer = HBox()
    private val bitNumberInput = Spinner<Int>(0, 7, 0)

    private var chosenFile: File = File("")
    private var targetPath: File = File("")
    private var bitNumber: Int = 0

    init {
        createCore()
        createBitNumberInput()
        createFileChooserButton()
        createSubmitButton()
        createResultBox()
    }

    private fun createCore() {
        isResizable = true
        centerOnScreen()
        height = 400.0
        width = 400.0
        scene = Scene(root)
        title = "Визуальная атака"
    }

    private fun createBitNumberInput() {
        val bitNumberRow = HBox()
        bitNumberRow.children.addAll(Text("Введите номер бита"), bitNumberInput)
        root.children.add(bitNumberRow)
    }

    private fun createFileChooserButton() {
        val fileChooser = FileChooser()
        val fileChooserButton = Button("Выбрать")
        val fileChooserRow = HBox()
        fileChooserRow.children.addAll(Text("Исходный файл и место сохранения"), fileChooserButton)
        root.children.add(fileChooserRow)
        fileChooserButton.onMouseClicked = EventHandler {
            chosenFile = File("")
            chosenFile = fileChooser.showOpenDialog(this)
            try {
                if (chosenFile.path.isNotEmpty()) {
                    targetPath = File("")
                    targetPath = fileChooser.showSaveDialog(this)
                }
            } catch (e: NullPointerException) {}
        }
    }

    private fun createSubmitButton() {
        val submitButton = Button("Анализировать")
        root.children.add(submitButton)
        submitButton.onMouseClicked = EventHandler {
            processData()
        }
    }

    private fun createImage(file: File) {
        val inputStream = file.inputStream()
        val image: Image
        val imageView: ImageView
        inputStream.use {
            image = Image(it)
            imageView = ImageView(image).also {
                it.prefWidth(300.0)
                it.isPreserveRatio = true
            }
        }
        resultContainer.children.add(imageView)
    }

    private fun createResultBox() {
        root.children.add(resultContainer)
    }

    private fun processFile(file: File, bitNumber: Int, targetPath: File) {
        val bufferedImage = ImageIO.read(file)?: return
        for (x in 0..<bufferedImage.width) {
            for (y in 0..<bufferedImage.height) {
                val pixel = bufferedImage.getRGB(x, y)
                val newColor = if (pixel.getBit(bitNumber) == 1) 0xFFFFFF else 0x000000
                bufferedImage.setRGB(x, y, newColor)
            }
        }
        ImageIO.write(bufferedImage, "png", targetPath)
    }

    private fun processData() {
        resultContainer.children.clear()
        bitNumber = bitNumberInput.value
        if (chosenFile.path.isNotEmpty()) {
            createImage(chosenFile)
            if (targetPath.path.isNotEmpty()) {
                processFile(chosenFile, bitNumber, targetPath)
                createImage(targetPath)
            }
        }
    }
}

fun Int.getBit(n: Int): Int {
    return (this shr n) and 1
}