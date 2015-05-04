/**
 * 
 */
package program3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author Jeroen
 *
 */
public class Main {

	/**
	 * main executable
	 * @param args
	 */
	public static void main(String[] args) {

		LoaderAndIndexer lni = new LoaderAndIndexer();

		File db = new File("mountains.txt");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File("output.txt"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		writer.print("");
		writer.close();
		IndexedFile index;

		try {
			System.out.println("loading db to disk");
			lni.loadOntoDisk(db);
			System.out.println("loaded db to disk, creating index....");
			index = lni.createIndex();
			System.out.println("index completed");

		} catch (FileNotFoundException e) {
			System.out.println("File not found. exiting...");
			return;
		}
		boolean cont = true;
		Scanner scan = new Scanner(System.in);
		while (cont) {
			showMenu();
			String input = scan.nextLine();
			if (input.equals("1")) {
				// inserting
				System.out.println("What's the name of the mountain?");
				String name = scan.nextLine();
				while (name.length() == 0) {
					System.out.println("enter a valid name");
					name = scan.nextLine();
				}
				System.out
						.println("What's the country in which the mountain is situated?");
				String country = scan.nextLine();
				while (name.length() == 0) {
					System.out.println("Enter a valid country");
					country = scan.nextLine();
				}
				System.out.println("How tall is this 'mountain'?");
				String altitude = scan.nextLine();
				boolean valid = false;

				try {
					int p = Integer.valueOf(altitude);
					valid = true;
				} catch (Exception e) {
					valid = false;
				}
				while (altitude.length() == 0 || !valid) {
					try {
						int o = Integer.valueOf(altitude);
						valid = true;
					} catch (Exception e) {
						System.out.println("Enter a valid value");
						altitude = scan.nextLine();
					}

				}
				// got all the data. adding extra blank space then truncating to
				// the right length (prevents nullpointers)

				name += "                           ";
				country += "                           ";
				altitude += "      ";

				name = name.substring(0, 27);
				country = country.substring(0, 27);
				altitude = altitude.substring(0, 6);

				char[] record = new char[60];
				int j = 0;
				for (int i = 0; i < 27; i++) {
					record[j] = name.toCharArray()[i];
					j++;
				}
				for (int i = 0; i < 27; i++) {
					record[j] = country.toCharArray()[i];
					j++;
				}
				for (int i = 0; i < 6; i++) {
					record[j] = altitude.toCharArray()[i];
					j++;
				}

				// insert on the disk
				if (!index.insertRecord(record)) {
					System.out
							.println("This mountain already exists in the database.");
				}

			} else if (input.equals("2")) {
				// finding
				System.out
						.println("What's the name of the mountain you're looking for?");
				String name = scan.nextLine();
				while (name.length() == 0) {
					System.out.println("Enter a valid name");
					name = scan.nextLine();
				}
				name += "                           ";
				name = name.substring(0, 27);
				if (index.findRecord(name.toCharArray())) {
					System.out.println("Yep! We've got one of those here");
				} else {
					System.out.println("Nope, can't find that here...");
				}

			} else if (input.equals("3")) {
				cont = false;// quitting
				// emptying the output file, prepping for rewrite to get the
				// latest version of the disk...
				try {
					writer = new PrintWriter(new File("output.txt"));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				writer.print("");
				writer.close();

				lni.disk.write();

			} else {
				System.out.println("Please make a valid selection");
			}
		}
	}

	private static void showMenu() {
		System.out.println("Main menu:");
		System.out.println("1) Insert record");
		System.out.println("2) Find record");
		System.out.println("3) Quit");
		System.out.println("Enter one of the option numbers to proceed.");
	}

}
