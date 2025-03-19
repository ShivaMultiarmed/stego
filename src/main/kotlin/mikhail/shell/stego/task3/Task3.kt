package mikhail.shell.stego.task3

import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val parentPath = "C:/Users/Mikhail_Shell/Desktop/"
    val projectPath = "src/main/kotlin/mikhail/shell/stego/task3/"

    val inputFile = File(parentPath, "1.png")
    val extension = inputFile.extension
    val inputImage = ImageIO.read(inputFile)

    File(projectPath, "inputBits.txt").printWriter().use { writer ->
        for (x in 0..<inputImage.width) {
            for (y in 0..<inputImage.height) {
                val pixel = inputImage.getRGB(x, y)
                val blueComponent = pixel.B
                for (i in 0..7) {
                    writer.write(blueComponent.getBit(i).toString())
                }
                writer.write("\n")
            }
        }
    }

    val dataFile = File("src/main/kotlin/mikhail/shell/stego/task3/data.txt")
    val dataString = dataFile.bufferedReader().use { it.readLines().joinToString("") }
    val byteData = dataString.toByteArray(Charsets.UTF_8)

    val decomposedBits = byteData.decompose()

    File(projectPath, "decomposedBits.txt").printWriter().use { writer ->
        decomposedBits.forEach { bitPair ->
            writer.write("[${bitPair[0]}, ${bitPair[1]}], ")
        }
    }

    val outputImage = inputImage.insertData(byteData)

    File(projectPath, "outputBits.txt").printWriter().use { writer ->
        for (x in 0..<outputImage.width) {
            for (y in 0..<outputImage.height) {
                val pixel = outputImage.getRGB(x, y)
                val blueComponent = pixel.B
                for (i in 0..7) {
                    writer.write(blueComponent.getBit(i).toString())
                }
                writer.write("\n")
            }
        }
    }
    val outputFile = File(parentPath, "2.$extension")
    ImageIO.write(outputImage, extension, outputFile)

    val extractedData = ImageIO.read(outputFile).extractData()
    val extractedString = extractedData.decodeToString()
    File(projectPath, "extractedData.txt").printWriter(Charsets.UTF_8).use {
        it.println(extractedString)
    }
}