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
	  int nbOfRows = image.length;
	  int nbOfCols = image[0].length;
	  
	  if (row < 0 || row > nbOfRows - 1 || col < 0 || col > nbOfCols - 1) {
		  return false;
	  }
	  else {
		  return true;
	  }
	  
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
    // lists of coordinates of the pixel's neighbours relative to the pixel
	  int[] neighbourRowDiff = {-1, -1, 0, 1, 1, 1, 0, -1};
	  int[] neighbourColDiff = {0, 1, 1, 1, 0, -1, -1, -1};
	  
    // testing if the pixel we are analysing is in the image
	  if (pixelTest(image, row, col)) {
      // assessing the position and value of each neighbour (from 0 to 7)
		  for (int i = 0; i < neighbours.length; i++) {
			  int neighbourRow = row + neighbourRowDiff[i];
			  int neighbourCol = col + neighbourColDiff[i];
			  
			  if (pixelTest(image, neighbourRow, neighbourCol) == true) {
				  neighbours[i] = image[neighbourRow][neighbourCol];
			  } else {
				  neighbours[i] = false;
			  }
		  }
	  } else {
		  neighbours = null;
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

	  int blackNeighbourscount = 0;
	  for (int i = 0; i < neighbours.length; i++) {
		  if (neighbours[i] == true) {
			  blackNeighbourscount++;
		  }
	  }
	  return blackNeighbourscount;
	 
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
    
	   if ((image1.length != image2.length) || (image1[0].length != image2[0].length)) {
			  return false;
		  }
		  
		  for (int row = 0; row < image1.length; row++) {
			  for (int col = 0; col < image1[0].length; col++) {
				  if (image1[row][col] != image2[row][col]) {
					  return false;
				  }  
			  }
		  }
	  return true;
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

    boolean[][] square = copyImage(image);
    
    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {

        boolean condition1 = image[row][col];
        boolean condition2 = true;
        boolean[] neighbours = getNeighbours(image, row, col);
// github test 
        if (neighbours == null) {
          condition2 = false;
        }

        boolean p0 = neighbours[0];
        boolean p2 = neighbours[2];
        boolean p4 = neighbours[4];
        boolean p6 = neighbours[6];

        boolean condition3 = blackNeighbours(neighbours) >= 2 && blackNeighbours(neighbours) <= 6;
        boolean condition4 = transitions(neighbours) == 1;
        boolean condition5 = false;
        boolean condition6 = false;

        if (step == 0) {

          condition5 = !p0 || !p2 || !p4;
          condition6 = !p2 || !p4 || !p6;
        }
        else if (step == 1) {

          condition5 = !p0 || !p2 || !p6;
          condition6 = !p0 || !p4 || !p6;
        }

        if (condition1 && condition2 && condition3 && condition4 && condition5 && condition6) {
          square[row][col] = false;
        }

      }
    }
	  return square;
  }
  
  /**
   * Compute the skeleton of a boolean image.
   *
   * @param image array containing each pixel's boolean value.
   * @return array containing the boolean value of each pixel of the image after
   *         applying the thinning algorithm.
   */
  public static boolean[][] thin(boolean[][] image) {
	  
    boolean pixelChanged = true;
    boolean[][] tempImage0 = copyImage(image); 
    boolean[][] tempImage1 = null;
    boolean[][] tempImage2 = null;

    while (pixelChanged) {

      tempImage1 = thinningStep(tempImage0, 0);

      if (identical(tempImage0, tempImage1)) {
        pixelChanged = false;
        break;
      }

      tempImage2 = thinningStep(tempImage1, 1);

      if (identical(tempImage0, tempImage2)) {
        pixelChanged = false;
      }
      else {
        tempImage0 = copyImage(tempImage2);
      }
    }
	  return tempImage2;
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
	  int squareLength = (2 * distance) + 1;
	  boolean[][] square = new boolean[squareLength][squareLength];
	  boolean[][] newImage = new boolean[image.length][image[0].length];
	  
	  boolean pixelExist = true;
	  
	  if(image[row][col] == false) {
		 return null;
    } else {
      newImage[row][col] = true;
      
      for (int newRow = 0; newRow < image.length; newRow++) {
        for (int newCol = 0; newCol < image[newRow].length; newCol++) {
          square[squareLength / 2  + newRow][squareLength / 2  + newCol] = image[newRow][newCol];
        }
      }

      for (int rowLength = 0; rowLength <= squareLength; rowLength++) {
        for (int colLength = 0; colLength <= squareLength; colLength++) {
          if (pixelTest(image, rowLength, colLength) && image[row][col]) {

            boolean[] tempNeighbours = getNeighbours(newImage, rowLength,colLength);
            for (int tempSize = 0; tempSize < tempNeighbours.length; tempSize++) {
    
              System.out.print(tempNeighbours[tempSize]+ " ");
              if(tempNeighbours[tempSize]) {
                newImage[rowLength][colLength] = true;
              }
            }
            System.out.println();
          }
        }
      }
    }
    for(int ligne = 0; ligne < squareLength; ligne++) {
      for(int colonne = 0; colonne< squareLength; colonne ++) {
        System.out.print(square[ligne][colonne]+ " ");
      }
      System.out.println();
    }
    return newImage;
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
  public static double computeSlope(boolean[][] connectedPixels, int row, int col) {
	  //TODO implement
	  return 0;
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
  public static double computeAngle(boolean[][] connectedPixels, int row, int col, double slope) {
	  //TODO implement
	  return 0;
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
	  //TODO implement
	  return 0;
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
