package mikhail.shell.stego.task4

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val inputFile = File("C:/Users/Mikhail_Shell/Desktop/121.bmp")
    //val inputFile = File("C:/Users/Mikhail_Shell/Desktop/200.bmp")
    val inputImg = ImageIO.read(inputFile)
    //val inputImg = BufferedImage(2, 2, TYPE_BYTE_GRAY)
//    val bytes = (inputImg.raster.dataBuffer as DataBufferByte).data
//    bytes.forEachIndexed { i, _ ->
//        bytes[i] = arrayOf(100.toByte(), 120.toByte(), 130.toByte(), 140.toByte())[i]
//    }
    val interpolatedImg = inputImg.interpolate()
    val outputImg = interpolatedImg.insertData("Some string".encodeToByteArray())
    val outputFile = File(inputFile.parentFile, "${inputFile.nameWithoutExtension}-output.${inputFile.extension}")
    ImageIO.write(outputImg, inputFile.extension, outputFile)
    val extractedString = outputImg.extractData().decodeToString()
    println(extractedString)
}
