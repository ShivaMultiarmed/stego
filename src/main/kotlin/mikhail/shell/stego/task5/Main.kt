package mikhail.shell.stego.task5

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.stego.common.StegoButton
import mikhail.shell.stego.common.StegoProgressIndicator
import mikhail.shell.stego.common.openFiles
import java.awt.Frame
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs

fun main(args: Array<String>) = application {
    var checkedTabNumber by remember { mutableStateOf(0) }
    val analysisScreenTab by derivedStateOf { AnalysisScreenTab.entries[checkedTabNumber] }
    Window(
        onCloseRequest = ::exitApplication,
        title = analysisScreenTab.title,
    ) {
        Column {
            mikhail.shell.stego.common.TabRow(
                modifier = Modifier.fillMaxWidth(),
                checkedTabNumber = checkedTabNumber,
                tabs = AnalysisScreenTab.entries.associate { it.ordinal to it.title },
                onTabSwitch = {
                    checkedTabNumber = it
                }
            )
            when (analysisScreenTab) {
                AnalysisScreenTab.VISUAL_ATTACK -> VisualAttackScreen(window)
                AnalysisScreenTab.KHI_SQUARED -> KhiSquaredScreen(window)
                AnalysisScreenTab.RS_ANALYSIS -> RSAnalysisScreen(window)
                AnalysisScreenTab.AUMP -> AumpScreen(window)
            }
        }
    }
}

@Composable
fun VisualAttackScreen(
    frame: Frame
) {
    var progress by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()
    val inputPaths = remember { mutableStateListOf<String>() }
    val inputBitmaps by derivedStateOf {
        inputPaths.map {
            ImageIO.read(File(it)).toComposeImageBitmap()
        }
    }
    var bitNumber by remember { mutableStateOf(0f) }
    val outputBitmaps = remember { mutableStateListOf<ImageBitmap>() }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            StegoButton(
                text = "Выбрать файлы",
                onClick = {
                    val selectedFiles = openFiles(frame)
                    if (selectedFiles != null) {
                        inputPaths.clear()
                        selectedFiles.forEach(inputPaths::add)
                    }
                }
            )
            if (inputBitmaps.isNotEmpty()) {
                Slider(
                    modifier = Modifier.width(300.dp),
                    value = bitNumber,
                    onValueChange = {
                        bitNumber = it
                    },
                    valueRange = 0f..7f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(106, 162, 252),
                        activeTrackColor = Color(200, 219, 250),
                        inactiveTickColor = Color(240, 240, 240)
                    )
                )
                Text(bitNumber.toInt().toString() + "-й бит")
                StegoButton(
                    text = "Анализировать",
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            progress = 0f
                            outputBitmaps.clear()
                            inputPaths.forEach {
                                progress += 1f / inputPaths.size
                                val inputFile = File(it)
                                val inputImage = ImageIO.read(inputFile)
                                val outputImage = inputImage.visualAttack(bitNumber.toInt())
                                outputBitmaps.add(outputImage.toComposeImageBitmap())
                                delay(1000)
                            }
                        }
                    }
                )
            }
            if (progress < 0.9f) {
                StegoProgressIndicator(progress)
            }
        }
        val scrollState = rememberScrollState()
        if (inputBitmaps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                inputBitmaps.forEachIndexed { i, it ->
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
        }
        if (outputBitmaps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                outputBitmaps.forEach {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RSAnalysisScreen(
    frame: Frame
) {
    var progress by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
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
			stringBuilder.append("P = $it%.\n")
			if (it < 0.012f) {
				stringBuilder.append("В изображении нет встроенных данных")
			} else {
				stringBuilder.append("В изображении содержатся встроенные данные")
			}
			stringBuilder.toString()
		}
    }
    Column {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            StegoButton(
                text = "Выбрать файлы",
                onClick = {
                    val selectedFiles = openFiles(frame)
                    if (selectedFiles != null) {
                        inputPaths.clear()
                        selectedFiles.forEach(inputPaths::add)
                    }
                }
            )
            if (inputBitmaps.isNotEmpty()) {
                StegoButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            progress = 0f
                            p.clear()
                            val analyzer = RSAnalysis.getInstance()
                            inputPaths.forEach {
                                progress += 1f / inputPaths.size
                                val inputFile = File(it)
                                val inputImage = ImageIO.read(inputFile)
                                val results = analyzer.doAnalysis(inputImage, RSAnalysis.ANALYSIS_COLOUR_BLUE, true)
                                val Rp = results[0]
                                val Sp = results[1]
                                val Rn = results[2]
                                val Sn = results[3]
                                val currentP = abs((Rp - Sp) - (Rn - Sn)) / (Rp + Sp + Rn + Sn)
                                p.add(currentP.toFloat())
                            }
                            val uuid = UUID.randomUUID().toString()
                            val resultFile = File(inputPaths[0].substringBeforeLast("\\") + "\\rs-results-$uuid.txt")
                            resultFile.createNewFile()
                            resultFile.outputStream().bufferedWriter().use {
                                p.forEachIndexed { i, pValue ->
                                    val formattedResult = "%.5f".format(Locale.US, pValue)
                                    it.append("$i\t$formattedResult\n")
                                }
                            }
                        }
                    },
                    text = "Анализировать"
                )
            }
            if (progress < 0.95f) {
                StegoProgressIndicator(progress)
            }
        }
        if (inputBitmaps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                inputBitmaps.forEach {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
        }
        if (resultMsgs.isNotEmpty()) {
            Row (
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                resultMsgs.forEach {
                    Text(
                        modifier = Modifier.width(300.dp),
                        text = it
                    )
                }
            }
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun KhiSquaredScreen(frame: Frame) {
    val coroutineScope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(1f) }
    val scrollState = rememberScrollState()
    val inputPaths = remember { mutableStateListOf<String>() }
    val inputBitmaps by derivedStateOf {
        inputPaths.map {
            ImageIO.read(File(it)).toComposeImageBitmap()
        }
    }
    val khi = remember { mutableStateListOf<Double>() }
    val resultTexts by derivedStateOf {
        khi.map {
            val stringBuilder = StringBuilder()
            stringBuilder.append("Хи-квадрат равен $it.\n")
            if (it < 280) {
                stringBuilder.append("В изображении нет данных.\n")
            } else {
                stringBuilder.append("В изображении содержатся данные.\n")
            }
            stringBuilder.toString()
        }
    }
    Column {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            StegoButton(
                text = "Выбрать файлы",
                onClick = {
                    val selectedFiles = openFiles(frame)
                    if (selectedFiles != null) {
                        inputPaths.clear()
                        selectedFiles.forEach(inputPaths::add)
                    }
                }
            )
            if (inputBitmaps.isNotEmpty()) {
                StegoButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            progress = 0f
                            khi.clear()

                            inputPaths.forEach {
                                progress += 1f / inputPaths.size
                                val inputFile = File(it)
                                val inputImage = ImageIO.read(inputFile)
                                val inputPixels = inputImage.getBytes()
                                val inputMatrix = inputPixels.toMatrix(inputImage.width, inputImage.height)
                                val inputChunks = inputMatrix.chunk(16)
                                var resultKhi2 = 0.0
                                outer@ for (i in inputChunks.indices) {
                                    for (j in inputChunks[0].indices) {
                                        val inputChunk = inputChunks[i][j]
                                        val expected = inputChunk.evaluateExpectedColorFrequencies()
                                        val actual = inputChunk.evaluateActualColorFrequencies()
                                        val result = evaluateKhiSquared(expected, actual)
                                        resultKhi2 = result.first
                                        if (resultKhi2 > 100) {
                                            break@outer
                                        }
                                    }
                                }
                                khi.add(resultKhi2)
                            }
                            val uuid = UUID.randomUUID().toString()
                            val resultFile = File(inputPaths[0].substringBeforeLast("\\") + "\\chi2-results-$uuid.txt")
                            resultFile.createNewFile()
                            resultFile.outputStream().bufferedWriter().use {
                                khi.forEachIndexed { i, khi2 ->
                                    val formattedResult = "%.5f".format(Locale.US, khi2)
                                    it.append("$i\t$formattedResult\n")
                                }
                            }
                        }
                    },
                    text = "Анализировать"
                )
            }
            if (progress < 0.95f) {
                StegoProgressIndicator(progress)
            }
        }
        if (inputBitmaps.isNotEmpty()) {
            Row (
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                inputBitmaps.forEach {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
        }
        if (resultTexts.isNotEmpty()) {
            Row (
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                resultTexts.forEach {
                    Text(
                        modifier = Modifier.width(300.dp),
                        text = it
                    )
                }
            }
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AumpScreen(
    frame: Frame
) {
    val coroutineScope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(1f) }
    val scrollState = rememberScrollState()
    Column {
        val inputPaths = remember { mutableStateListOf<String>() }
        val inputBitmaps by derivedStateOf {
            inputPaths.map {
                ImageIO.read(File(it)).toComposeImageBitmap()
            }
        }
        val results = remember { mutableStateListOf<Double>() }
        val resultMessages by derivedStateOf {
            results.indices.map { i ->
                val stringBuilder = StringBuilder()
                stringBuilder.append("aump = ${results[i]}\n")
                if (results[i] < 0.98) {
                    stringBuilder.append("В изображении нет данных.\n")
                } else {
                    stringBuilder.append("В изображении присутствуют данные.\n")
                }
                stringBuilder.toString()
            }
        }
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            StegoButton(
                text = "Выбрать файлы",
                onClick = {
                    val selectedFiles = openFiles(frame)
                    if (selectedFiles != null) {
                        inputPaths.clear()
                        selectedFiles.forEach(inputPaths::add)
                    }
                }
            )
            if (inputBitmaps.isNotEmpty()) {
                StegoButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            progress = 0f
                            results.clear()
                            inputPaths.forEach {
                                progress += 1f / inputPaths.size
                                val inputFile = File(it)
                                val inputImage = ImageIO.read(inputFile)
                                val result = abs(aump(inputImage, 16, 2))
                                results.add(result)
                            }
                            val uuid = UUID.randomUUID().toString()
                            val resultFile = File(inputPaths[0].substringBeforeLast("\\") + "\\aump-results-$uuid.txt")
                            resultFile.createNewFile()
                            resultFile.outputStream().bufferedWriter().use {
                                results.forEachIndexed { i, result ->
                                    val formattedResult = "%.5f".format(Locale.US, result)
                                    it.append("$i\t$formattedResult\n")
                                }
                            }
                        }
                    },
                    text = "Анализировать"
                )
            }
            if (progress < 0.95f) {
                StegoProgressIndicator(progress)
            }
        }
        if (inputBitmaps.isNotEmpty()) {
            Row (
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                inputBitmaps.forEach {
                    Image(
                        modifier = Modifier.width(300.dp),
                        bitmap = it,
                        contentDescription = null
                    )
                }
            }
        }
        if (resultMessages.isNotEmpty()) {
            Row (
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                resultMessages.forEach {
                    Text(
                        modifier = Modifier.width(300.dp),
                        text = it
                    )
                }
            }
        }
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

enum class AnalysisScreenTab(val title: String) {
    VISUAL_ATTACK("Визуальная атака"),
    RS_ANALYSIS("RS-анализ"),
    KHI_SQUARED("Хи-квадрат"),
    AUMP("AUMP")
}

