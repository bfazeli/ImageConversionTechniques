
/**
 * API  @ Author: Bijan Fazeli
 * 
 * API - Provides encapsulation of various actions
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class API {

    public int compVal;
    private int newH, newW;
    private int heightO, widthO;
    private boolean width_padded = false;
    private boolean height_padded = false;

    private int[][][] y8x8, cb8x8, cr8x8;

    private int getGrayScaleValueFor(int[] rgb) {
        int gray = (int) Math.round(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);

        if (gray > 255) {
            return 255;
        } else if (gray < 0) {
            return 0;
        }

        return gray;
    }

    private int getGrayScaleValueFor8bitConversion(int[] rgb) {

        return (Integer.parseInt(String.format("%3s", Integer.toBinaryString(rgb[0] / 32)).replace(' ', '0')
                + String.format("%3s", Integer.toBinaryString(rgb[1] / 32)).replace(' ', '0')
                + String.format("%2s", Integer.toBinaryString(rgb[2] / 64)).replace(' ', '0'), 2));
    }

    public void convertImgToGrayScaleImg(MImage img) {
        int grayScaleValue;
        int height = img.getH(), width = img.getW();
        int[] current_rgb = new int[3];

        for (int row = 0; row < width; ++row) {
            for (int column = 0; column < height; ++column) {
                img.getPixel(row, column, current_rgb);
                grayScaleValue = getGrayScaleValueFor(current_rgb);
                img.setPixel(row, column, new int[] { grayScaleValue, grayScaleValue, grayScaleValue });
            }
        }
    }

    public void convertImgToQuantized(MImage img, int[][] lut) {

        int height = img.getH(), width = img.getW();
        int[] current_rgb = new int[3];

        for (int row = 0; row < width; ++row) {
            for (int column = 0; column < height; ++column) {
                img.getPixel(row, column, current_rgb);
                img.setPixel(row, column,
                        new int[] { lut[current_rgb[0]][0], lut[current_rgb[0]][1], lut[current_rgb[0]][2] });
            }
        }
    }

    public void convertImgToGrayScaleIndex(MImage img) {
        int grayScaleValue;
        int height = img.getH(), width = img.getW();
        int[] current_rgb = new int[3];

        for (int row = 0; row < width; ++row) {
            for (int column = 0; column < height; ++column) {
                img.getPixel(row, column, current_rgb);
                grayScaleValue = getGrayScaleValueFor8bitConversion(current_rgb);
                img.setPixel(row, column, new int[] { grayScaleValue, grayScaleValue, grayScaleValue });
            }
        }
    }

    public int getInputFromUser(Scanner reader) {
        System.out.print("Main Menu-----------------------------------\n" + "1. VQ (Vector Quantization)\n"
                + "2. DCT-based Coding\n" + "3. Conversion to Gray-scale Image (24bits->8bits)\n"
                + "4. Conversion to 8bit Indexed Color Image using Uniform Color\n"
                + "   Quantization (24bits->8bits)\n" + "5. Quit\n" + "\nPlease enter the task number [1-3]: ");
        return reader.nextInt();
    }

    public void setCompressionValue(Scanner reader) {
        do {
            System.out.print("Give value of n [0-5]: ");
            compVal = reader.nextInt();
        } while ((compVal < 0 || compVal > 5));
    }

    public void generateLUT(int[][] lut) {
        String bits, padding = "";

        HashMap<String, Integer> rgb = new HashMap<>();
        rgb.put("red", 0);
        rgb.put("green", 0);
        rgb.put("blue", 0);

        System.out.println("\nLUT by UCQ");
        System.out.println("Index\t\tR\t  G \t    B");
        System.out.println("-------------------------------------------");
        for (int i = 0; i < 256; i++) {
            bits = Integer.toBinaryString(i);

            for (int j = 0; j < (8 - bits.length()); j++) {
                padding += "0";
            }

            bits = padding + bits;

            lut[i][0] = (Integer.parseInt(bits.substring(0, 3), 2) * 32 + 16);
            lut[i][1] = (Integer.parseInt(bits.substring(3, 6), 2) * 32 + 16);
            lut[i][2] = (32 * ((Integer.parseInt(bits.substring(6, 8), 2) * 2 + 1)));

            System.out.println(String.format("%-3s%15s%10s%10s", i, lut[i][0], lut[i][1], lut[i][2]));

            padding = "";

        }
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
        

    }

    public MImage paddOrigImage(MImage img) {
        
        int width = img.getW();
        int height = img.getH();
        int origWidth = width;
        int origHeight = height;

        //If a dimension is not even, pad
        if (width % 2 != 0) {
            width++;
            width_padded = true;
        }
        if (height % 2 != 0) {
            height++;
            height_padded = true;
        }

        //Create a new image with padding
        MImage paddedImg = new MImage(width, height);
        //Pass the info in the old image into the new one
        int[] rgb = new int[3];
        for (int i = 0; i < origHeight; i++) {
            for (int j = 0; j < origWidth; j++) {
                img.getPixel(j, i, rgb);
                paddedImg.setPixel(j, i, rgb);
            }
        }

        return paddedImg;
    }

    public static ArrayList<int[]> convertImageToInputVectors(MImage img) {
        //Loop through the pixels 2X2
        int[] rgb = new int[3];

        ArrayList<int[]> vectors = new ArrayList<>();
        int [] vector = new int[12];

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

        int [][] decodedY = expandTo2D(inverseTransform(y8x8));
        int [][] decodedCb = expandTo2D(inverseTransform(cb8x8));
        int [][] decodedCr = expandTo2D(inverseTransform(cr8x8));

        System.out.println(Arrays.toString(decodedY[decodedY.length-1]));

        superSample(decodedCb, decodedCr);

        MImage decodedImage = new MImage(widthO, heightO);
        System.out.println(decodedY[0].length + " " + decodedCb[0].length + " " + decodedCr[0].length);

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
                            summation += cu * cv * blocks[i][u][v] * Math.cos((((2 * x) + 1) * u * Math.PI) / 16) * Math.cos((((2 * y) + 1) * v * Math.PI) / 16);
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
                auxCb[newRow][newColumn] = 
                    (int) ((cb[row][column] 
                    + cb[row][column + 1] 
                    + cb[row + 1][column] 
                    + cb[row + 1][column + 1]) / 4.0);
                auxCr[newRow][newColumn] = 
                    (int) ((cr[row][column] 
                    + cr[row][column + 1] 
                    + cr[row + 1][column] 
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

        // Reassign the auxCbCr distributions to the cb and cr passed in
        // for (int row = 0; row < cb.length; row++) {
        //     for (int column = 0; column < cb[row].length; column++) {
        //         cb[row][column] = auxCb[row][column];
        //         cr[row][column] = auxCr[row][column];
        //     }
        // }

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

                // System.out.println(Arrays.toString(pixel));

                // img.getPixel(row, column, prevPixel);
                // System.out.println("prev: " + Arrays.toString(prevPixel));

                newImg.setPixel(column, row, pixel);
                // System.out.println("new: " + Arrays.toString(pixel));

            }
            System.out.print(Arrays.toString(pixel));
        }
        System.out.println();
    }

    private MImage paddImage(MImage img) {
        widthO = img.getW();
        heightO = img.getH();

        if (widthO % 8 != 0) {
            newW = (widthO / 8 + 1) * 8;
            System.out.println("width_padded:" + newW);
        }
        if (heightO % 8 != 0) {
            newH = (heightO / 8 + 1) * 8;
            System.out.println("height_padded:" + newH);
        }

        MImage paddedImg = new MImage(newW, newH);

        int[] rgb = new int[3];

        // Copy the pixels from img to the new paddedImg
        for (int i = 0; i < heightO; i++) {
            for (int j = 0; j < widthO; j++) {
                img.getPixel(j, i, rgb);
                paddedImg.setPixel(j, i, rgb);
            }
        }

        return paddedImg;
    }
}
