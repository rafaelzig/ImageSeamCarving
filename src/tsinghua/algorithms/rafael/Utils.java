package tsinghua.algorithms.rafael;

import javax.media.jai.PlanarImage;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Utilities class for manipulating images.
 *
 * @author Rafael da Silva Costa - 2015280364
 * @version 1.0
 */
class Utils
{
	/**
	 * Obtains the pixel data of the specified image object.
	 *
	 * @param image Image to extract the pixel data from
	 * @return 2D Array containing the pixel data of the image.
	 */
	static float[][] getImageData(PlanarImage image)
	{
		int width = image.getWidth();

		byte[] pixels = ((DataBufferByte) image.getData().getDataBuffer()).getData();
		float[][] data = new float[image.getHeight()][width];

		if (image.getColorModel().hasAlpha())
		{
			int pixelLength = 4;

			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength)
			{
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				data[row][col] = argb;
				col++;
				if (col == width)
				{
					col = 0;
					row++;
				}
			}
		}
		else
		{
			int pixelLength = 3;

			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength)
			{
				int argb = 0;
				argb += 0xff000000; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
				data[row][col] = argb;
				col++;
				if (col == width)
				{
					col = 0;
					row++;
				}
			}
		}

		return data;
	}

	/**
	 * Produces an output image which is the rotated input image.
	 *
	 * @param input       Input image to be rotated
	 * @param isClockwise Whether the rotation should be clockwise or anticlockwise
	 * @return Rotated image.
	 */
	static BufferedImage rotateImage(BufferedImage input, boolean isClockwise)
	{
		int width = input.getWidth();
		int height = input.getHeight();

		BufferedImage output = new BufferedImage(height, width, input.getType());

		double theta;

		if (isClockwise)
		{
			theta = Math.PI / 2;
		}
		else
		{
			theta = -Math.PI / 2;
		}

		AffineTransform transform = new AffineTransform();
		transform.translate(0.5 * height, 0.5 * width);
		transform.rotate(theta);
		transform.translate(-0.5 * width, -0.5 * height);

		return new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR).filter(input, output);
	}
}