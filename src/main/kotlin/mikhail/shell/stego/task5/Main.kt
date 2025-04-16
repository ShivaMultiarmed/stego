package mikhail.shell.stego.task5

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mikhail.shell.stego.task4.openFile
import mikhail.shell.stego.task5.aump.analyzeImage
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) = application {
    var screen by remember { mutableStateOf(Screen.VISUAL_ATTACK) }
    Window(
        onCloseRequest = ::exitApplication,
        title = screen.title,
    ) {
        Column {
            TabRow(
                onTabSwitch = {
                    screen = it
                }
            )
            when (screen) {
                Screen.VISUAL_ATTACK -> VisualAttackScreen(window)
                Screen.KHI_SQUARED -> KhiSquaredScreen(window)
                Screen.RS_ANALYSIS -> RSAnalysisScreen(window)
                Screen.AUMP -> AumpScreen(window)
            }
        }
    }
}

@Composable
fun TabRow(
    onTabSwitch: (screen: Screen) -> Unit
) {
    Row {
        Screen.entries.forEach {
            Tab(
                screen = it,
                onClick = {
                    onTabSwitch(it)
                }
            )
        }
    }
}

@Composable
fun Tab(screen: Screen, onClick: () -> Unit) {
    Button(
        onClick = onClick
    ) {
        Text(
            text = screen.title
        )
    }
}

@Composable
fun VisualAttackScreen(
    frame: Frame
) {
    val inputPaths = remember { mutableStateListOf<String>() }
    val inputBitmaps by derivedStateOf {
        inputPaths.map {
            ImageIO.read(File(it)).toComposeImageBitmap()
        }
    }
    val outputBitmaps = remember { mutableStateListOf<ImageBitmap>() }
    Column {
        Button(
            onClick = {
                val selectedFiles = openFiles(frame)
                if (selectedFiles != null) {
                    inputPaths.clear()
                    selectedFiles.forEach(inputPaths::add)
                }
            }
        ) {
            Text("Выбрать файлы")
        }
        if (inputBitmaps.isNotEmpty()) {
            Row {
                inputBitmaps.forEachIndexed { i, it ->
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
            Button(
                onClick = {
                    outputBitmaps.clear()
                    inputPaths.forEach {
                        val inputFile = File(it)
                        val inputImage = ImageIO.read(inputFile)
                        val outputImage = inputImage.proccess()
                        outputBitmaps.add(outputImage.toComposeImageBitmap())
                    }
                }
            ) {
                Text(
                    text = "Анализировать"
                )
            }
        }
        if (outputBitmaps.isNotEmpty()) {
            Row {
                outputBitmaps.forEach {
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

@Composable
fun RSAnalysisScreen(
    frame: Frame
) {
    val inputPaths = remember { mutableStateListOf<String>() }
    val inputBitmaps by derivedStateOf {
        inputPaths.map {
            ImageIO.read(File(it)).toComposeImageBitmap()
        }
    }
    val p = remember { mutableStateListOf<Float>() }
    val resultMsgs by derivedStateOf {
        p.map {
            val stringBuilder = StringBuilder()
            stringBuilder.append("P = $it.\n")
            if (it > 0.25725) {
                stringBuilder.append("В изображении нет стегосообщения.\n")
            } else {
                stringBuilder.append("В изображении обнаружено стегосообщение.\n")
            }
            stringBuilder.toString()
        }
    }
    Column {
        Button(
            onClick = {
                val selectedFiles = openFiles(frame)
                if (selectedFiles != null) {
                    inputPaths.clear()
                    selectedFiles.forEach(inputPaths::add)
                }
            }
        ) {
            Text("Выбрать файлы")
        }
        if (inputBitmaps.isNotEmpty()) {
            Row {
                inputBitmaps.forEach {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
            Button(
                onClick = {
                    p.clear()
                    val analyzer = RSAnalysis.getInstance()
                    inputPaths.forEach {
                        val inputFile = File(it)
                        val inputImage = ImageIO.read(inputFile)
                        val results = analyzer.doAnalysis(inputImage, RSAnalysis.ANALYSIS_COLOUR_BLUE, false)
                        val R = results[0]
                        val S = results[1]
                        p.add(((R - S) / (R + S)).toFloat())
                    }
                }
            ) {
                Text(
                    text = "Анализировать"
                )
            }
        }
        if (resultMsgs.isNotEmpty()) {
            Row {
                resultMsgs.forEach {
                    Text(
                        modifier = Modifier.width(300.dp),
                        text = it
                    )
                }
            }
        }
    }
}

@Composable
fun KhiSquaredScreen(window: Frame) {
    val inputPaths = remember { mutableStateListOf<String>() }
    val inputBitmaps by derivedStateOf {
        inputPaths.map {
            ImageIO.read(File(it)).toComposeImageBitmap()
        }
    }
    val khi = remember { mutableStateListOf<Double>() }
    val df = remember { mutableStateListOf<Int>() }
    val p by derivedStateOf {
        khi.indices.map { i ->
            evaluateP(khi[i], df[i])
        }
    }
    val resultTexts by derivedStateOf {
        p.indices.map { i ->
            val builder = StringBuilder()
            builder.append("Хи-квадрат равен ${khi[i]}.\n")
            builder.append("P равен ${p[i]}.\n")
            if (p[i] <= 0.006) {
                builder.append("В изображении содержится скрытая информация.\n")
            } else {
                builder.append("В изображении нет скрытой информации.\n")
            }
            builder.toString()
        }
    }
    Column {
        Button(
            onClick = {
                val selectedFilePaths = openFiles(window)
                if (selectedFilePaths != null) {
                    inputPaths.clear()
                    selectedFilePaths.forEach(inputPaths::add)
                }
            }
        ) {
            Text("Выбрать файлы")
        }
        if (inputBitmaps.isNotEmpty()) {
            Row {
                inputBitmaps.forEach {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
            Button(
                onClick = {
                    khi.clear()
                    df.clear()
                    inputPaths.forEach {
                        val inputFile = File(it)
                        val inputImage = ImageIO.read(inputFile)
                        val expected = inputImage.evaluateExpectedColorFrequencies()
                        val actual = inputImage.evaluateActualColorFrequencies()
                        val result = evaluateKhiSquared(expected, actual)
                        khi.add(result.first)
                        df.add(result.second)
                    }
                }
            ) {
                Text(
                    text = "Анализировать"
                )
            }
        }
        if (resultTexts.isNotEmpty()) {
            Row {
                resultTexts.forEach {
                    Text(
                        modifier = Modifier.width(300.dp),
                        text = it
                    )
                }
            }
        }
    }
}

@Composable
fun AumpScreen(
    window: Frame
) {
    Column {
        var inputPath by remember { mutableStateOf(null as String?) }
        val inputBitmap = remember(inputPath) { inputPath?.let { ImageIO.read(File(it)).toComposeImageBitmap() } }
        var sp by remember { mutableStateOf(null as Double?) }
        var triples by remember { mutableStateOf(null as Double?) }
        var ws by remember { mutableStateOf(null as Double?) }
        val resultMessage = remember(sp, triples, ws) {
            if (sp != null && triples != null && ws != null) {
                val stringBuilder = StringBuilder()
                stringBuilder.append("sp = $sp\n")
                stringBuilder.append("triples = $triples\n")
                stringBuilder.append("ws = $ws\n")
                if (sp!! >= 0.064875) {
                    stringBuilder.append("Высокая вероятность, что встроены данные")
                } else {
                    stringBuilder.append("Данных скорее всего нет")
                }
                stringBuilder.toString()
            } else null
        }
        Button(
            onClick = {
                inputPath = openFile(window)
            }
        ) {
            Text("Выбрать файл")
        }
        inputBitmap?.let {
            Image(
                modifier = Modifier.width(300.dp),
                bitmap = it,
                contentDescription = null
            )
            Button(
                onClick = {
                    val inputFile = File(inputPath!!)
                    val inputImage = ImageIO.read(inputFile)
                    val analysisResults = analyzeImage(inputImage)[2] // беру только синий канал
                    sp = analysisResults[0]
                    triples = analysisResults[1]
                    ws = analysisResults[2]
                }
            ) {
                Text(
                    text = "Анализировать"
                )
            }
        }
        resultMessage?.let {
            Text(
                text = it
            )
        }
    }
}

enum class Screen(val title: String) {
    VISUAL_ATTACK("Визуальная атака"),
    RS_ANALYSIS("RS-анализ"),
    KHI_SQUARED("Хи-квадрат"),
    AUMP("AUMP")
}

fun openFiles(
    parent: Frame,
    title: String = "Выберите файлы"
): List<String>? {
    val dialog = FileDialog(parent, title, FileDialog.LOAD)
    dialog.isMultipleMode = true
    dialog.isVisible = true
    return dialog.files.takeIf { it.isNotEmpty() }?.toList()?.map { it.absolutePath }
}