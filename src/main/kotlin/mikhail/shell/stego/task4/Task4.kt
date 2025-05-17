package mikhail.shell.stego.task4

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import mikhail.shell.stego.common.StegoButton
import mikhail.shell.stego.common.openFiles
import mikhail.shell.stego.common.TabRow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO

fun main() = application {
    var screen by remember { mutableStateOf(InterpolatingScreen.INTEGRATING_SCREEN) }
    Window(
        onCloseRequest = ::exitApplication,
        title = when (screen) {
            InterpolatingScreen.INTEGRATING_SCREEN -> "Внедрение данных"
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

enum class InterpolatingScreen(val title: String) {
    INTEGRATING_SCREEN("Вставка данных"),
    EXTRACTING_SCREEN("Извлечение данных")
}

@Composable
fun App(
    parent: Frame,
    screen: InterpolatingScreen,
    onScreenSwitch: (InterpolatingScreen) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabScreen by derivedStateOf { InterpolatingScreen.entries[tabIndex] }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            tabs = InterpolatingScreen.entries.associate { it.ordinal to it.title },
            checkedTabNumber = tabIndex,
            onTabSwitch = {
                tabIndex = it
                onScreenSwitch(tabScreen)
            }
        )
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (screen == InterpolatingScreen.INTEGRATING_SCREEN) {
                IntegratingScreen(parent)
            } else {
                ExtractingScreen(parent)
            }
        }
    }
}

@Composable
fun IntegratingScreen(
    parent: Frame
) {
    val screenScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(1f) }
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .verticalScroll(screenScrollState)
        ) {
            val inputPaths = remember { mutableStateListOf<String>() }
            val interpolatedPaths = remember { mutableStateListOf<String>() }
            val interpolatedBitmaps by derivedStateOf {
                inputPaths.map { ImageIO.read(File(it)).toComposeImageBitmap() }
            }
            val outputPaths = remember { mutableStateListOf<String>() }
            val outputBitmaps by derivedStateOf {
                outputPaths.map { ImageIO.read(File(it)).toComposeImageBitmap() }
            }
            var data by remember { mutableStateOf("") }
            val mse = remember { mutableStateListOf<Float>() }
            val psnr = derivedStateOf { mse.map { evaluatePSNR(255f, it) } }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                StegoButton(
                    onClick = {
                        val selectedFiles =
                            openFiles(parent, if (inputPaths.isEmpty()) "Выберите изображения" else "Измените изображения")
                        if (selectedFiles != null) {
                            inputPaths.clear()
                            selectedFiles.forEach(inputPaths::add)
                        }
                    },
                    text = if (inputPaths.isEmpty()) "Выберите изображение" else "Измените изображение"
                )
                if (progress < 0.95f) {
                    CircularProgressIndicator(progress)
                }
            }
            TextField(
                modifier = Modifier.size(400.dp, 200.dp),
                value = data,
                onValueChange = { data = it }
            )
            StegoButton(
                onClick = {
                    coroutineScope.launch {
                        interpolatedPaths.clear()
                        outputPaths.clear()
                        progress = 0f
                        inputPaths.map {
                            progress += 1 / inputPaths.size
                            val inputFile = File(it)
                            val inputImage = ImageIO.read(inputFile)
                            val interpolatedImage = inputImage.interpolate()
                            val interpolatedFile = File(
                                inputFile.parentFile,
                                inputFile.nameWithoutExtension + "-interpolated." + inputFile.extension
                            )
                            ImageIO.write(interpolatedImage, interpolatedFile.extension, interpolatedFile)
                            val dataBytes = data.encodeToByteArray()
                            val outputImage = interpolatedImage.insertData(dataBytes)
                            for (byte in dataBytes) {
                                print("$byte ")
                            }
                            val outputFile = File(
                                inputFile.parentFile,
                                inputFile.nameWithoutExtension + "-output." + inputFile.extension
                            )
                            ImageIO.write(outputImage, outputFile.extension, outputFile)
                            outputPaths.add(outputFile.path)
                            mse.add(evaluateMSE(interpolatedImage, outputImage))
                        }
                    }
                },
                text = "Внедрить данные"
            )
            val imagesScrollState = rememberScrollState()
            if (interpolatedBitmaps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .horizontalScroll(imagesScrollState),
                ) {
                    interpolatedBitmaps.forEach {
                        Image(
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(400.dp)
                                .wrapContentHeight(),
                            bitmap = it,
                            contentDescription = null
                        )
                    }
                }
            }
            if (outputBitmaps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .horizontalScroll(imagesScrollState),
                ) {
                    interpolatedBitmaps.forEach {
                        Image(
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(400.dp)
                                .wrapContentHeight(),
                            bitmap = it,
                            contentDescription = null
                        )
                    }
                }
            }
            if (outputBitmaps.isNotEmpty() || interpolatedBitmaps.isNotEmpty()) {
                HorizontalScrollbar(
                    adapter = rememberScrollbarAdapter(imagesScrollState),
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(screenScrollState),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
        )
    }
}

@Composable
fun ExtractingScreen(
    parent: Frame
) {
    val screenScrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .verticalScroll(screenScrollState)
        ) {
            var inputPath by remember { mutableStateOf(null as String?) }
            var extractedData by remember { mutableStateOf(null as String?) }
            StegoButton(
                onClick = {
                    val selectedFile =
                        openFile(parent, if (inputPath == null) "Выберите изображение" else "Измените изображение")
                    if (selectedFile != null) {
                        inputPath = selectedFile
                    }
                },
                text = if (inputPath == null) "Выберите изображение" else "Измените изображение"
            )
            StegoButton(
                onClick = {
                    inputPath?.let {
                        val inputFile = File(it)
                        val inputImage = ImageIO.read(inputFile)
                        val extractedBytes = inputImage.extractData()
                        extractedData = extractedBytes.decodeToString()
                        for (byte in extractedBytes) {
                            print("$byte ")
                        }
                    }
                },
                text = "Извлечь данные"
            )
            extractedData?.let {
                Text(it)
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(screenScrollState),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight(),
        )
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