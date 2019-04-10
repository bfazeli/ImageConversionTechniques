
/**
 * API  @ Author: Bijan Fazeli
 * 
 * API - Provides encapsulation of various actions
 */

import java.util.HashMap;
import java.util.Scanner;

public class API {

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
        System.out.print(
                "Main Menu-----------------------------------\n" + "1. Conversion to Gray-scale Image (24bits->8bits)\n"
                        + "2. Conversion to 8bit Indexed Color Image using Uniform Color\n"
                        + "   Quantization (24bits->8bits)\n" + "3. Quit\n" + "\nPlease enter the task number [1-3]: ");
        return reader.nextInt();
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

    

    // Task 2
    public static void getDCT(String input_image) {
        MImage img = encodingE1(input_image);
        // MImage decodedImg = decodingD4(img);
        img = encodingE2(img);
        img = decodingD3(img);

    }

    public static MImage encodingE1(String input_image) {
        
        MImage img = new MImage(input_image);
        
        MImage paddedImg = paddImage(img);

        int[][] y = new int[paddedImg.getW()][paddedImg.getH()];
        int[][] cb = new int[paddedImg.getW()][paddedImg.getW()];
        int[][] cr = new int[paddedImg.getW()][paddedImg.getW()];

        update(y, cb, cr, paddedImg);


        return img;
    }

    public static void update(int[][] y, int[][] cb, int[][] cr, MImage img) {
        
    }

    public static MImage paddImage(MImage img) {
        int width = img.getW();
        int height = img.getH();
        
        if (width % 8 != 0) {
            width = (width / 8 + 1) * 8;
            System.out.println("width_padded:" + width);
        }
        if (height % 8 != 0) {
            height = (height / 8 + 1) * 8;
            System.out.println("height_padded:" + height);
        }

        MImage paddedImg = new MImage(width, height);

        // Fill last row with 0s
        int[] blackPixel = new int[] { 0, 0, 0 };
        if (paddedImg.getH() > img.getH()) {
            for (int i = 0; i < paddedImg.getW(); i++) {
                img.setPixel(i, paddedImg.getH() - 1, blackPixel);
            }
        }
        // Fill last width with 0s
        if (paddedImg.getW() > img.getW()) {
            for (int i = 0; i < paddedImg.getH(); i++) {
                img.setPixel(paddedImg.getW() - 1, i, blackPixel);
            }
        }

        return paddedImg;
    }

    private static MImage decodingD3(MImage img) {
        int width = img.getW();
        int height = img.getH();
        int r = 0, g = 0, b = 0, y = 0, cb = 0, cr = 0;
        int[] rgb = new int[3];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                img.getPixel(i, j, rgb);

                y = rgb[0];
                cb = rgb[1];
                cr = rgb[2];

                r = (int) (y + 128 + 0 + 1.402 * cr);
                g = (int) (y - .3441 * cb - .7141 * cr + .5);
                b = (int) (y + 1.7720 * cb + .5);
                rgb[0] = r;
                rgb[1] = g;
                rgb[2] = b;
                img.setPixel(i, j, rgb);
            }
        }

    }
}