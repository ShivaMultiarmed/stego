package mikhail.shell.stego.task3

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO

enum class ChosenScreen {
    INTEGRATING_SCREEN, EXTRACTING_SCREEN
}

fun main(args: Array<String>) = application {

    Window(
        onCloseRequest = this::exitApplication
    ) {
        var chosenScreen by remember { mutableStateOf(ChosenScreen.INTEGRATING_SCREEN) }
        Column {
            Row {
                Button(
                    onClick = {
                        chosenScreen = ChosenScreen.INTEGRATING_SCREEN
                    }
                ) {
                    Text("Вставка")
                }
                Button(
                    onClick = {
                        chosenScreen = ChosenScreen.EXTRACTING_SCREEN
                    }
                ) {
                    Text("Извлечение")
                }
            }
            if (chosenScreen == ChosenScreen.INTEGRATING_SCREEN) {
                IntegratingScreen(window)
            } else {
                ExtractingScreen(window)
            }
        }
    }
}

@Composable
fun IntegratingScreen(
    parent: Frame
) {
    var data by remember { mutableStateOf("") }
    var inputFilePath by remember { mutableStateOf(null as String?) }
    var outputFilePath by remember { mutableStateOf(null as String?) }
    Column {
        TextField(
            value = data,
            onValueChange = {
                data = it
            }
        )
        Button(
            onClick = {
                inputFilePath = openFile(parent)
            }
        ) {
            Text("Выбрать файл")
        }
        Button(
            onClick = {
                val parentPath = inputFilePath?.substringBeforeLast("/")
                val inputFileName = inputFilePath?.substringAfterLast("/")
                val rawFilePath = inputFileName?.substringBeforeLast(".")
                val extension = inputFileName?.substringAfterLast(".")
                outputFilePath = "$rawFilePath (с данными).$extension"
                if (inputFilePath != null) {
                    insertData(inputFilePath!!, data, outputFilePath!!)
                }
            }
        ) {
            Text("Вставить данные")
        }
        Row {
            if (inputFilePath != null) {
                Image(
                    modifier = Modifier.width(300.dp),
                    bitmap = ImageIO.read(File(inputFilePath!!)).toComposeImageBitmap(),
                    contentDescription = null
                )
            }
            if (outputFilePath != null) {
                Image(
                    modifier = Modifier.width(300.dp),
                    bitmap = ImageIO.read(File(outputFilePath!!)).toComposeImageBitmap(),
                    contentDescription = null
                )
            }
        }
    }
}

fun insertData(inputImage: String, data: String, outputImage: String) {
    val bufferedInputImage = ImageIO.read(File(inputImage))
    val dataBytes = data.toByteArray()
    val bufferedOutputImage = bufferedInputImage.insertData(dataBytes)
    val extension = outputImage.substringAfterLast(".")
    ImageIO.write(bufferedOutputImage, extension, File(outputImage))
}

fun openFile(
    parent: Frame,
    title: String = "Выберите файл"
): String {
    val dialog = FileDialog(parent, title, FileDialog.LOAD)
    dialog.isVisible = true
    return "${dialog.directory}${dialog.file}"
}

@Composable
fun ExtractingScreen(parent: Frame) {
    var filePath by remember { mutableStateOf(null as String?) }
    var result by remember { mutableStateOf<String?>(null) }
    Column {
        Button(
            onClick = {
                filePath = openFile(parent)
            }
        ) {
            Text("Выбрать файл")
        }
        Button(
            onClick = {
                filePath?.let {
                    result = extractData(it)
                }
            }
        ) {
            Text("Извлечь данные")
        }
        if (result != null) {
            Text(result!!)
        }
    }
}

fun extractData(image: String): String {
    val bufferedImage = ImageIO.read(File(image))
    return bufferedImage.extractData().decodeToString()
}