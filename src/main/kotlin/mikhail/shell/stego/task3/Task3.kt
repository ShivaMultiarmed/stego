package mikhail.shell.stego.task3

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mikhail.shell.stego.common.*
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO

enum class StegoIntegrationScreen(val title: String) {
    INTEGRATING_SCREEN("Вставка данных"),
    EXTRACTING_SCREEN("Извлечение данных")
}

fun main() = application {
    var checkedTabIndex by remember { mutableStateOf(0) }
    val stegoIntegrationScreen by derivedStateOf { StegoIntegrationScreen.entries[checkedTabIndex] }
    Window(
        title = stegoIntegrationScreen.title,
        onCloseRequest = this::exitApplication
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(
                tabs = StegoIntegrationScreen.entries.associate { it.ordinal to it.title },
                checkedTabNumber = checkedTabIndex,
                onTabSwitch = {
                    checkedTabIndex = it
                }
            )
            if (stegoIntegrationScreen == StegoIntegrationScreen.INTEGRATING_SCREEN) {
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
        StegoTextField(
            value = data,
            onValueChange = {
                data = it
            },
            modifier = Modifier
                .size(500.dp, 300.dp)
        )
        StegoButton (
            onClick = {
                val selectedFiles = openFiles(parent)
                if (selectedFiles != null) {
                    inputPaths.clear()
                    selectedFiles.forEach(inputPaths::add)
                }
            },
            text = "Выбрать файлы"
        )
        StegoButton(
            onClick = {
                outputPaths.clear()
                MSEs.clear()
                PSNRs.clear()
                inputPaths.forEach {
                    val inputFile = File(it)
                    val inputImage = ImageIO.read(inputFile)
                    val outputImage = inputImage.insertData(data.encodeToByteArray().toTypedArray())
                    val outputFile =
                        File(inputFile.parentFile, inputFile.nameWithoutExtension + "-output." + inputFile.extension)
                    ImageIO.write(outputImage, outputFile.extension, outputFile)
                    outputPaths.add(outputFile.absolutePath)
                    MSEs.add(evaluateMSE(inputImage, outputImage))
                    PSNRs.add(evaluatePSNR(255f, MSEs.last()))
                }
            },
            text = "Вставить данные"
        )
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
        StegoButton(
            onClick = {
                filePath = openFile(parent)
            },
            text = "Выбрать файл"
        )
        if (filePath != null) {
            StegoButton(
                onClick = {
                    val file = File(filePath)
                    val image = ImageIO.read(file)
                    val safeImage = image.getSafeImage()
                    val extractedBytes = safeImage.extractData()
                    result = extractedBytes.toByteArray().decodeToString()
                },
                text = "Извлечь данные"
            )
        }
        if (result != null) {
            Text(result!!)
        }
    }
}