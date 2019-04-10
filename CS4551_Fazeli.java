
/*******************************************************
 * CS4551 Multimedia Software Systems @ Author: Bijan Fazeli
 * 
 * 
 * Main Execution of Program - Performs grayscale, and color quantization on img
 *******************************************************/

import java.util.Scanner;

public class CS4551_Fazeli {
	public static void main(String[] args) {
		// the program expects one commandline argument
		// if there is no commandline argument, exit the program
		if (args.length != 1) {
			usage();
			System.exit(1);
		}

		System.out.println("\n--Welcome to Multimedia Software System--\n");

		// Create an Image object with the input PPM file name.
		API api = new API();
		int[][] lut = new int[256][3];

		MImage img = new MImage(args[0]);
		Scanner reader = new Scanner(System.in);
		String imgName = img.getName().substring(0, args[0].indexOf("."));
		System.out.println(img);

		boolean tableGenerated = false;

		while (true) {
			System.out.println();

			switch (api.getInputFromUser(reader)) {
			case 1:
				break;
			case 2:
				break;	
			case 3:
				api.convertImgToGrayScaleImg(img);
				System.out.println();
				img.write2PPM(imgName + "-gray.ppm");
				break;
			case 4:
				if (!tableGenerated) {
					api.generateLUT(lut);
					tableGenerated = true;
				}

				api.convertImgToGrayScaleIndex(img);
				img.write2PPM(imgName + "-index.ppm");
				api.convertImgToQuantized(img, lut);
				img.write2PPM(imgName + "-QT8.ppm");
				break;
			case 5:
				System.out.println("\n---Good Bye---\n");
				System.exit(1);
			default:
				break;
			}

			img = new MImage(args[0]);
		}
	}

	public static void usage() {
		System.out.println("\nUsage: java CS4551_Main [input_ppm_file]\n");
	}

}
