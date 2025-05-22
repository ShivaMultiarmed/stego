package mikhail.shell.stego.task8

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mikhail.shell.stego.common.StegoButton
import mikhail.shell.stego.common.TabRow
import mikhail.shell.stego.common.decompose

fun main(args: Array<String>) = application {
    Window(
        onCloseRequest = ::exitApplication
    ) {
        var tabIndex by remember { mutableStateOf(0) }
        Column {
            TabRow(
                tabs = mapOf(
                    0 to "Внедрение",
                    1 to "Извлечение"
                ),
                checkedTabNumber = tabIndex,
                onTabSwitch = {
                    tabIndex = it
                }
            )
            if (tabIndex == 0) {
                InsertingScreen()
            } else {
                ExtractingScreen()
            }
        }
    }
}

@Composable
fun InsertingScreen() {
    var dataString by remember { mutableStateOf("") }
    var generatedText by remember { mutableStateOf("") }
    Column {
        TextField(
            value = dataString,
            onValueChange = {
                dataString = it
            }
        )
        StegoButton(
            text = "Генерировать",
            onClick = {
                val dataBits = dataString
                    .encodeToByteArray()
                    .toTypedArray()
                    .decompose()
                generatedText = generateString(
                    template = template,
                    bitSequence = dataBits,
                    maps = generationKey
                )
                println(
                    generatedText
                        .replace(zeroSymbol.toString(), "[ZERO]")
                        .replace(oneSymbol.toString(), "[ONE]")
                )
            }
        )
        TextField(
            value = generatedText,
            onValueChange = {},
            enabled = false
        )
    }
}

@Composable
fun ExtractingScreen() {
    var generatedText by remember { mutableStateOf("") }
    var data by remember { mutableStateOf(emptyArray<Byte>()) }
    val dataString by derivedStateOf { data.toByteArray().decodeToString() }
    Column {
        TextField(
            value = generatedText,
            onValueChange = {
                generatedText = it
            }
        )
        StegoButton(
            text = "Извлечь",
            onClick = {
                data = extractFromString(
                    template = template,
                    maps = generationKey,
                    string = generatedText
                )
            }
        )
        TextField(
            value = dataString,
            onValueChange = {},
            enabled = false
        )
    }
}