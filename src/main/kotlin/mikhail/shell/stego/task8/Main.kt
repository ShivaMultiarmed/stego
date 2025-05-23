package mikhail.shell.stego.task8

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mikhail.shell.stego.common.StegoButton
import mikhail.shell.stego.common.StegoTextField
import mikhail.shell.stego.common.TabRow

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
    var containerText by remember { mutableStateOf("") }
    var stegoText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    Column (
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        StegoTextField(
            modifier = Modifier.size(400.dp, 250.dp),
            label = "Данные для внедрения",
            value = dataString,
            onValueChange = {
                dataString = it
            }
        )
        StegoTextField(
            label = "Текст-контейнер",
            modifier = Modifier.size(400.dp, 250.dp),
            value = containerText,
            onValueChange = {
                containerText = it
            }
        )
        StegoButton(
            text = "Внедрить",
            onClick = {
                val dataBytes = dataString
                    .encodeToByteArray()
                    .toTypedArray()
                stegoText = containerText.insert(dataBytes)
            }
        )
        StegoTextField(
            label = "Стеготекст",
            modifier = Modifier.size(400.dp, 250.dp),
            value = stegoText,
            onValueChange = {},
            enabled = false
        )
    }
    VerticalScrollbar(
        modifier = Modifier.fillMaxHeight(),
        adapter = rememberScrollbarAdapter(scrollState)
    )
}

@Composable
fun ExtractingScreen() {
    var stegoText by remember { mutableStateOf("") }
    var data by remember { mutableStateOf(emptyArray<Byte>()) }
    val dataString by derivedStateOf { data.toByteArray().decodeToString() }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        StegoTextField(
            label = "Стеготекст",
            modifier = Modifier.size(400.dp, 250.dp),
            value = stegoText,
            onValueChange = {
                stegoText = it
            }
        )
        StegoButton(
            text = "Извлечь",
            onClick = {
                data = stegoText.extract()
            }
        )
        StegoTextField(
            modifier = Modifier.size(400.dp, 250.dp),
            label = "Извлечённые данные",
            value = dataString,
            onValueChange = {},
            enabled = false
        )
    }
    VerticalScrollbar(
        modifier = Modifier.fillMaxHeight(),
        adapter = rememberScrollbarAdapter(scrollState)
    )
}