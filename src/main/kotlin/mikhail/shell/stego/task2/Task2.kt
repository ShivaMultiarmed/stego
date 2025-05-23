package mikhail.shell.stego.task2

import javafx.application.Application
import javafx.stage.Stage

class Task2: Application() {
    override fun start(stage: Stage?) {
        KjbIntegratingApplication()
    }
}

fun main() {
    Application.launch(Task2::class.java)
}