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

enum class ChosenScreen(val title: String) {
    INTEGRATING_SCREEN("Вставка данных"),
    EXTRACTING_SCREEN("Извлечение данных")
}

fun main() = application {
    var chosenScreen by remember { mutableStateOf(ChosenScreen.INTEGRATING_SCREEN) }
    Window(
        title = chosenScreen.title,
        onCloseRequest = this::exitApplication
    ) {
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
    val inputBitmapState = remember {
        inputFilePath?.let {
            derivedStateOf { ImageIO.read(File(it)) }
        }
    }
    var outputFilePath by remember { mutableStateOf(null as String?) }
    val outputBitmapState = remember {
        outputFilePath?.let {
            derivedStateOf { ImageIO.read(File(it)) }
        }
    }
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
                val inputFileName = inputFilePath?.substringAfterLast("/")
                val rawFilePath = inputFileName?.substringBeforeLast(".")
                val extension = inputFileName?.substringAfterLast(".")
                outputFilePath = "$rawFilePath (с данными).$extension"
                if (inputFilePath != null) {
                    insertData(inputFilePath!!, data)
                }
            }
        ) {
            Text("Вставить данные")
        }
        Row {
            inputBitmapState?.value?.let {
                Image(
                    modifier = Modifier.width(300.dp),
                    bitmap = it.toComposeImageBitmap(),
                    contentDescription = null
                )
            }
            outputBitmapState?.value?.let {
                Image(
                    modifier = Modifier.width(300.dp),
                    bitmap = it.toComposeImageBitmap(),
                    contentDescription = null
                )
            }
        }
    }
}

fun insertData(inputImage: String, data: String) {
    val dataBytes = data.toByteArray()
    File(inputImage).insertData(dataBytes)
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
    val file = File(image)
    return file.extractData().decodeToString()
}