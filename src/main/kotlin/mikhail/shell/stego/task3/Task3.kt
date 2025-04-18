package mikhail.shell.stego.task3

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mikhail.shell.stego.task5.openFiles
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
    val inputPaths = remember { mutableStateListOf<String>() }
    val inputBitmaps by derivedStateOf {
        inputPaths.map { ImageIO.read(File(it)).toComposeImageBitmap() }
    }
    val outputPaths = remember { mutableStateListOf<String>() }
    val outputBitmaps by derivedStateOf {
        outputPaths.map { ImageIO.read(File(it)).toComposeImageBitmap() }
    }
    val MSEs = remember { mutableStateListOf<Float>() }
    val PSNRs = remember { mutableStateListOf<Float>() }
    Column(
        modifier = Modifier
            //.verticalScroll(rememberScrollState())
    ) {
        TextField(
            value = data,
            onValueChange = {
                data = it
            },
            modifier = Modifier
                .width(500.dp)
                .height(300.dp)
        )
        Button(
            onClick = {
                val selectedFiles = openFiles(parent)
                if (selectedFiles != null) {
                    inputPaths.clear()
                    selectedFiles.forEach(inputPaths::add)
                }
            }
        ) {
            Text("Выбрать файлы")
        }
        Button(
            onClick = {
                outputPaths.clear()
                MSEs.clear()
                PSNRs.clear()
                inputPaths.forEach {
                    val inputFile = File(it)
                    val inputImage = ImageIO.read(inputFile)
                    val outputImage = inputImage.insertData(data.encodeToByteArray())
                    val outputFile =
                        File(inputFile.parentFile, inputFile.nameWithoutExtension + "-output." + inputFile.extension)
                    ImageIO.write(outputImage, outputFile.extension, outputFile)
                    outputPaths.add(outputFile.absolutePath)
                    MSEs.add(evaluateMSE(inputImage, outputImage))
                    PSNRs.add(evaluatePSNR(255f, MSEs.last()))
                }
            }
        ) {
            Text("Вставить данные")
        }
        val scrollState = rememberScrollState()
        if (outputPaths.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                for (i in outputPaths.indices) {
                    Column {
                        Text("Вычисленный MSE = ${MSEs[i]}")
                        Text("Вычисленный PSNR = ${PSNRs[i]}")
                        Image(
                            modifier = Modifier.width(300.dp),
                            bitmap = inputBitmaps[i],
                            contentDescription = null
                        )
                        Image(
                            modifier = Modifier.width(300.dp),
                            bitmap = outputBitmaps[i],
                            contentDescription = null
                        )
                    }
                }
            }
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.fillMaxWidth()
        )
    }
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
        if (filePath != null) {
            Button(
                onClick = {
                    val file = File(filePath)
                    val image = ImageIO.read(file)
                    val extractedBytes = image.extractData()
                    result = extractedBytes.decodeToString()
                }
            ) {
                Text("Извлечь данные")
            }
        }
        if (result != null) {
            Text(result!!)
        }
    }
}