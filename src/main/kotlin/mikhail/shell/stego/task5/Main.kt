package mikhail.shell.stego.task5

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import java.awt.Frame
import java.awt.image.BufferedImage
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
                Screen.KHI_SQUARED -> TODO()
                Screen.RS_ANALYSIS -> TODO()
                Screen.AUMP -> TODO()
            }
        }
    }
}

@Composable
fun TabRow(
    onTabSwitch: (screen: Screen) -> Unit
) {
    Row {
        Screen.values().forEach {
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
        Row {
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
                    modifier = Modifier.size(300.dp).background(Color.Black),
                    bitmap = it,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun RSAnalysisScreen(
    frame: Frame
) {
    var inputPath by remember { mutableStateOf(null as String?) }
    val inputBitmap = remember(inputPath) { inputPath?.let { ImageIO.read(File(it)).toComposeImageBitmap() } }
    Column {
        Button(
            onClick = {
                inputPath = openFile(frame)
            }
        ) {
            Text("Выбрать файл")
        }
        Row {
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
                        val analyzer = RSAnalysis(inputImage.width, inputImage.height)
                        val result = analyzer.doAnalysis(inputImage, 0, false)
                    }
                ) {
                    Text(
                        text = "Анализировать"
                    )
                }
            }
        }
    }
}

enum class Screen(val title: String) {
    VISUAL_ATTACK("Визуальная атака"),
    RS_ANALYSIS("RS-анализ"),
    KHI_SQUARED("Хи-квадрат"),
    AUMP("AUMP")
}