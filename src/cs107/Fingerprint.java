package cs107;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides tools to compare fingerprint.
 */
public class Fingerprint {

  /**
   * The number of pixels to consider in each direction when doing the linear
   * regression to compute the orientation.
   */
  public static final int ORIENTATION_DISTANCE = 16;

  /**
   * The maximum distance between two minutiae to be considered matching.
   */
  public static final int DISTANCE_THRESHOLD = 5;

  /**
   * The number of matching minutiae needed for two fingerprints to be considered
   * identical.
   */
  public static final int FOUND_THRESHOLD = 20;

  /**
   * The distance between two angle to be considered identical.
   */
  public static final int ORIENTATION_THRESHOLD = 20;

  /**
   * The offset in each direction for the rotation to test when doing the
   * matching.
   */
  public static final int MATCH_ANGLE_OFFSET = 2;

  // this method tests if a pixel's coordinates are contained within an image 
  public static boolean pixelTest(boolean[][] image, int row, int col) {
	  assert (image != null);

    boolean pixelInImage = false;
	  int nbOfRows = image.length;
	  int nbOfCols = image[0].length;
	  
	  if (row >= 0 && row <= nbOfRows - 1 && col >= 0 && col <= nbOfCols - 1) {
		  pixelInImage = true;
	  }
    return pixelInImage;
  }

  public static boolean returnPixel(boolean[][] image, int row, int col) {
    assert (image != null);

    boolean pixelValue = false;
    if (!pixelTest(image, row, col)) {
      pixelValue = false;
    } else {
      pixelValue = image[row][col];
    }
    return pixelValue;
  }
  
  /**
   * Returns an array containing the value of the 8 neighbours of the pixel at
   * coordinates <code>(row, col)</code>.
   * <p>
   * The pixels are returned such that their indices corresponds to the following
   * diagram:<br>
   * ------------- <br>
   * | 7 | 0 | 1 | <br>
   * ------------- <br>
   * | 6 | _ | 2 | <br>
   * ------------- <br>
   * | 5 | 4 | 3 | <br>
   * ------------- <br>
   * <p>
   * If a neighbours is out of bounds of the image, it is considered white.
   * <p>
   * If the <code>row</code> or the <code>col</code> is out of bounds of the
   * image, the returned value should be <code>null</code>.
   *
   * @param image array containing each pixel's boolean value.
   * @param row   the row of the pixel of interest, must be between
   *              <code>0</code>(included) and
   *              <code>image.length</code>(excluded).
   * @param col   the column of the pixel of interest, must be between
   *              <code>0</code>(included) and
   *              <code>image[row].length</code>(excluded).
   * @return An array containing each neighbours' value.
   */
  public static boolean[] getNeighbours(boolean[][] image, int row, int col) {
	  assert (image != null); // special case that is not expected (the image is supposed to have been checked earlier)
	  // initiate a list that will contain the values of the neighbours
	  boolean[] neighbours = new boolean[8];
    // lists of coordinates of the pixel's neighbours relative to the pixelfrom p0 to p7
	  int[] neighbourRow = {row - 1, row - 1, row, row + 1, row + 1, row + 1, row, row - 1};
	  int[] neighbourCol = {col, col + 1, col + 1, col + 1, col, col - 1, col - 1, col - 1};
    // checking if the pixel we are analysing is in the image
	  if (!pixelTest(image, row, col)) {
      neighbours = null;
	  } else {
      for (int i = 0; i < neighbours.length; i++) {
        neighbours[i] = returnPixel(image, neighbourRow[i], neighbourCol[i]);
		  }	  
	  }
	  return neighbours;
  }

  /**
   * Computes the number of black (<code>true</code>) pixels among the neighbours
   * of a pixel.
   *
   * @param neighbours array containing each pixel value. The array must respect
   *                   the convention described in
   *                   {@link #getNeighbours(boolean[][], int, int)}.
   * @return the number of black neighbours.
   */
  public static int blackNeighbours(boolean[] neighbours) {
	  int blackNeighboursCount = 0;
	  for (int i = 0; i < neighbours.length; i++) {
		  if (neighbours[i] == true) {
			  blackNeighboursCount++;
		  }
	  }
	  return blackNeighboursCount;	 
  }
  
  /**
   * Computes the number of white to black transitions among the neighbours of
   * pixel.
   *
   * @param neighbours array containing each pixel value. The array must respect
   *                   the convention described in
   *                   {@link #getNeighbours(boolean[][], int, int)}.
   * @return the number of white to black transitions.
   */
  public static int transitions(boolean[] neighbours) {
	  int nbTransitions = 0;

	  for (int i = 0; i < neighbours.length - 1; i++) {
		  if (neighbours[i] == false && neighbours[i+1] == true) { //Transition if the pixel is white and if the next one is black
			  nbTransitions++;
		  }
	  }
	  if (neighbours[7] == false && neighbours[0] == true) {
		  nbTransitions++;
	  }
	  return nbTransitions;
  }

  /**
   * Returns <code>true</code> if the images are identical and false otherwise.
   *
   * @param image1 array containing each pixel's boolean value.
   * @param image2 array containing each pixel's boolean value.
   * @return <code>True</code> if they are identical, <code>false</code>
   *         otherwise.
   */
  public static boolean identical(boolean[][] image1, boolean[][] image2) {
    boolean match = true;
	   if ((image1.length != image2.length) || (image1[0].length != image2[0].length)) {
			  match = false;
		  }
		  for (int row = 0; row < image1.length; row++) {
			  for (int col = 0; col < image1[0].length; col++) {
				  if (image1[row][col] != image2[row][col]) {
					  match = false;
				  }  
			  }
		  }
	  return match;
  }

  public static boolean[][] copyImage(boolean[][] copiedImage) {
    int nbOfRows = copiedImage.length;
    int nbOfCols = copiedImage[0].length;
    boolean[][] pastedImage = new boolean[nbOfRows][nbOfCols];

    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {
        pastedImage[row][col] = copiedImage[row][col];
      }
    }
    return pastedImage;
  }

  /**
   * Internal method used by {@link #thin(boolean[][])}.
   *
   * @param image array containing each pixel's boolean value.
   * @param step  the step to apply, Step 0 or Step 1.
   * @return A new array containing each pixel's value after the step.
   */
  public static boolean[][] thinningStep(boolean[][] image, int step) {
    int nbOfRows = image.length;
    int nbOfCols = image[0].length;

    boolean[][] newImage = copyImage(image);
    
    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {

        boolean[] neighbours = getNeighbours(image, row, col);

        boolean condition1 = image[row][col];
        boolean condition2 = true;
        boolean condition3 = blackNeighbours(neighbours) >= 2 && blackNeighbours(neighbours) <= 6;
        boolean condition4 = transitions(neighbours) == 1;
        boolean condition5 = false;
        boolean condition6 = false;

        if (neighbours == null) {
          condition2 = false;
        }

        boolean p0 = neighbours[0];
        boolean p2 = neighbours[2];
        boolean p4 = neighbours[4];
        boolean p6 = neighbours[6];

        if (step == 0) {
          condition5 = !p0 || !p2 || !p4;
          condition6 = !p2 || !p4 || !p6;
        } else if (step == 1) {
          condition5 = !p0 || !p2 || !p6;
          condition6 = !p0 || !p4 || !p6;
        }
        if (condition1 && condition2 && condition3 && condition4 && condition5 && condition6) {
          newImage[row][col] = false;
        }
      }
    }
	  return newImage;
  }
  
  /**
   * Compute the skeleton of a boolean image.
   *
   * @param image array containing each pixel's boolean value.
   * @return array containing the boolean value of each pixel of the image after
   *         applying the thinning algorithm.
   */
  public static boolean[][] thin(boolean[][] image) {
    
    boolean[][] startImage = copyImage(image); 
    boolean[][] intermediaryImage = null;
    boolean[][] endImage = null;
    boolean pixelChange = true;

    while (pixelChange) {

      intermediaryImage = thinningStep(startImage, 0);
      endImage = thinningStep(intermediaryImage, 1);
      if (identical(startImage, endImage)) {
        pixelChange = false; 
      } else {
        startImage = copyImage(endImage);
      }
    }
    return endImage;
  }

  /**
   * Computes all pixels that are connected to the pixel at coordinate
   * <code>(row, col)</code> and within the given distance of the pixel.
   *
   * @param image    array containing each pixel's boolean value.
   * @param row      the first coordinate of the pixel of interest.
   * @param col      the second coordinate of the pixel of interest.
   * @param distance the maximum distance at which a pixel is considered.
   * @return An array where <code>true</code> means that the pixel is within
   *         <code>distance</code> and connected to the pixel at
   *         <code>(row, col)</code>.
   */
  public static boolean[][] connectedPixels(boolean[][] image, int row, int col, int distance) {
    if(distance < 0) {
	return null;
	  }
    boolean[][] newImage = new boolean[image.length][image[0].length];
    newImage[row][col] = true;
    boolean[][] tempImage = copyImage(newImage);
    boolean pixelExist = true;
	  
    if(image[row][col] == false) {
   	return null;

    } else {
      while(pixelExist){
      
        for(int rowGap = row - distance; rowGap <= row + distance; rowGap++){
          for(int colGap = col - distance; colGap <= col + distance; colGap ++){

            if(pixelTest(image, rowGap, colGap) && (image[rowGap][colGap])){
              boolean[] tempNeighbours = getNeighbours(newImage, rowGap, colGap);
            
                if(blackNeighbours(tempNeighbours) > 0){
                 newImage[rowGap][colGap] = true;
                }
            }
          }
        }
        if(identical(newImage, tempImage)){
          pixelExist = false;

        } else {
          tempImage = copyImage(newImage);
        }
      }
    }
    return newImage;
  } 

  public static int countConnectedPixels(boolean[][] image) {
    int nbOfRows = image.length;
    int nbOfCols = image[0].length;
    int nbOfConnectedPixels = 0;

    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {
        boolean currentPixel = returnPixel(image, row, col);
        if (currentPixel) {
          nbOfConnectedPixels++;
        }
      }
    }
    return nbOfConnectedPixels;
  }

  public static int[] connectedPixelsRows (boolean[][] image, int minutiaRow) {

    int nbOfConnectedPixels = countConnectedPixels(image);
    int nbOfRows = image.length;
    int nbOfCols = image[0].length;
    int[] rows = new int[nbOfConnectedPixels];
    int i = 0;

    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {
        boolean pixel = returnPixel(image, row, col);
        if (pixel) {
          rows[i] = minutiaRow - row;
          i++;
        }
      }
    }
    return rows;
  }

  public static int[] connectedPixelsCols (boolean[][] image, int minutiaCol) {
    int nbOfConnectedPixels = countConnectedPixels(image);
    int nbOfRows = image.length;
    int nbOfCols = image[0].length;
    int[] cols = new int[nbOfConnectedPixels];
    int i = 0;

    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {
        boolean pixel = returnPixel(image, row, col);
        if (pixel) {
          cols[i] = col - minutiaCol;
          i++;
        }
      }
    }
    return cols;
  }

  /**
   * Computes the slope of a minutia using linear regression.
   *
   * @param connectedPixels the result of
   *                        {@link #connectedPixels(boolean[][], int, int, int)}.
   * @param row             the row of the minutia.
   * @param col             the col of the minutia.
   * @return the slope.
   */
  public static double computeSlope(boolean[][] connectedPixels, int minutiaRow, int minutiaCol) {
	  int nbOfConnectedPixels = countConnectedPixels(connectedPixels);
    int[] rows = connectedPixelsRows(connectedPixels, minutiaRow);
    int[] cols = connectedPixelsCols(connectedPixels, minutiaCol);

    double sumOfxy = 0;
    double sumOfx2 = 0;
    double sumOfy2 = 0;
    double slope = 0;

    for (int i = 0; i < nbOfConnectedPixels; i++) {
      int x = cols[i];
      int y = rows[i];
      sumOfxy += x*y;
      sumOfx2 += x*x;
      sumOfy2 += y*y;
    }

    if (sumOfx2 == 0) {
      slope = Double.POSITIVE_INFINITY;

    } else if (sumOfx2 >= sumOfy2) {
      slope = sumOfxy / sumOfx2;

    } else if (sumOfx2 < sumOfy2){
      slope = sumOfy2 / sumOfxy;
    }
	  return slope;
  }

  /**
   * Computes the orientation of a minutia in radians.
   * 
   * @param connectedPixels the result of
   *                        {@link #connectedPixels(boolean[][], int, int, int)}.
   * @param row             the row of the minutia.
   * @param col             the col of the minutia.
   * @param slope           the slope as returned by
   *                        {@link #computeSlope(boolean[][], int, int)}.
   * @return the orientation of the minutia in radians.
   */
  public static double computeAngle(boolean[][] connectedPixels, int minutiaRow, int minutiaCol, double slope) {
	  int nbOfConnectedPixels = countConnectedPixels(connectedPixels);
    int[] rows = connectedPixelsRows(connectedPixels, minutiaRow);
    int[] cols = connectedPixelsCols(connectedPixels, minutiaCol);
    int nbOfPixelsOverLine = 0;
    int nbOfPixelsUnderLine = 0;
    double arcTan = 0;

    if (slope == 0) {
      for (int i = 0; i < nbOfConnectedPixels; i++) {
        if (cols[i] >= 0) {
          nbOfPixelsOverLine++;
        } else if (cols[i] < 0) {
          nbOfPixelsUnderLine++;
        }
      }
      boolean moreUnder = nbOfPixelsUnderLine > nbOfPixelsOverLine;
      if (moreUnder) {
        arcTan = Math.PI;
      } else {
        arcTan = 0;
      }
    }

    if (slope != Double.POSITIVE_INFINITY && slope != 0) {
      double inverseSlope = -(1 / slope);
      arcTan = Math.atan(slope);
      
      for (int i = 0; i < nbOfConnectedPixels; i++) {
        double x = cols[i];
        double y = rows[i];
        if (y >= inverseSlope * x) {
          nbOfPixelsOverLine++;
        } else {
          nbOfPixelsUnderLine++;
        }
      }
      boolean moreOver = nbOfPixelsOverLine >= nbOfPixelsUnderLine;
      boolean moreUnder = !moreOver;
      boolean condition1 = arcTan > 0 && moreUnder;
      boolean condition2 = arcTan < 0 && moreOver;
      if (condition1 || condition2) {
        arcTan += Math.PI;
      }
    }
    
    if (slope == Double.POSITIVE_INFINITY) {
      double inverseSlope = 0;
      for (int i = 0; i < nbOfConnectedPixels; i++) {
        double x = cols[i];
        double y = rows[i];
        if (y >= inverseSlope * x) {
          nbOfPixelsOverLine++;
        } else {
          nbOfPixelsUnderLine++;
        }
      }
      boolean moreOver = nbOfPixelsOverLine > nbOfPixelsUnderLine;
      if (moreOver) {
        arcTan = Math.PI / 2;
      } else {
        arcTan = -Math.PI / 2;
      }
    }
    return arcTan;
  }

  /**
   * Computes the orientation of the minutia that the coordinate <code>(row,
   * col)</code>.
   *
   * @param image    array containing each pixel's boolean value.
   * @param row      the first coordinate of the pixel of interest.
   * @param col      the second coordinate of the pixel of interest.
   * @param distance the distance to be considered in each direction to compute
   *                 the orientation.
   * @return The orientation in degrees.
   */
  public static int computeOrientation(boolean[][] image, int row, int col, int distance) {
	  boolean[][] connectedPixels = connectedPixels(image, row, col, distance);
    double slope = computeSlope(connectedPixels, row, col);
    double arcTan = computeAngle(connectedPixels, row, col, slope);
    int angle = Math.toIntExact(Math.round(Math.toDegrees(arcTan)));
    if (angle < 0) {
      angle += 360;
    }
    return angle;
  }

  /**
   * Extracts the minutiae from a thinned image.
   *
   * @param image array containing each pixel's boolean value.
   * @return The list of all minutiae. A minutia is represented by an array where
   *         the first element is the row, the second is column, and the third is
   *         the angle in degrees.
   * @see #thin(boolean[][])
   */
  public static List<int[]> extract(boolean[][] image) {
	  //TODO implement
	  return null;
  }

  /**
   * Applies the specified rotation to the minutia.
   *
   * @param minutia   the original minutia.
   * @param centerRow the row of the center of rotation.
   * @param centerCol the col of the center of rotation.
   * @param rotation  the rotation in degrees.
   * @return the minutia rotated around the given center.
   */
  public static int[] applyRotation(int[] minutia, int centerRow, int centerCol, int rotation) {
	  //TODO implement
	  return null;
  }

  /**
   * Applies the specified translation to the minutia.
   *
   * @param minutia        the original minutia.
   * @param rowTranslation the translation along the rows.
   * @param colTranslation the translation along the columns.
   * @return the translated minutia.
   */
  public static int[] applyTranslation(int[] minutia, int rowTranslation, int colTranslation) {
	  //TODO implement
	  return null;
  } 
  
  /**
   * Computes the row, column, and angle after applying a transformation
   * (translation and rotation).
   *
   * @param minutia        the original minutia.
   * @param centerCol      the column around which the point is rotated.
   * @param centerRow      the row around which the point is rotated.
   * @param rowTranslation the vertical translation.
   * @param colTranslation the horizontal translation.
   * @param rotation       the rotation.
   * @return the transformed minutia.
   */
  public static int[] applyTransformation(int[] minutia, int centerRow, int centerCol, int rowTranslation,
      int colTranslation, int rotation) {
	  //TODO implement
	  return null;
  }

  /**
   * Computes the row, column, and angle after applying a transformation
   * (translation and rotation) for each minutia in the given list.
   *
   * @param minutiae       the list of minutiae.
   * @param centerCol      the column around which the point is rotated.
   * @param centerRow      the row around which the point is rotated.
   * @param rowTranslation the vertical translation.
   * @param colTranslation the horizontal translation.
   * @param rotation       the rotation.
   * @return the list of transformed minutiae.
   */
  public static List<int[]> applyTransformation(List<int[]> minutiae, int centerRow, int centerCol, int rowTranslation,
      int colTranslation, int rotation) {
	  //TODO implement
	  return null;
  }
  /**
   * Counts the number of overlapping minutiae.
   *
   * @param minutiae1      the first set of minutiae.
   * @param minutiae2      the second set of minutiae.
   * @param maxDistance    the maximum distance between two minutiae to consider
   *                       them as overlapping.
   * @param maxOrientation the maximum difference of orientation between two
   *                       minutiae to consider them as overlapping.
   * @return the number of overlapping minutiae.
   */
  public static int matchingMinutiaeCount(List<int[]> minutiae1, List<int[]> minutiae2, int maxDistance,
      int maxOrientation) {
	  //TODO implement
	  return 0;
  }

  /**
   * Compares the minutiae from two fingerprints.
   *
   * @param minutiae1 the list of minutiae of the first fingerprint.
   * @param minutiae2 the list of minutiae of the second fingerprint.
   * @return Returns <code>true</code> if they match and <code>false</code>
   *         otherwise.
   */
  public static boolean match(List<int[]> minutiae1, List<int[]> minutiae2) {
	  //TODO implement
	  return false;
  }
}
