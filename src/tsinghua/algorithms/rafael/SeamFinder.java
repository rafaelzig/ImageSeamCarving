package tsinghua.algorithms.rafael;

import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Class which is used to find and remove the minimum energy vertical seam in an image
 *
 * @author Rafael da Silva Costa - 2015280364
 * @version 1.0
 */
class SeamFinder
{
	/**
	 * Uses seam carving to remove n columns from the image
	 *
	 * @param image Image to remove columns from
	 * @param n     number of columns to be removed
	 * @return A new image that is n columns smaller than the input
	 */
	static BufferedImage carve(BufferedImage image, int n)
	{
		// Creates and obtains the gradient of the image.
		PlanarImage gradientImage = JAI.create("gradientmagnitude", image, KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL, KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL);

		float[][] data = Utils.getImageData(gradientImage);
		float[][] seams = calculateSeams(data);
		int[] minSeam = findMin(data, seams);
		image = removeSeam(image, minSeam);

		for (int i = 1; i < n; i++)
		{
			data = removeSeam(data, minSeam);
			seams = removeSeam(seams, minSeam);
			recalculateSeams(data, seams, minSeam);
			minSeam = findMin(data, seams);
			image = removeSeam(image, minSeam);
		}

		return image;
	}

	/**
	 * Recalculates the required values of the dynamic programming table from a previous iteration.
	 *
	 * @param data    Gradient data to be used during calculations
	 * @param seams   Dynamic programming table containing the costs to remove pixels
	 * @param minSeam Minimum vertical seam in the gradient data of the image
	 */
	private static void recalculateSeams(float[][] data, float[][] seams, int[] minSeam)
	{
		int height = data.length;
		int width = data[0].length;
		int begin;
		int end;

		if (minSeam[0] == 0)
		{
			begin = minSeam[0];
			end = minSeam[0];
		}
		else if (minSeam[0] == width)
		{
			begin = minSeam[0] - 1;
			end = minSeam[0] - 1;
		}
		else
		{
			begin = minSeam[0] - 1;
			end = minSeam[0];
		}

		for (int row = 1; row < height; row++)
		{
			for (int col = begin; col <= end; col++)
			{
				seams[row][col] = calculateCell(data, seams, row, col);
			}

			if (begin > 0)
			{
				begin--;
			}
			if (end < width - 1)
			{
				end++;
			}
		}
	}

	/**
	 * Calculates the values of the dynamic programming table.
	 *
	 * @param data Gradient data to be used during calculations
	 */
	private static float[][] calculateSeams(float[][] data)
	{
		int height = data.length;
		int width = data[0].length;

		float[][] seams = new float[height][width];

		for (int row = 0; row < height; row++)
		{
			for (int col = 0; col < width; col++)
			{
				seams[row][col] = calculateCell(data, seams, row, col);
			}
		}

		return seams;
	}

	/**
	 * Calculates the minimum of the possible previous seam to fill in a cell of the current
	 * seam. Takes previous seam weight and then adds the value at seams[row][col].
	 *
	 * @param data  Gradient data to be used during calculations
	 * @param seams Dynamic programming table containing the costs to remove pixels
	 * @param row   Row to be examined
	 * @param col   Column to be examined
	 * @return Minimum cost of the possible previous seams
	 */
	private static float calculateCell(float[][] data, float[][] seams, int row, int col)
	{
		// initlizes first row
		if (row == 0)
		{
			return data[row][col];
		}

		// checks for single col data
		if (col == 0 && col == seams[0].length - 1)
		{
			return data[row][col] + seams[row - 1][col];
		}

		// if on the left edge does not consider going to the left
		if (col == 0)
		{
			return data[row][col] + Math.min(seams[row - 1][col], seams[row - 1][col + 1]);
		}

		// if on the right edge does not consider going to the right
		if (col == seams[0].length - 1)
		{
			return data[row][col] + Math.min(seams[row - 1][col], seams[row - 1][col - 1]);
		}

		// otherwise looks at the left, center, and right possibilities as mins
		return data[row][col] + Math.min(seams[row - 1][col - 1], Math.min(seams[row - 1][col], seams[row - 1][col + 1]));
	}

	/**
	 * Finds the minimum vertical seam in the gradient data of the image. A seam is a connected
	 * line through the image.
	 *
	 * @param seams The cost of removing each pixel in the image
	 * @param data  Gradient data to be used during calculations
	 * @return Array of the same height as the image where each entry specifies,
	 * for a single row, which column to remove.
	 */
	private static int[] findMin(float[][] data, float[][] seams)
	{
		int[] minSeam = new int[data.length];
		int lastRow = data.length - 1;

		// finds the column of the last row of the minimum seam
		int col = findEndPoint(seams);
		minSeam[(lastRow)] = col;

		// Starting from the last row of the DP table, the minSeam is found
		for (int row = lastRow; row > 0; row--)
		{
			minSeam[row] = col;
			col = findNextPoint(seams, row, col);
		}

		minSeam[0] = col;

		return minSeam;
	}

	/**
	 * Performs a linear search on the bottom row of table of seams to determine
	 * smallest seam.
	 *
	 * @param seams Dynamic programming table containing the costs to remove pixels
	 * @return The index of the smallest seam at the bottom row
	 */
	private static int findEndPoint(float[][] seams)
	{
		int lastRow = seams.length - 1;

		int minIndex = 0;
		float minValue = seams[lastRow][0];

		for (int col = 1; col < seams[0].length; col++)
		{
			if (seams[lastRow][col] < minValue)
			{
				minIndex = col;
				minValue = seams[lastRow][col];
			}
		}

		return minIndex;
	}

	/**
	 * From a certain (row,col) in the table seams, determines the previous smallest
	 * path and returns which column it is in.
	 *
	 * @param seams Dynamic programming table containing the costs to remove pixels
	 * @param row   Row to be examined
	 * @param col   Column to be examined
	 * @return Previous smallest column in the dynamic table of costs.
	 */
	private static int findNextPoint(float[][] seams, int row, int col)
	{
		int width = seams[row].length;

		// if at the left border only considers above and above to the right
		if (col == 0)
		{
			return (seams[row - 1][0] <= seams[row - 1][1]) ? 0 : 1;
		}

		// if at the right border only considers above and above to the left
		if (col == width - 1)
		{
			return (seams[row - 1][width - 2] <= seams[row - 1][width - 1]) ? width - 2 : width - 1;
		}

		// otherwise looks at above left, center, and right
		return checkAbove(seams, row, col);
	}

	/**
	 * Determines which column leads to smallest seam for a certain
	 * (row,col) and returns the column
	 *
	 * @param seams Dynamic programming table containing the costs to remove pixels
	 * @param row   Row to be examined
	 * @param col   Column to be examined
	 * @return Index of the smallest seam of the cells above.
	 */
	private static int checkAbove(float[][] seams, int row, int col)
	{
		int minIndex = col - 1;
		float minValue = seams[row - 1][col - 1];

		for (int i = 1; i < 3; i++)
		{
			if (seams[row - 1][col - 1 + i] < minValue)
			{
				minValue = seams[row - 1][col - 1 + i];
				minIndex = col - 1 + i;
			}
		}

		return minIndex;
	}

	/**
	 * Removes the smallest cost seam from the image data.
	 *
	 * @param data    Gradient data to be used during calculations
	 * @param minSeam Minimum vertical seam in the gradient data of the image
	 * @return Input data with the minimum cost seam removed.
	 */
	private static float[][] removeSeam(float[][] data, int[] minSeam)
	{
		int outputWidth = data[0].length - 1;
		float[][] output = new float[data.length][outputWidth];

		// Now fill in the new image one row at a time
		for (int row = 0; row < minSeam.length; row++)
		{
			int col = minSeam[row];

			if (col > 0)
			{
				// There are pixels to copy to the left of the minSeam
				System.arraycopy(data[row], 0, output[row], 0, col);
			}

			if (col < outputWidth)
			{
				// There are pixels to copy to the right of the minSeam
				int colsAfter = outputWidth - col;
				System.arraycopy(data[row], col + 1, output[row], col, colsAfter);
			}
		}

		return output;
	}

	/**
	 * Removes the smallest cost seam from the input image.
	 *
	 * @param input   Image to remove the seam from
	 * @param minSeam Minimum vertical seam in the gradient data of the image
	 * @return Input image with the minimum cost seam removed.
	 */
	private static BufferedImage removeSeam(BufferedImage input, int[] minSeam)
	{
		int outputWidth = input.getWidth() - 1;

		// Create the return input
		BufferedImage output = new BufferedImage(outputWidth, input.getHeight(), input.getType());
		WritableRaster outputData = output.getRaster();

		// Now fill in the new input one row at a time
		Raster inputData = input.getData();
		float[] auxiliary = new float[input.getWidth() * 3];

		for (int row = 0; row < minSeam.length; row++)
		{
			int col = minSeam[row];
			if (col > 0)
			{
				// There are pixels to copy to the left of the minSeam
				outputData.setPixels(0, row, col, 1, inputData.getPixels(0, row, col, 1, auxiliary));
			}

			if (col < outputWidth)
			{
				// There are pixels to copy to the right of the minSeam
				int colsAfter = outputWidth - col;
				outputData.setPixels(col, row, colsAfter, 1, inputData
						.getPixels(col + 1, row, colsAfter, 1, auxiliary));
			}
		}

		return output;
	}
}