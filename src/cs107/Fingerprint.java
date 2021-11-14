package cs107;

import java.util.ArrayList;
//import java.util.Arrays;
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

  /**
   * Returns the value of the pixel if it is in the image, otherwise it returns false.
   *
   * @param image array containing each pixel's boolean value.
   * @param row   the row of the pixel of interest.
   * @param col   the column of the pixel of interest.
   * @return <code>true</code> or <code>false</code> depending on the pixel's value and location.
   */
  public static boolean returnPixel(boolean[][] image, int row, int col) {
    assert (image != null);
    if (row < 0 || row >= image.length || col < 0 || col >= image[0].length) {
      return false;
    } else {
      return image[row][col];
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
	  boolean[] neighbours = new boolean[8]; 
	  int[] neighbourRow = {row - 1, row - 1, row, row + 1, row + 1, row + 1, row, row - 1}; // lists of coordinates of the pixel's neighbours 
	  int[] neighbourCol = {col, col + 1, col + 1, col + 1, col, col - 1, col - 1, col - 1}; // relative to the pixel
	  if (row < 0 || row >= image.length || col < 0 || col >= image[0].length) {
      return null;
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
		  if (neighbours[i]) {
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
		  if (!neighbours[i] && neighbours[i+1]) { //Transition if the pixel is white and if the next one is black
			  nbTransitions++;
		  }
	  }
	  if (!neighbours[7] && neighbours[0]) {
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

  /**
   * Returns the image given in argument.
   * @param copiedImage array containing each pixel's boolean value.
   * @return an array identical to the image to copy.
   */
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
        } else {
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
    boolean[][] newImage = new boolean[image.length][image[0].length];
    newImage[row][col] = true;
    boolean[][] tempImage = copyImage(newImage);
    boolean pixelChange = true;
    if (distance < 0 || !returnPixel(image, row, col)) {
      return null;
	  } else {
      while (pixelChange){
        for (int rowGap = row - distance; rowGap <= row + distance; rowGap++){
          for (int colGap = col - distance; colGap <= col + distance; colGap++){  //"construction" of a square around the minutia to iterate over each pixel
            if (returnPixel(image, rowGap, colGap)) {
              boolean[] neighbours = getNeighbours(newImage, rowGap, colGap);
                if (blackNeighbours(neighbours) > 0) {
                  newImage[rowGap][colGap] = true;
                }
              }
            }
          }
          if (identical(newImage, tempImage)) {
            pixelChange = false;
          } else {
            tempImage = copyImage(newImage);
          }
        }
      }
    return newImage;
  }

  /**
   * Computes the number of black pixels in an image.
   *
   * @param image array containing each pixel's boolean value.
   * @return the number of black pixels in the image.
   */
  /**
   * Returns an arraylist containing all connected pixels'
   * rows, with the minutia as the coordinate origin.
   * 
   * @param connectedPixels the result of
   *                        {@link #connectedPixels(boolean[][], int, int, int)}.
   * @param minutiaRow      the row of the minutia.
   * @return an arraylist of vertical coordinates.
   */
  public static List<Integer> connectedPixelsRows (boolean[][] connectedPixels, int minutiaRow) {
    
    int nbOfRows = connectedPixels.length;
    int nbOfCols = connectedPixels[0].length;
    List<Integer> rowList = new ArrayList<Integer>();
    
    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {
        if (returnPixel(connectedPixels, row, col)) {
          rowList.add(minutiaRow - row);
          
        }
      }
    }
    return rowList;
  }

  /**
   * Returns an arraylist containing all connected pixels'
   * columns, with the minutia as the coordinate origin.
   * 
   * @param connectedPixels the result of
   *                        {@link #connectedPixels(boolean[][], int, int, int)}.
   * @param minutiaCol      the column of the minutia.
   * @return an arraylist of horizontal coordinates.
   */
  public static List<Integer> connectedPixelsCols (boolean[][] image, int minutiaCol) {
    
    int nbOfRows = image.length;
    int nbOfCols = image[0].length;
    List<Integer> colList = new ArrayList<Integer>();
    
    for (int row = 0; row < nbOfRows; row++) {
      for (int col = 0; col < nbOfCols; col++) {
        if (returnPixel(image, row, col)) {
          colList.add(col - minutiaCol);
          
        }
      }
    }
    return colList;
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
	  
    List<Integer> rowList = connectedPixelsRows(connectedPixels, row);
    List<Integer> colList = connectedPixelsCols(connectedPixels, col);
    double sumOfxy = 0;
    double sumOfx2 = 0;
    double sumOfy2 = 0;
    double slope = 0;
    for (int i = 0; i < rowList.size(); i++) {
      int x = colList.get(i);
      int y = rowList.get(i);
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
   * Computes the number of pixels over the line perpendicular
   *  to the tangent in computeAngle.
   *
   * @param nbOfConnectedPixels size of the lists that contains the columns/rows of connected black pixels
   * @param rows                the result of
   *                            {@link #connectedPixelsRows(boolean[][], int)}
   * @param cols                the result of
   *                            {@link #connectedPixelsCols(boolean[][], int)}
   * @param inverseSlope        the negative inverse of the slope 
   *                            computed in computeSlope.
   * @return the number of pixels over the line.
   */
  public static int countPixelsOverLine(int nbOfConnectedPixels, List<Integer> rows, List<Integer> cols, double inverseSlope) {
    int nbOverLine = 0;
    for (int i = 0; i < nbOfConnectedPixels; i++) {
      double x = cols.get(i);
      double y = rows.get(i);
      if (y >= x * inverseSlope) {
        nbOverLine++;
      }
    }
    return nbOverLine;
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
	  
    List<Integer> rowList = connectedPixelsRows(connectedPixels, row);
    int nbOfConnectedPixels = rowList.size();
    List<Integer> cols = connectedPixelsCols(connectedPixels, col);
    double arcTan = 0;
    if (slope == 0) {
      for (int i = 0; i < nbOfConnectedPixels; i++) {
        if (returnPixel(connectedPixels, row, col + 1)) {
          arcTan = 0;
        } else {
          arcTan = Math.PI;
        }
      }
    } else if (slope == Double.POSITIVE_INFINITY) {
      double inverseSlope = 0;
      int nbOverLine = countPixelsOverLine(nbOfConnectedPixels, rowList, cols, inverseSlope);
      int nbUnderLine = nbOfConnectedPixels - nbOverLine;
      boolean moreOver = nbOverLine >= nbUnderLine;
      if (moreOver) {
        arcTan = Math.PI / 2;
      } else {
        arcTan = -Math.PI / 2;
      }
    } else if (slope != Double.POSITIVE_INFINITY && slope != 0) {
      double inverseSlope = -(1 / slope);
      arcTan = Math.atan(slope);
      int nbOverLine = countPixelsOverLine(nbOfConnectedPixels, rowList, cols, inverseSlope);
      int nbUnderLine = nbOfConnectedPixels - nbOverLine;
      boolean moreOver = nbOverLine > nbUnderLine;
      if (arcTan > 0 && !moreOver || arcTan < 0 && moreOver) {
        arcTan += Math.PI;
      }
    }
    return arcTan;
  }

  /**
   * Returns a given angle's mod [360] value.
   * 
   * @param angle the angle's value in degrees to modify.
   * @return the angle with a value between 0 and 359.
   */
  public static int returnTrueAngle(int angle) {
    while (angle > 359){
      angle -=360;
    } 
    while (angle < 0) {
      angle += 360;
    }
    return angle;
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
    int angle = returnTrueAngle(Math.toIntExact(Math.round(Math.toDegrees(arcTan))));
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
	  ArrayList<int[]> minutiaList = new ArrayList<int[]>();
	  for (int row = 1; row < image.length - 1; row++){ 
      for (int col = 1; col < image[0].length - 1; col++){ 
        if (returnPixel(image, row, col)) { 
          boolean[] neighbours = getNeighbours(image, row, col);
          if (transitions(neighbours) == 1 || transitions(neighbours) == 3) { 
            int angle = computeOrientation(image, row, col, ORIENTATION_DISTANCE);
            minutiaList.add(new int[] {row, col, angle});
          }
        }
      }
    }
	  return minutiaList;
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
    int row = minutia[0];
    int col = minutia[1];
    int orientation = minutia[2];
    int x = col - centerCol;
    int y = centerRow - row;
    double rotationInRad = Math.toRadians(rotation);
    int newX = Math.toIntExact(Math.round(x * Math.cos(rotationInRad) - y * Math.sin(rotationInRad)));
    int newY = Math.toIntExact(Math.round(x * Math.sin(rotationInRad) + y * Math.cos(rotationInRad)));
    int newRow = Math.round(centerRow - newY);
    int newCol = Math.round(newX + centerCol);
    int newOrientation = returnTrueAngle(Math.round(orientation + rotation));
    int[] afterRotation = {newRow,newCol, newOrientation};
    return afterRotation;
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
    int row = minutia[0];
    int col = minutia[1];
    int orientation = minutia[2];
    int newRow = row - rowTranslation;
    int newCol = col - colTranslation;
    int newOrientation = orientation;
    int[] afterTranslation = {newRow, newCol, newOrientation};
    return afterTranslation;
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
	  int[] minutiaAfterRotation = applyRotation(minutia, centerRow, centerCol, rotation);
    int[] minutiaAfterTranslation = applyTranslation(minutiaAfterRotation, rowTranslation, colTranslation);
	  return minutiaAfterTranslation;
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
  public static List<int[]> applyTransformation(List<int[]> minutiae, int centerRow, int centerCol, int rowTranslation, int colTranslation, int rotation) {
    List <int[]> newMinutiae = new ArrayList<int[]>();
    int nbOfMinutiae = minutiae.size();
	  for (int i = 0; i < nbOfMinutiae; i++) {
      int[] minutia = minutiae.get(i);
      int[] newMinutia = applyTransformation(minutia, centerRow, centerCol, rowTranslation, colTranslation, rotation);
      newMinutiae.add(new int[] {newMinutia[0], newMinutia[1], newMinutia[2]});
    }
	  return newMinutiae;
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
  public static int matchingMinutiaeCount(List<int[]> minutiae1, List<int[]> minutiae2, int maxDistance, int maxOrientation) {
    int NbSameMinutiae = 0;
    for(int m1Count = 0; m1Count < minutiae1.size(); m1Count++ ){ 
      for(int m2Count = 0; m2Count < minutiae2.size(); m2Count++){
        int row1 = minutiae1.get(m1Count)[0];
        int col1 = minutiae1.get(m1Count)[1];
        int angle1 = minutiae1.get(m1Count)[2];
        int row2 = minutiae2.get(m2Count)[0];
        int col2 = minutiae2.get(m2Count)[1];
        int angle2 = minutiae2.get(m2Count)[2];
        double euclidianDistance = Math.sqrt((row1-row2)*(row1-row2) + (col1-col2)*(col1-col2));
        int angleDiff = Math.abs(angle1 - angle2);
        if (euclidianDistance <= maxDistance && angleDiff <= maxOrientation){
          NbSameMinutiae++;
        }
      }
    }
	  return NbSameMinutiae;
  }

  /**
   * Compares the minutiae from two fingerprints.
   *
   * @param minutiae1 the list of minutiae of the first fingerprint.
   * @param minutiae2 the list of minutiae of the second fingerprint.
   * @return Returns <code>true</code> if they match and <code>false</code>
   *         otherwise.
   */
  public static boolean  match(List<int[]> minutiae1, List<int[]> minutiae2) {
    int nbMatchingMinutiae = 0;
    List<int[]> transfomerdMinutiae = new ArrayList<int[]>();
      for (int i = 0; i < minutiae1.size(); i++) { //Loop through all minutiae in List1
        for (int j = 0; j < minutiae2.size(); j++) { //Loop through all minutiae in List2
          int centerRow = minutiae1.get(i)[0];
          int centerCol = minutiae1.get(i)[1];
          int rowTranslation = minutiae2.get(j)[0] - centerRow;
          int colTranslation = minutiae2.get(j)[1] - centerCol;
          int rotation = minutiae2.get(j)[2] - minutiae1.get(i)[2];
          for (int offset = rotation - MATCH_ANGLE_OFFSET; offset <= rotation + MATCH_ANGLE_OFFSET; offset++) { //Loop through each angle offset
            transfomerdMinutiae = applyTransformation(minutiae2, centerRow, centerCol, rowTranslation, colTranslation, offset);
            nbMatchingMinutiae = matchingMinutiaeCount(minutiae1, transfomerdMinutiae, DISTANCE_THRESHOLD, ORIENTATION_THRESHOLD); 
            if (nbMatchingMinutiae >= FOUND_THRESHOLD) { //Check if the number of matching minutiae is great enough for the two fingerprints to match
              System.out.print(nbMatchingMinutiae+ " ");
              return true;
            }
          }
        }
      }
      System.out.print(nbMatchingMinutiae + " ");
      return false;
  }
}
