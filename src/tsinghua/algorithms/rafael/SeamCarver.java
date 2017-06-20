package tsinghua.algorithms.rafael;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class which removes seams from an image specified in the arguments of the command line.
 *
 * @author Rafael da Silva Costa - 2015280364
 * @version 1.0
 */
class SeamCarver
{
	/**
	 * @param args Command line arguments in the form: "image.jpg 4 true" - where image.jpg is
	 *             the image name and 4 is the number of seams to be removed from the specified image
	 */
	public static void main(String[] args)
	{
		try
		{
			BufferedImage input = ImageIO.read(new File(args[0]));
			int n = Integer.parseInt(args[1]);
			boolean isHorizontal = Boolean.parseBoolean(args[2]);

			long start = System.currentTimeMillis();
			BufferedImage output = remove((isHorizontal) ? Utils.rotateImage(input, false) : input, n);
			long elapsed = System.currentTimeMillis() - start;

			String extension = args[0].substring(args[0].length() - 3);
			ImageIO.write((isHorizontal) ?
			              Utils.rotateImage(output, true) :
			              output, extension, new File("out_" + args[0]));
			System.out.println("Calculated in " + elapsed + "ms");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Uses seam carving to remove n columns from the image
	 *
	 * @param input Image to remove columns
	 * @param n     number of columns to be removed
	 * @return A new image that is n columns smaller than the input
	 * @throws IllegalArgumentException if the input image is invalid or the specified value of n is greater than the image width
	 */
	static BufferedImage remove(BufferedImage input, int n) throws IllegalArgumentException
	{
		int reducedWidth = input.getWidth() - 1;

		if (reducedWidth < 0)
		{
			throw new IllegalArgumentException(
					"The image must be at least one column wide");
		}
		else if (n >= input.getWidth())
		{
			throw new IllegalArgumentException(
					"The number of columns to be removed cannot be greater than the number of the input width");
		}

		return SeamFinder.carve(input, n);
	}
}