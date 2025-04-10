package mikhail.shell.stego.task5

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mikhail.shell.stego.task4.openFile
import mikhail.shell.stego.task5.aump.analyzeImage
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
    var inputPath by remember { mutableStateOf(null as String?) }
    val inputBitmap = remember(inputPath) { inputPath?.let { ImageIO.read(File(it)).toComposeImageBitmap() } }
    var outputBitmap by remember { mutableStateOf(null as ImageBitmap?) }
    Column {
        Button(
            onClick = {
                inputPath = openFile(frame)
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
                    val outputImage = inputImage.proccess()
                    outputBitmap = outputImage.toComposeImageBitmap()
                }
            ) {
                Text(
                    text = "Анализировать"
                )
            }
        }
        outputBitmap?.let {
            Image(
                modifier = Modifier.width(300.dp),
                bitmap = it,
                contentDescription = null
            )
        }
    }
}

@Composable
fun RSAnalysisScreen(
    frame: Frame
) {
    var inputPath by remember { mutableStateOf(null as String?) }
    val inputBitmap = remember(inputPath) { inputPath?.let { ImageIO.read(File(it)).toComposeImageBitmap() } }
    var p by remember { mutableStateOf(null as Float?) }
    val resultMessage = remember(p) {
        p?.let {
            if (it >= 0.05f) {
                "P = $it. В изображении нет стегосообщения."
            } else {
                "P = $it. В изображении обнаружено стегосообщение."
            }
        }
    }
    Column {
        Button(
            onClick = {
                inputPath = openFile(frame)
            }
        ) {
            Text("Выбрать файл")
        }
        inputBitmap?.let {
            Image(
                modifier = Modifier.size(300.dp),
                bitmap = it,
                contentDescription = null
            )
            Button(
                onClick = {
                    val inputFile = File(inputPath!!)
                    val inputImage = ImageIO.read(inputFile)
                    val analyzer = RSAnalysis.getInstance()
                    val results = analyzer.doAnalysis(inputImage, RSAnalysis.ANALYSIS_COLOUR_BLUE, false)
                    val R = results[0]
                    val S = results[1]
                    p = ((R - S) / (R + S)).toFloat()
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

@Composable
fun KhiSquaredScreen(window: Frame) {
    var inputPath by remember { mutableStateOf(null as String?) }
    val inputBitmap = remember(inputPath) { inputPath?.let { ImageIO.read(File(it)).toComposeImageBitmap() } }
    var khi by remember { mutableStateOf(null as Double?) }
    var df by remember { mutableStateOf(null as Int?) }
    val p = remember(khi) { khi?.let { evaluateP(it, df!!) } }
    val resultText = remember(khi, p) {
        val builder = StringBuilder()
        if (khi != null && p != null) {
            builder.append("Хи-квадрат равен $khi.")
            if (khi!! < 0.05) {
                builder.append("В изображении содержится скрытая информация.")
            } else {
                builder.append("В изображении нет скрытой информации.")
            }
        }
        if (builder.isNotBlank()) {
            builder.toString()
        } else {
            null
        }
    }
    Column {
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
                    val colorFrequencies = inputImage.evaluateColorFrequencies()
                    val result = colorFrequencies.evaluateKhiSquared()
                    khi = result.first
                    df = result.second
                }
            ) {
                Text(
                    text = "Анализировать"
                )
            }
        }
        resultText?.let {
            Text(
                text = it
            )
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
                if (sp!! > 0.4 && triples!! > 0.4 && ws!! > 0.4) {
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