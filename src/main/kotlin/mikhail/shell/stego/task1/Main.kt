package mikhail.shell.stego.task1

import javafx.application.Application
import javafx.stage.Stage
import mikhail.shell.web.application.mikhail.shell.stego.InputStage

class Main: Application() {
    override fun start(stage: Stage?) {
        val inputStage = InputStage()
        inputStage.show()
    }
}

fun main() {
    Application.launch(Main::class.java)
}
