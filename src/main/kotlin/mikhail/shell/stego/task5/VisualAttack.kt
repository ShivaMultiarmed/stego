package mikhail.shell.stego.task5

import java.awt.image.BufferedImage


operator fun Int.get(n: Int): Int {
    return (this shr n) and 1
}

fun BufferedImage.visualAttack(n: Int = 0): BufferedImage {
    val outputImage = BufferedImage(width, height, type)
    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixel = getRGB(x, y)
            val newColor = if (pixel[n] == 1) 0xFFFFFFFF else 0xFF000000
            outputImage.setRGB(x, y, newColor.toInt())
        }
    }
    return outputImage
}