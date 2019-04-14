
/**
 * API  @ Author: Bijan Fazeli
 * 
 * API - Provides encapsulation of various actions
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javafx.util.Pair;

public class API {

    public int compVal;
    private int newH, newW;
    private int heightO, widthO;

    private int[][][] y8x8, cb8x8, cr8x8;

    public int getInputFromUser(Scanner reader) {
        System.out.print("Main Menu-----------------------------------\n" + "1. VQ (Vector Quantization)\n"
                + "2. DCT-based Coding\n" + "3. Quit\n" + "\nPlease enter the task number [1-3]: ");
        return reader.nextInt();
    }

    // // TASK 1
    //
    //
    //
    //
    //
    //
    //
    //
    public void performVQ(String imgName) {
        MImage img = new MImage(imgName);
        MImage paddedImage = paddOrigImage(img);

        ArrayList<int[]> vectors = getVectorsFromImg(paddedImage);

        Pair<int[][], int[]> clustersAndClosestCluster = kMeansClustering(vectors);
        printClusters(clustersAndClosestCluster.getKey());

        MImage vqImage = quantizationWithCodebook(paddedImage.getW(), paddedImage.getH(),
                clustersAndClosestCluster.getValue());
        String newName = "vq" + imgName + ".ppm";
        vqImage.write2PPM(newName);

        decodeVQ(clustersAndClosestCluster.getKey(), newName);
    }

    public MImage paddOrigImage(MImage img) {

        int width = img.getW();
        int height = img.getH();
        int origWidth = width;
        int origHeight = height;

        // If a dimension is not even, pad
        if (width % 2 != 0) {
            width++;
        }
        if (height % 2 != 0) {
            height++;
        }

        // Create a new image with padding
        MImage paddedImg = new MImage(width, height);
        // Pass the info in the old image into the new one
        int[] rgb = new int[3];
        for (int i = 0; i < origHeight; i++) {
            for (int j = 0; j < origWidth; j++) {
                img.getPixel(j, i, rgb);
                paddedImg.setPixel(j, i, rgb);
            }
        }

        return paddedImg;
    }

    public static ArrayList<int[]> getVectorsFromImg(MImage img) {
        // Loop through the pixels 2X2
        int[] rgb = new int[3];

        ArrayList<int[]> vectors = new ArrayList<>();
        int[] vector = new int[12];

        for (int row = 0, height = img.getH(); row < height - 1; row += 2) {
            for (int column = 0, width = img.getW(); column < width - 1; column += 2) {
                img.getPixel(column, row, rgb);
                vector[0] = rgb[0];
                vector[1] = rgb[1];
                vector[2] = rgb[2];
                img.getPixel(column + 1, row, rgb);
                vector[3] = rgb[0];
                vector[4] = rgb[1];
                vector[5] = rgb[2];
                img.getPixel(column, row + 1, rgb);
                vector[6] = rgb[0];
                vector[7] = rgb[1];
                vector[8] = rgb[2];
                img.getPixel(column + 1, row + 1, rgb);
                vector[9] = rgb[0];
                vector[10] = rgb[1];
                vector[11] = rgb[2];

                vectors.add(vector);
            }
        }

        return vectors;
    }

    private Pair<int[][], int[]> kMeansClustering(ArrayList<int[]> vectors) {
        // Create a 2D array with 256 X 12 elements (Clusters)
        int clusters[][] = new int[256][12];

        int[] closestCluster = null;
        Random rand = new Random();
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 12; j++) {
                clusters[i][j] = rand.nextInt(256);
            }
        }

        Map<Integer, List<Integer>> prevNearestMap = null;
        for (int run = 0; run < 100; run++) {

            closestCluster = nearestCluster(vectors, clusters);

            // Map the closest cluster with their location
            Map<Integer, List<Integer>> nearestMap = new HashMap();

            // Add the closestCluster to the nearestClusterMap
            for (int i = 0; i < closestCluster.length; i++) {
                if (nearestMap.containsKey(closestCluster[i])) {
                    List<Integer> x = nearestMap.get(closestCluster[i]);
                    x.add(i);
                    nearestMap.put(closestCluster[i], x);

                } // If not, insert new arraylist
                else {
                    List<Integer> x = new ArrayList();
                    x.add(i);
                    nearestMap.put(closestCluster[i], x);
                }

            }

            // Find the average per cluster and update the centroid
            Set<Map.Entry<Integer, List<Integer>>> set = nearestMap.entrySet();

            for (Map.Entry<Integer, List<Integer>> entry : set) {

                List<Integer> currentCluster = entry.getValue();
                int[] sum = new int[12];

                for (int i = 0; i < currentCluster.size(); i++) {
                    int[] currentVector = vectors.get(currentCluster.get(i));
                    for (int j = 0; j < 12; j++) {
                        sum[j] += currentVector[j];
                    }
                }

                for (int i = 0; i < 12; i++) {
                    sum[i] = sum[i] / currentCluster.size();
                }

                clusters[entry.getKey()] = sum;
            }

            // If no change, break
            if (prevNearestMap != null && prevNearestMap.equals(nearestMap)) {
                break;
            }

            prevNearestMap = new HashMap(nearestMap);

            // Clear map
            nearestMap.clear();
        }

        return new Pair<int[][], int[]>(clusters, closestCluster);
    }

    public static int euclideanDistance(int[] list1, int[] list2) {
        int distance = 0;

        for (int i = 0; i < list1.length; i++) {
            distance += Math.pow((list1[i] - list2[i]), 2);
        }
        distance = (int) Math.sqrt(distance);
        return distance;
    }

    public static int[] nearestCluster(ArrayList<int[]> vectors, int[][] clusters) {
        int[] closestCluster = new int[vectors.size()];
        for (int i = 0, size = vectors.size(); i < size; i++) {
            int[] x = vectors.get(i);

            int indexOfClosestCluster = -1;
            double shortestDistance = 1000000000;

            for (int j = 0; j < clusters.length; j++) {
                int[] currentCluster = clusters[j];

                double distance = euclideanDistance(x, currentCluster);

                if (shortestDistance > distance) {
                    shortestDistance = distance;
                    indexOfClosestCluster = j;
                }
            }

            closestCluster[i] = indexOfClosestCluster;
        }
        return closestCluster;
    }

    private void printClusters(int[][] clusters) {
        for (int i = 0; i < clusters.length; i++) {
            System.out.println("Cluster " + (i + 1) + ": " + Arrays.toString(clusters[i]));
        }
    }

    private MImage quantizationWithCodebook(int width, int height, int[] closestCluster) {
        // New quantization algorithm
        // Create a new image with half the width and height of the input padded image
        width = width / 2;
        height = height / 2;
        MImage img = new MImage(width, height);

        // Loop through the input vectors
        int[] rgb = new int[3];

        for (int row = 0; row < height - 1; row += 2) {
            for (int column = 0; column < width - 1; column += 2) {
                rgb[0] = closestCluster[0];
                rgb[1] = closestCluster[1];
                rgb[2] = closestCluster[2];
                img.setPixel(column, row, rgb);
                rgb[3] = closestCluster[0];
                rgb[4] = closestCluster[1];
                rgb[5] = closestCluster[2];
                img.setPixel(column, row, rgb);
                rgb[6] = closestCluster[0];
                rgb[7] = closestCluster[1];
                rgb[8] = closestCluster[2];
                img.setPixel(column, row, rgb);
                rgb[9] = closestCluster[0];
                rgb[10] = closestCluster[1];
                rgb[11] = closestCluster[2];
                img.setPixel(column, row, rgb);
            }
        }

        return img;
    }

    public static void decodeVQ(int[][] clusters, String imgName) {
        // New decode algorithm
        // Create an image 2 times the size of the input image
        MImage img = new MImage(imgName);
        int width = img.getW();
        int height = img.getH();

        ArrayList<int[]> vectors = new ArrayList();
        // Loop through the image
        int[] rgb = new int[3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                img.getPixel(j, i, rgb);
                vectors.add(clusters[rgb[0]]);
            }
        }

        // Convert the output vectors into an image
        MImage decodedImg = new MImage(width * 2, height * 2);

        int x = 0, y = 0;
        boolean end = false;

        for (int index = 0; index < vectors.size() && !end; index++) {
            for (int i = 0; i < 12 && !end; i += 3) {
                rgb[0] = vectors.get(index)[i];
                rgb[1] = vectors.get(index)[i + 1];
                rgb[2] = vectors.get(index)[i + 2];

                decodedImg.setPixel(x, y, rgb);

                x++;

                if (y >= decodedImg.getH() - 1) {
                    end = true;
                } else if (x >= decodedImg.getW() - 1 && y < decodedImg.getH()) {
                    x = 0;
                    y++;
                }

            }
        }

        // Save the image.
        decodedImg.write2PPM("decoded_" + imgName);
    }

    // // TASK 2
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    public void setCompressionValue(Scanner reader) {
        do {
            System.out.print("Give value of n [0-5]: ");
            compVal = reader.nextInt();
        } while ((compVal < 0 || compVal > 5));
    }

    public void performDCT(MImage img) {

        MImage paddedImg = paddImage(img);

        System.out.println(paddedImg);

        int[][] y = new int[paddedImg.getH()][paddedImg.getW()];
        int[][] cb = new int[paddedImg.getH()][paddedImg.getW()];
        int[][] cr = new int[paddedImg.getH()][paddedImg.getW()];
        // int[][] subSampledCb = new int[paddedImg.getH()][paddedImg.getW()];
        // int[][] subSampledCr = new int[paddedImg.getH()][paddedImg.getW()];

        colorSpaceTransformation(y, cb, cr, paddedImg);
        subSampleCbCr(cb, cr);

        // Perform quantization of each transformed y, subSampCb and subSampCr that's
        // been flattened into 8x8 blocks
        y8x8 = transform(compressTo8x8(y));
        cb8x8 = transform(compressTo8x8(cb));
        cr8x8 = transform(compressTo8x8(cr));

        quantization(y8x8, cb8x8, cr8x8);
    }

    public MImage performInverseDCT() {
        deQuantization(y8x8, cb8x8, cr8x8);

        int[][] decodedY = expandTo2D(inverseTransform(y8x8));
        int[][] decodedCb = expandTo2D(inverseTransform(cb8x8));
        int[][] decodedCr = expandTo2D(inverseTransform(cr8x8));

        superSample(decodedCb, decodedCr);

        MImage decodedImage = new MImage(widthO, heightO);

        inverseColorTransform(decodedY, decodedCb, decodedCr, decodedImage);

        return decodedImage;
    }

    // colorSpaceTransformation
    // - Description: Transforms each pixel from RGB to YCbCr using the conversion
    // eq
    private void colorSpaceTransformation(int[][] y, int[][] cb, int[][] cr, MImage img) {
        int[] pixel = new int[3];
        double yVal, cbVal, crVal;
        for (int row = 0; row < img.getH(); row++) {
            for (int column = 0; column < img.getW(); column++) {
                // Populate the pixel
                img.getPixel(column, row, pixel);

                // Do the nec calculation for each R G B
                yVal = (int) (.299 * pixel[0] + .5870 * pixel[1] + .114 * pixel[2]);
                cbVal = (int) (-.1687 * pixel[0] - .3313 * pixel[1] + .5000 * pixel[2]);
                crVal = (int) (.5 * pixel[0] - .4187 * pixel[1] - 0.0813 * pixel[2]);

                // Bound y value
                if (yVal > 255)
                    yVal = 255;
                else if (yVal < 0)
                    yVal = 0;
                // Bound cb val
                if (cbVal > 127.5)
                    cbVal = 127.5;
                else if (cbVal < -127.5)
                    cbVal = -127.5;
                // Bound cr val
                if (crVal > 127.5)
                    crVal = 127.5;
                else if (crVal < -127.5)
                    crVal = -127.5;

                yVal -= 128;
                cbVal -= 0.5;
                crVal -= 0.5;

                y[row][column] = (int) yVal;
                cb[row][column] = (int) cbVal;
                cr[row][column] = (int) crVal;
            }
        }
    }

    // compressTo8x8
    // - Description: Divide image into 8x8 blocks, utilize transform to get uv from
    // xy
    public static ArrayList<int[][]> compressTo8x8(int[][] arr) {

        ArrayList<int[][]> arrOfBlocks = new ArrayList<>();
        int[][] currentBlock = new int[8][8];

        for (int i = 0; i <= arr.length - 8; i += 8) {
            for (int j = 0; j <= arr[i].length - 8; j += 8) {
                for (int k = i, row = 0; row < 8; k++, row++) {
                    for (int l = j, column = 0; column < 8; l++, column++) {
                        currentBlock[row][column] = arr[k][l];

                    }
                }

                currentBlock = new int[8][8];

                arrOfBlocks.add(currentBlock);
            }
        }

        return arrOfBlocks;
    }

    private int[][] expandTo2D(int[][][] blocks) {

        int[][] result = new int[newH][newW];

        int eighth = 0;
        for (int i = 0; i <= newH - 8; i += 8) {
            for (int j = 0; j <= newW - 8; j += 8) {
                for (int k = i, row = 0; row < 8; k++, row++) {
                    for (int l = j, column = 0; column < 8; l++, column++) {
                        result[k][l] = blocks[eighth][row][column];
                    }
                }
                eighth++;
            }
        }

        return result;
    }

    // transform
    // - Description: Applies dct to a block of 8x8 in blocks
    private int[][][] transform(ArrayList<int[][]> blocks) {
        int[][][] dctBlocks = new int[blocks.size()][8][8];

        double cu, cv;
        double summation;
        int result;

        for (int i = 0; i < blocks.size(); i++) {
            for (int u = 0; u < 8; u++) {
                if (u == 0) {
                    cu = 1 / Math.sqrt(2);
                } else {
                    cu = 1;
                }
                for (int v = 0; v < 8; v++) {
                    if (v == 0) {
                        cv = 1 / Math.sqrt(2);
                    } else {
                        cv = 1;
                    }
                    summation = 0;
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            summation += blocks.get(i)[x][y] * (Math.cos((((2 * x) + 1) * u * Math.PI) / 16))
                                    * (Math.cos((((2 * y) + 1) * v * Math.PI) / 16));
                        }
                    }
                    summation = (1 / 4.0) * cu * cv * summation;
                    result = Math.toIntExact(Math.round(summation));

                    if (result < -1024) {
                        result = -1024;
                    } else if (result > 1024) {
                        result = 1024;
                    }
                    dctBlocks[i][u][v] = result;
                }
            }
        }
        return dctBlocks;
    }

    // inverseTransform
    // - Description: Applies inverse dct to a block of 8x8 in blocks
    private int[][][] inverseTransform(int[][][] blocks) {
        int[][][] invBlocks = new int[blocks.length][8][8];

        double cu, cv;
        double summation;
        int result;

        for (int i = 0; i < blocks.length; i++) {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    summation = 0;
                    for (int u = 0; u < 8; u++) {
                        if (u == 0) {
                            cu = 1 / Math.sqrt(2);
                        } else {
                            cu = 1;
                        }
                        for (int v = 0; v < 8; v++) {
                            if (v == 0) {
                                cv = 1 / Math.sqrt(2);
                            } else {
                                cv = 1;
                            }
                            summation += cu * cv * blocks[i][u][v] * Math.cos((((2 * x) + 1) * u * Math.PI) / 16)
                                    * Math.cos((((2 * y) + 1) * v * Math.PI) / 16);
                        }
                    }
                    summation /= 4;
                    result = Math.toIntExact(Math.round(summation));

                    if (result < -128) {
                        result = -128;
                    } else if (result > 127) {
                        result = 127;
                    }
                    invBlocks[i][x][y] = result;
                }
            }
        }
        return invBlocks;
    }

    private void quantization(int[][][] blocksY, int[][][] blocksCb, int[][][] blocksCr) {

        int[][][] yBlocks = blocksY;
        int[][][] cbBlocks = blocksCb;
        int[][][] crBlocks = blocksCr;

        int[][] yTable = new int[][] { { 4, 4, 4, 8, 8, 16, 16, 32 }, { 4, 4, 4, 8, 8, 16, 16, 32 },
                { 4, 4, 8, 8, 16, 16, 32, 32 }, { 8, 8, 8, 16, 16, 32, 32, 32 }, { 8, 8, 16, 16, 32, 32, 32, 32 },
                { 16, 16, 16, 32, 32, 32, 32, 32 }, { 16, 16, 32, 32, 32, 32, 32, 32 },
                { 32, 32, 32, 32, 32, 32, 32, 32 } };

        int[][] cbCrTable = new int[][] { { 8, 8, 8, 16, 32, 32, 32, 32 }, { 8, 8, 8, 16, 32, 32, 32, 32 },
                { 8, 8, 16, 32, 32, 32, 32, 32 }, { 16, 16, 32, 32, 32, 32, 32, 32 },
                { 32, 32, 32, 32, 32, 32, 32, 32 }, { 32, 32, 32, 32, 32, 32, 32, 32 },
                { 32, 32, 32, 32, 32, 32, 32, 32 }, { 32, 32, 32, 32, 32, 32, 32, 32 } };

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                yTable[i][j] *= Math.pow(2, compVal);
                cbCrTable[i][j] *= Math.pow(2, compVal);
            }
        }

        for (int i = 0; i < yBlocks.length; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    // Alter value of each pixel
                    yBlocks[i][j][k] = Math.round((float) yBlocks[i][j][k] / yTable[j][k]);
                }
            }
        }
        for (int i = 0; i < cbBlocks.length; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    // Alter value of each pixel
                    cbBlocks[i][j][k] = Math.round((float) cbBlocks[i][j][k] / cbCrTable[j][k]);
                    crBlocks[i][j][k] = Math.round((float) crBlocks[i][j][k] / cbCrTable[j][k]);
                }
            }
        }

        blocksY = yBlocks;
        blocksCb = cbBlocks;
        blocksCr = crBlocks;
    }

    public void deQuantization(int[][][] blocksY, int[][][] blocksCb, int[][][] blocksCr) {
        int[][][] yBlocks = blocksY;
        int[][][] cbBlocks = blocksCb;
        int[][][] crBlocks = blocksCr;

        int[][] yTable = new int[][] { { 4, 4, 4, 8, 8, 16, 16, 32 }, { 4, 4, 4, 8, 8, 16, 16, 32 },
                { 4, 4, 8, 8, 16, 16, 32, 32 }, { 8, 8, 8, 16, 16, 32, 32, 32 }, { 8, 8, 16, 16, 32, 32, 32, 32 },
                { 16, 16, 16, 32, 32, 32, 32, 32 }, { 16, 16, 32, 32, 32, 32, 32, 32 },
                { 32, 32, 32, 32, 32, 32, 32, 32 } };

        int[][] cbCrTable = new int[][] { { 8, 8, 8, 16, 32, 32, 32, 32 }, { 8, 8, 8, 16, 32, 32, 32, 32 },
                { 8, 8, 16, 32, 32, 32, 32, 32 }, { 16, 16, 32, 32, 32, 32, 32, 32 },
                { 32, 32, 32, 32, 32, 32, 32, 32 }, { 32, 32, 32, 32, 32, 32, 32, 32 },
                { 32, 32, 32, 32, 32, 32, 32, 32 }, { 32, 32, 32, 32, 32, 32, 32, 32 } };

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                yTable[i][j] *= Math.pow(2, compVal);
                cbCrTable[i][j] *= Math.pow(2, compVal);
            }
        }

        for (int i = 0; i < yBlocks.length; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    // Alter value of each pixel
                    yBlocks[i][j][k] = Math.round((float) yBlocks[i][j][k] * yTable[j][k]);
                }
            }
        }
        for (int i = 0; i < cbBlocks.length; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    // Alter value of each pixel
                    cbBlocks[i][j][k] = Math.round((float) cbBlocks[i][j][k] * cbCrTable[j][k]);
                    crBlocks[i][j][k] = Math.round((float) crBlocks[i][j][k] * cbCrTable[j][k]);
                }
            }
        }

        blocksY = yBlocks;
        blocksCb = cbBlocks;
        blocksCr = crBlocks;
    }

    // subSampleCbCr
    // - Description: Since Cb & Cr vals aren't nearly as important as Y val we
    // reduce it by using 4:2:0 (MPEG1) chrominance subsampling scheme.
    private void subSampleCbCr(int[][] cb, int[][] cr) {
        int[][] auxCb = new int[cb.length][cb[0].length];
        int[][] auxCr = new int[cr.length][cr[0].length];

        for (int row = 0, newRow = 0; row < cb.length - 1; row += 2, newRow++) {
            for (int column = 0, newColumn = 0; column < cb[row].length - 1; column += 2, newColumn++) {
                auxCb[newRow][newColumn] = (int) ((cb[row][column] + cb[row][column + 1] + cb[row + 1][column]
                        + cb[row + 1][column + 1]) / 4.0);
                auxCr[newRow][newColumn] = (int) ((cr[row][column] + cr[row][column + 1] + cr[row + 1][column]
                        + cr[row + 1][column + 1]) / 4.0);
            }
        }

        cb = auxCb;
        cr = auxCr;
    }

    //////////// MARK: - Decoding //////////
    // superSamplingCbCr
    // - Description: Purpose is to retrieve a CbCr prior to the state of
    //////////// subsampling
    // by distributing the avg CbCr values to 4 px.
    private void superSample(int[][] cb, int[][] cr) {
        int[][] auxCb = new int[cb.length][cb[0].length];
        int[][] auxCr = new int[cr.length][cr[0].length];

        // Expand cb cr
        for (int row = 0, i = 0; row < cb.length - 1; row += 2, i++) {
            for (int column = 0, j = 0; column < cb[row].length - 1; column += 2, j++) {
                auxCb[row][column] = cb[i][j];
                auxCb[row][column + 1] = cb[i][j];
                auxCb[row + 1][column] = cb[i][j];
                auxCb[row + 1][column + 1] = cb[i][j];
            }
        }

        cb = auxCb;
        cr = auxCr;
    }

    // inverseColorTransform
    // - Description: Inverses the color space from yCbCr values to rgb values
    private void inverseColorTransform(int[][] y, int[][] cb, int[][] cr, MImage newImg) {
        int[] pixel = new int[3];

        double yVal, cbVal, crVal;
        for (int row = 0; row < newImg.getH(); row++) {
            for (int column = 0; column < newImg.getW(); column++) {
                yVal = y[row][column] + 128;
                cbVal = cb[row][column] + 0.5;
                crVal = cr[row][column] + 0.5;

                pixel[0] = (int) (1.0 * yVal + 0 * cbVal + 1.4020 * crVal);
                pixel[1] = (int) (1.0 * yVal - 0.3441 * cbVal - 0.7141 * crVal);
                pixel[2] = (int) (1.0 * yVal + 1.7720 * cbVal + 0 * crVal);

                // Bound r value
                if (pixel[0] > 255)
                    pixel[0] = 255;
                else if (pixel[0] < 0)
                    pixel[0] = 0;
                // Bound g val
                if (pixel[1] > 255)
                    pixel[1] = 255;
                else if (pixel[1] < 0)
                    pixel[1] = 0;
                // Bound b val
                if (pixel[2] > 255)
                    pixel[2] = 255;
                else if (pixel[2] < 0)
                    pixel[2] = 0;

                newImg.setPixel(column, row, pixel);

            }
        }
    }

    private MImage paddImage(MImage img) {
        widthO = img.getW();
        heightO = img.getH();

        if (widthO % 8 != 0) {
            newW = (widthO / 8 + 1) * 8;
        }
        if (heightO % 8 != 0) {
            newH = (heightO / 8 + 1) * 8;
        }

        MImage paddedImg = new MImage(newW, newH);

        int[] rgb = new int[3];

        for (int i = 0; i < heightO; i++) {
            for (int j = 0; j < widthO; j++) {
                img.getPixel(j, i, rgb);
                paddedImg.setPixel(j, i, rgb);
            }
        }

        return paddedImg;
    }
}
