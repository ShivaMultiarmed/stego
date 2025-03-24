package mikhail.shell.stego.task3

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        Column(
            modifier = Modifier
                .width(600.dp)
                .height(1000.dp)
                .verticalScroll(rememberScrollState())
        ) {
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
    val inputBitmapState = remember(inputFilePath) {
        inputFilePath?.let { ImageIO.read(File(it)) }?.toComposeImageBitmap()
    }
    var outputFilePath by remember { mutableStateOf(null as String?) }
    val outputBitmapState = remember(outputFilePath) {
        outputFilePath?.let { ImageIO.read(File(it)) }?.toComposeImageBitmap()
    }
    var mse by remember { mutableStateOf(null as Float?) }
    var psnr by remember { mutableStateOf(null as Float?) }
    Column {
        TextField(
            value = data,
            onValueChange = {
                data = it
            },
            modifier = Modifier
                .width(600.dp)
                .height(400.dp)
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
                outputFilePath = "$rawFilePath-output.$extension"
                if (inputFilePath != null) {
                    outputFilePath = insertData(inputFilePath!!, data).path
                    mse = evaluateMSE(File(inputFileName!!), File(outputFilePath!!))
                    psnr = evaluatePSNR(255f, mse!!)
                }
            }
        ) {
            Text("Вставить данные")
        }
        Column {
            mse?.let {
                Text("Вычисленный MSE = $it")
            }
            psnr?.let {
                Text("Вычисленный PSNR = $it")
            }
            Row {
                inputBitmapState?.let {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
                outputBitmapState?.let {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

fun insertData(inputImage: String, data: String): File {
    val dataBytes = data.toByteArray()
    val inputFile = File(inputImage)
    val outputFile = inputFile.insertData(dataBytes)
    return outputFile
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