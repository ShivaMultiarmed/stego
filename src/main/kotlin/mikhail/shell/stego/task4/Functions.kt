package mikhail.shell.stego.task4

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_BYTE_GRAY
import java.awt.image.DataBufferByte


fun BufferedImage.interpolate(): BufferedImage {
    val K = 2
    val inputBuffer = raster.dataBuffer as DataBufferByte
    val bytes = inputBuffer.data
    val initialBytes = Array(height) {  i ->
        Array(width) { j ->
            bytes[i * width + j].toInt() and 0xFF
        }
    }
    val newWidth = width * K
    val newHeight = height * K
    val newBytes = Array(newHeight) { Array(newWidth) { 0 } }
    for (i in 0 until newHeight) {
        for (j in 0 until newWidth) {
            val m = i / K
            val n = j / K
            if (i % K == 0 && j % K == 0) {
                newBytes[i][j] = initialBytes[m][n]
            } else if (i % K == 0) {
                newBytes[i][j] = when {
                    (n in 1..width - 2) -> (initialBytes[m][n - 1] + initialBytes[m][n + 1]) / K
                    (n >= 1) -> initialBytes[m][n - 1]
                    (n <= width -2) -> initialBytes[m][n + 1]
                    else -> 0
                }
            } else if (j % K == 0) {
                newBytes[i][j] = when {
                    (m in 1..height - 2) -> (initialBytes[m - 1][n] + initialBytes[m + 1][n]) / K
                    (m >= 1) -> initialBytes[m - 1][n]
                    (m <= width - 2) -> initialBytes[m + 1][n]
                    else -> 0
                }
            } else {
                var sum = newBytes[i - 1][j] + newBytes[i][j - 1]
                var count = 2
                if (m > 0 && n > 0) {
                    sum += initialBytes[m - 1][n - 1]
                    count++
                }
                newBytes[i][j] = sum / count
            }
        }
    }
    val output = BufferedImage(newWidth, newHeight, TYPE_BYTE_GRAY)
    val outputBuffer = output.raster.dataBuffer as DataBufferByte
    outputBuffer.data.forEachIndexed { i, _ ->
        outputBuffer.data[i] = newBytes[i / newWidth][i % newWidth].toByte()
    }
    return output
}