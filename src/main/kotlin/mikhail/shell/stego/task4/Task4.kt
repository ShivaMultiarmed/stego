package mikhail.shell.stego.task4

import java.io.File
import javax.imageio.ImageIO

fun main() {
    val inputFile = File("C:/Users/Mikhail_Shell/Desktop/121.bmp")
    val inputImg = ImageIO.read(inputFile)
    val interpolatedImg = inputImg.interpolate()
    val outputImg = interpolatedImg.insertData("Some string".encodeToByteArray())
    val outputFile = File(inputFile.parentFile, "${inputFile.nameWithoutExtension}-output.${inputFile.extension}")
    ImageIO.write(outputImg, inputFile.extension, outputFile)
    val extractedString = outputImg.extractData().decodeToString()
    println(extractedString)
}
