
/*******************************************************
 * CS4551 Multimedia Software Systems @ Author: Bijan Fazeli
 * 
 * 
 * Main Execution of Program - Performs vq and dct on img
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

		MImage img = new MImage(args[0]);
		Scanner reader = new Scanner(System.in);
		String imgName = img.getName().substring(0, args[0].indexOf("."));
		System.out.println(img);

		while (true) {
			System.out.println();

			switch (api.getInputFromUser(reader)) {
			case 1:
				api.performVQ(imgName);
				break;
			case 2:
				api.setCompressionValue(reader);
				api.performDCT(img);
				MImage decodedImg = api.performInverseDCT();
				decodedImg.write2PPM(api.compVal + "-" + imgName + ".ppm");
				break;	
			case 3:
				System.out.println("\n---Good Bye---\n");
				System.exit(1);
				break;
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
