package mikhail.shell.stego.task5.histogram

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.koalaplot.core.bar.*
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*
import mikhail.shell.stego.task4.openFile
import mikhail.shell.stego.task5.Screen
import mikhail.shell.stego.task5.StegoButton
import mikhail.shell.stego.task5.TabRow
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalKoalaPlotApi::class)
fun main(args: Array<String>) = application {
    val coroutineScope = rememberCoroutineScope()
    Window(
        onCloseRequest = ::exitApplication
    ) {
        var tab by remember { mutableStateOf(Screen.AUMP) }
        var inputPath by remember { mutableStateOf("") }
        val scrollState = rememberScrollState()
        val histogramEntries = remember { mutableStateListOf<Pair<Float,Int>>() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TabRow(
                currentTab = tab,
                onTabSwitch = {
                    tab = it
                    histogramEntries.clear()
                }
            )
            Row {
                StegoButton(
                    text = "Выбрать файл",
                    onClick = {
                        val selectedFile = openFile(window)
                        if (selectedFile != null) {
                            inputPath = selectedFile
                        }
                    }
                )
                if (inputPath.isNotEmpty()) {
                    StegoButton(
                        text = "Построить диаграмму",
                        onClick = {
                            //coroutineScope.launch(Dispatchers.IO) {
                                histogramEntries.clear()
                                val input = File(inputPath).readLines().map { it.split("\t")[1].toFloat() }
                                val newResults = evaluateHistEntries(
                                    data = input,
                                    min = when (tab) {
                                        Screen.RS_ANALYSIS -> 0f
                                        Screen.KHI_SQUARED -> 10f
                                        Screen.AUMP -> -1.5f
                                        else -> return@StegoButton
                                    },
                                    max = when (tab) {
                                        Screen.RS_ANALYSIS -> 0.5f
                                        Screen.KHI_SQUARED -> 700f
                                        Screen.AUMP -> 1.5f
                                        else -> return@StegoButton
                                    },
                                    precision = 10
                                )
                                println()
                                newResults.forEach(histogramEntries::add)
                            //}
                        }
                    )
                }
            }
            if (histogramEntries.isNotEmpty()) {
                XYGraph(
                    modifier = Modifier
                        .width(1000.dp)
                        .height(600.dp),
                    xAxisModel = FloatLinearAxisModel(
                        range = when (tab) {
                            Screen.RS_ANALYSIS -> 0f..0.5f
                            Screen.KHI_SQUARED -> 0f..1000f
                            Screen.AUMP -> -1.7f..1.7f
                            else -> return@Column
                        }
                    ),
                    yAxisModel = IntLinearAxisModel(
                        range = 0..(1.2 * histogramEntries.maxOf { it.second }).roundToInt()
                    )
                ) {
                    val data = buildList<VerticalBarPlotEntry<Float, Int>> {
                        histogramEntries.forEach {
                            add(DefaultVerticalBarPlotEntry(it.first, DefaultVerticalBarPosition(0, it.second)))
                        }
                    }
                    VerticalBarPlot(
                        data = data,
                        bar = {
                            DefaultVerticalBar(
                                brush = SolidColor(Color.Blue),
                            )
                        },
                        barWidth = 1f
                    )
                }
            }
//            XYGraph(
//                modifier = Modifier
//                    .width(300.dp)
//                    .height(600.dp),
//                xAxisModel = FloatLinearAxisModel(
//                    range = 0f..10f
//                ),
//                yAxisModel = FloatLinearAxisModel(
//                    range = -1f..1f
//                )
//            ) {
//                val data = List<Point<Float, Float>>(100) { i ->
//                    DefaultPoint(i / 10f, sin(i / 10f))
//                }
//                LinePlot(
//                    data = data,
//                    lineStyle = LineStyle(brush = SolidColor(Color.Blue))
//                )
//            }
        }
    }
}

fun evaluateHistEntries(
    data: List<Float>,
    min: Float = data.min(),
    max: Float =  data.max(),
    precision: Int = 50
): List<Pair<Float, Int>> {
    val step = (max - min) / precision
    return IntArray(precision) { i ->
        data.count {
            val start = min + i * step
            val end = min + (i + 1) * step
            it in start ..< end
        }
    }.withIndex().associate { it.index * step + min to it.value }.toList()
}