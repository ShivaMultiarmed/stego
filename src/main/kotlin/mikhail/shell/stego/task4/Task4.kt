package mikhail.shell.stego.task4

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main() {
    val inputFile = File("C:/Users/Mikhail_Shell/Desktop/121.bmp")
    val inputImg = ImageIO.read(inputFile)
    val outputImg = inputImg.interpolate()
    val outputFile = File(inputFile.parentFile, "${inputFile.nameWithoutExtension}-interpolated.${inputFile.extension}")
    ImageIO.write(outputImg, inputFile.extension, outputFile)
}
