package mikhail.shell.stego.task4

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.BodyContentHandler
import java.io.File
import javax.imageio.ImageIO

val File.metaData: Metadata
    get() {
        val metadata = Metadata()
        val parser = AutoDetectParser()
        inputStream().use {
            parser.parse(it, BodyContentHandler(), metadata)
        }
        return metadata
    }

val File.metaDataLength: Long
    get() {
        return metaData.names().sumOf { it.length.toLong() + (metaData.get(it)?.length?.toLong() ?: 0L) }
    }

val File.contentLength: Long
    get() = length() - metaDataLength

val File.dimensions: Pair<Int, Int>
    get() {
        val bufferedImage = ImageIO.read(this)
        return bufferedImage.width to bufferedImage.height
    }

fun File.interpolate(K: Int = 2): File {
    val width = dimensions.first
    val height = dimensions.second
    val metadataLength = metaDataLength
    var metadataPassed = false
    val initialBytes = Array(height) { Array(width) { 0 } }
    inputStream().use {  input ->
        var byteReadCount = 0
        var readByte: Int
        while (input.read().also { readByte = it } != -1) {
            if (byteReadCount >= metadataLength && !metadataPassed) {
                byteReadCount -= metadataLength.toInt()
                metadataPassed = true
            }
            if (metadataPassed) {
                val (x, y) = (byteReadCount / width) to (byteReadCount % width)
                initialBytes[y][x] = readByte
            }
            byteReadCount++
        }
    }
    val newWidth = width * K
    val newHeight = height * K
    val newBytes = Array(newHeight) { Array(newWidth) { 0 } }
    for (m in 0..<height) {
        for (n in 0..<width) {
            newBytes[m * K][n * K] = initialBytes[m][n]
            if (n in 1..width - 2) {
                val left = initialBytes[m][n - 1]
                val right = initialBytes[m][n + 1]
                for (k in 1 until K) {
                    newBytes[m * K][n * K + k] = (left + right) / 2
                }
            }
            if (m in 1..height - 2) {
                val top  = initialBytes[m - 1][n]
                val bottom = initialBytes[m + 1][n]
                for (k in 1 until K) {
                    newBytes[m * K + k][n * K] = (top + bottom) / 2
                }
            }
        }
    }
    val outputFile = File(this.parentFile, "$nameWithoutExtension-interpolated.$extension")
    outputFile.outputStream().use { output ->
        inputStream().use { input ->
            output.write(input.readNBytes(metadataLength.toInt())) // копируем метаданные
        }
        for (m in 0..<height) {
            for (n in 0..<width) {
                val byte = newBytes[m][n]
                output.write(byte)
            }
        }
    }
    return outputFile
}