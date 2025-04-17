package mikhail.shell.stego.task5

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.launch
import mikhail.shell.stego.task5.aump.aump
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
                modifier = Modifier.fillMaxWidth(),
                currentTab = screen,
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
    modifier: Modifier = Modifier,
    currentTab: Screen,
    onTabSwitch: (screen: Screen) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Screen.entries.forEach { screen ->
            Tab(
                screen = screen,
                checked = currentTab == screen,
                onClick = {
                    onTabSwitch(screen)
                }
            )
        }
    }
}

@Composable
fun Tab(
    screen: Screen,
    checked: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (checked) Color.White else Color(230, 230, 230),
            backgroundColor = if (checked) Color(40, 83, 153) else Color.White
        ),
        shape = RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp)
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
                        progress = 0f
                        outputBitmaps.clear()
                        inputPaths.forEach {
                            progress += 1 / inputPaths.size
                            val inputFile = File(it)
                            val inputImage = ImageIO.read(inputFile)
                            val outputImage = inputImage.visualAttack(bitNumber.toInt())
                            outputBitmaps.add(outputImage.toComposeImageBitmap())
                        }
                    }
                )
            }
            CircularProgressIndicator(progress)
        }
        val scrollState = rememberScrollState()
        if (inputBitmaps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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
            stringBuilder.append("P = $it.\n")
            if (it > 0.2671) {
                stringBuilder.append("В изображении нет стегосообщения.\n")
            } else {
                stringBuilder.append("В изображении обнаружено стегосообщение.\n")
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
                        coroutineScope.launch {
                            progress = 0f
                            p.clear()
                            val analyzer = RSAnalysis.getInstance()
                            inputPaths.forEach {
                                progress += 1 / inputPaths.size
                                val inputFile = File(it)
                                val inputImage = ImageIO.read(inputFile)
                                val results = analyzer.doAnalysis(inputImage, RSAnalysis.ANALYSIS_COLOUR_BLUE, true)
                                val R = results[0]
                                val S = results[1]
                                p.add(((R - S) / (R + S)).toFloat())
                            }
                        }
                    },
                    text = "Анализировать"
                )
            }
            CircularProgressIndicator(progress)
        }
        if (inputBitmaps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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
            if (p[i] < 0.0038) {
                builder.append("В изображении возможно есть скрытая информация.\n")
            } else {
                builder.append("В изображении нет скрытой информации.\n")
            }
            builder.toString()
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
            CircularProgressIndicator(progress)
            if (inputBitmaps.isNotEmpty()) {
                StegoButton(
                    onClick = {
                        coroutineScope.launch {
                            progress = 0f
                            khi.clear()
                            df.clear()
                            inputPaths.forEach {
                                progress += 1 / inputPaths.size
                                val inputFile = File(it)
                                val inputImage = ImageIO.read(inputFile)
                                val expected = inputImage.evaluateExpectedColorFrequencies()
                                val actual = inputImage.evaluateActualColorFrequencies()
                                val result = evaluateKhiSquared(expected, actual)
                                khi.add(result.first)
                                df.add(result.second)
                            }
                        }
                    },
                    text = "Анализировать"
                )
            }
        }
        if (inputBitmaps.isNotEmpty()) {
            Row (
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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
                //stringBuilder.append("ws = ${ws[i]}\n")
                stringBuilder.append("sp = ${results[i]}\n")
                if (results[i] >= 0.0532) {
                    stringBuilder.append("Присутствуют данные")
                } else {
                    stringBuilder.append("Данных нет")
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
            CircularProgressIndicator(progress)
            if (inputBitmaps.isNotEmpty()) {
                StegoButton(
                    onClick = {
                        coroutineScope.launch {
                            progress = 0f
                            results.clear()
                            inputPaths.forEach {
                                progress += 1 / inputPaths.size
                                val inputFile = File(it)
                                val inputImage = ImageIO.read(inputFile)
                                results.add(aump(inputImage))
                            }
                        }
                    },
                    text = "Анализировать"
                )
            }
        }
        if (inputBitmaps.isNotEmpty()) {
            Row (
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(scrollState, true),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
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

enum class Screen(val title: String) {
    VISUAL_ATTACK("Визуальная атака"),
    RS_ANALYSIS("RS-анализ"),
    KHI_SQUARED("Хи-квадрат"),
    AUMP("AUMP")
}

@Preview
@Composable

fun StegoButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color.White,
            backgroundColor = Color(106, 162, 252)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text
        )
    }
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