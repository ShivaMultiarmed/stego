package mikhail.shell.stego.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jdk.jfr.Enabled
import java.awt.FileDialog
import java.awt.Frame

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

@Preview
@Composable
fun StegoProgressIndicator(
    progress: Float
) {
    CircularProgressIndicator(
        progress = progress,
        backgroundColor = Color.Gray,
        color = Color(106, 162, 252)
    )
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

@Composable
fun TabRow(
    modifier: Modifier = Modifier,
    tabs: Map<Int, String>,
    checkedTabNumber: Int,
    onTabSwitch: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        tabs.entries.forEach { tab ->
            Tab(
                title = tab.value,
                checked = checkedTabNumber == tab.key,
                onClick = {
                    onTabSwitch(tab.key)
                }
            )
        }
    }
}

@Composable
fun Tab(
    title: String,
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
            text = title
        )
    }
}

@Composable
fun StegoTextField(
    modifier: Modifier = Modifier,
    label: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    OutlinedTextField(
        label = {
            if (label != null) {
                Text ( text = label)
            }
        },
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color(20, 20, 20),
            focusedBorderColor = Color(106, 162, 252),
            focusedLabelColor = Color(106, 162, 252),
            backgroundColor = Color(240, 240, 240),
            unfocusedBorderColor = Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp),
        enabled = enabled
    )
}