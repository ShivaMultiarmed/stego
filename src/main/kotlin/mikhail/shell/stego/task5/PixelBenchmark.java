package mikhail.shell.stego.task5;

public class PixelBenchmark{

    /**
     * Gets the red content of a pixel.
     *
     * @param pixel The pixel to get the red content of.
     * @return The red content of the pixel.
     */
    public int getRed(int pixel){
        return ((pixel >> 16) & 0xff);
    }

    /**
     * Gets the green content of a pixel.
     *
     * @param pixel The pixel to get the green content of.
     * @return The green content of the pixel.
     */
    public int getGreen(int pixel){
        return ((pixel >> 8) & 0xff);
    }

    /**
     * Gets the blue content of a pixel.
     *
     * @param pixel The pixel to get the blue content of.
     * @return The blue content of the pixel.
     */
    public int getBlue(int pixel){
        return (pixel & 0xff);
    }

}