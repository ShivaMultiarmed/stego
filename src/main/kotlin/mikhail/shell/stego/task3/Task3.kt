package mikhail.shell.stego.task3

import mikhail.shell.stego.task1.getBit
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val parentPath = "C:/Users/Mikhail_Shell/Desktop/"
    val projectPath = "src/main/kotlin/mikhail/shell/stego/task3/"
    val input = File(parentPath,"1.jpg")
    val bufferedInput = ImageIO.read(input)
    File(projectPath, "inputBits.txt").printWriter().use { writer ->
        for (x in 0..<bufferedInput.width) {
            for (y in 0..<bufferedInput.height) {
                val pixel = bufferedInput.getRGB(x, y)
                val Bcomponent = pixel.B
                for (i in 0..7) {
                    writer.write(Bcomponent.getBit(i).toString())
                }
                writer.write("\n")
            }
        }
    }
    val data = File("src/main/kotlin/mikhail/shell/stego/task3/data.txt")
        .bufferedReader()
        .use { it.readLines().joinToString("") }
    val dataBytes = data.toByteArray()
    val decomposedBits = dataBytes.decompose()
    File(projectPath, "decomposedBits.txt").printWriter().use { writer ->
        decomposedBits.forEach {
            writer.write("[${it[0]}, ${it[1]}], ")
        }
    }
    val bufferedOutput = bufferedInput.insertData(decomposedBits)
    ImageIO.write(bufferedOutput, "jpg", File(parentPath, "2.jpg"))
}