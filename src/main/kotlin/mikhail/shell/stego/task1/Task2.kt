package mikhail.shell.stego.task1

import javafx.application.Application
import javafx.stage.Stage

class Task2: Application() {
    override fun start(stage: Stage?) {
        DwmIntegratingApplication()
    }
}

fun main() {
    Application.launch(Task2::class.java)
}