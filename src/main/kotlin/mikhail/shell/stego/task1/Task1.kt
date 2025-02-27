package mikhail.shell.stego.task1

import javafx.application.Application
import javafx.stage.Stage

class Task1: Application() {
    override fun start(stage: Stage?) {
        val visualAttackingApplication = VisualAttackingApplication()
        visualAttackingApplication.show()
    }
}

fun main() {
    Application.launch(Task1::class.java)
}