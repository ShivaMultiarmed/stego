package mikhail.shell.stego.task4

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO

fun main() = application {
    var screen by remember { mutableStateOf(ChosenScreen.INTEGRATING_SCREEN) }
    Window(
        onCloseRequest = ::exitApplication,
        title = when (screen) {
            ChosenScreen.INTEGRATING_SCREEN -> "Внедрение данных"
            else -> "Извлечение данных"
        }
    ) {
        App(
            parent = this.window,
            screen = screen,
            onScreenSwitch = {
                screen = it
            }
        )
    }
}

enum class ChosenScreen(val title: String) {
    INTEGRATING_SCREEN("Вставка данных"),
    EXTRACTING_SCREEN("Извлечение данных")
}

@Composable
fun App(
    parent: Frame,
    screen: ChosenScreen,
    onScreenSwitch: (ChosenScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .size(600.dp, 600.dp)
    ) {
        Column {
            Row {
                Button(
                    onClick = { onScreenSwitch(ChosenScreen.INTEGRATING_SCREEN) }
                ) {
                    Text("Внедрить")
                }
                Button(
                    onClick = { onScreenSwitch(ChosenScreen.EXTRACTING_SCREEN) }
                ) {
                    Text("Извлечь")
                }
            }
            Box {
                if (screen == ChosenScreen.INTEGRATING_SCREEN) {
                    IntegratingScreen(parent)
                } else {
                    ExtractingScreen(parent)
                }
            }
        }
    }
}

@Composable
fun IntegratingScreen(
    parent: Frame
) {
    Column {
        var inputPath by remember { mutableStateOf(null as String?) }
        val inputBitmap = remember(inputPath) {
            inputPath?.let { ImageIO.read(File(it)).toComposeImageBitmap() }
        }
        var outputPath by remember { mutableStateOf(null as String?) }
        val outputBitmap = remember (outputPath) {
            outputPath?.let { ImageIO.read(File(it)).toComposeImageBitmap() }
        }
        var data by remember { mutableStateOf("") }
        Button(
            onClick = {
                val selectedFile = openFile(parent, if (inputPath == null) "Выберите изображение" else "Измените изображение")
                if (selectedFile != null) {
                    inputPath = selectedFile
                }
            }
        ) {
            Text(
                text = if (inputPath == null) "Выберите изображение" else "Измените изображение"
            )
        }
        TextField(
            modifier = Modifier.size(400.dp, 200.dp),
            value = data,
            onValueChange = { data = it }
        )
        Button(
            onClick = {
                inputPath?.let {
                    val inputFile = File(it)
                    val inputImage = ImageIO.read(inputFile)
                    val interpolatedImage = inputImage.interpolate()
                    val dataBytes = data.encodeToByteArray()
                    val outputImage = interpolatedImage.insertData(dataBytes)
                    val outputFile = File(inputFile.parentFile, inputFile.nameWithoutExtension + "-output." + inputFile.extension)
                    ImageIO.write(outputImage, outputFile.extension, outputFile)
                    outputPath = outputFile.path
                }
            }
        ) {
            Text(
                text = "Внедрить данные"
            )
        }
        Row {
            if (inputBitmap != null) {
                Image(
                    modifier = Modifier,
                    bitmap = inputBitmap,
                    contentDescription = null
                )
            }
            if (outputBitmap != null) {
                Image(
                    modifier = Modifier,
                    bitmap = outputBitmap,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ExtractingScreen(
    parent: Frame
) {
    Column {
        var inputPath by remember { mutableStateOf(null as String?) }
        var extractedData by remember { mutableStateOf(null as String?) }
        Button(
            onClick = {
                val selectedFile = openFile(parent, if (inputPath == null) "Выберите изображение" else "Измените изображение")
                if (selectedFile != null) {
                    inputPath = selectedFile
                }
            }
        ) {
            Text(
                text = if (inputPath == null) "Выберите изображение" else "Измените изображение"
            )
        }
        Button(
            onClick = {
                inputPath?.let {
                    val inputFile = File(it)
                    val inputImage = ImageIO.read(inputFile)
                    val extractedBytes = inputImage.extractData()
                    extractedData = extractedBytes.decodeToString()
                }
            }
        ) {
            Text(
                text = "Извлечь данные"
            )
        }
        extractedData?.let {
            Text(it)
        }
    }
}

fun openFile(
    parent: Frame,
    title: String = "Выберите файл"
): String? {
    val dialog = FileDialog(parent, title, FileDialog.LOAD)
    dialog.isVisible = true
    return if (dialog.file != null) "${dialog.directory}${dialog.file}" else null
}