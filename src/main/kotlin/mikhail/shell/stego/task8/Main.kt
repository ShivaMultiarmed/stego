package mikhail.shell.stego.task8

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mikhail.shell.stego.common.StegoButton
import mikhail.shell.stego.common.decompose

fun main(args: Array<String>) = application {
    Window(
        onCloseRequest = ::exitApplication
    ) {
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
            Text(
                text = generatedText
            )
        }
    }
}