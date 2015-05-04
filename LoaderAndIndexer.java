/**
 * 
 */
package program3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class loads the entire db into
 * 
 * @author Jeroen
 */
public class LoaderAndIndexer {
	public Disk disk;

	/**
	 * default constructor
	 */
	public LoaderAndIndexer() {
		disk = new Disk();
	}

	/**
	 * loads the file onto the db, but throws an exception if the output file
	 * cannot be created for whatever reason
	 * 
	 * @param database
	 *            the file that needs to be read from
	 * @throws FileNotFoundException
	 */
	public void loadOntoDisk(File database) throws FileNotFoundException {

		char[] sectorBuffer = new char[disk.getSectorSize()];// create a sector
																// buffer.
		int bufferIndex = 0; // just the index of where the next record can be
								// written.
								// int recordsWritten = 0; // variable keeping
								// track of the number of
		// // records written (5 max, leaving room for 3
		// // more)
		// need to start writing at 1000, so here we go:
		int write = 1000;
		Scanner scan = new Scanner(database);
		while (scan.hasNextLine()) {
			String der = scan.nextLine();
			String[] line = der.split("#");
			/*
			 * name: 27 characters (the key field) country: 27 characters
			 * altitude (in feet): 6 characters
			 * 
			 * null char = \000
			 */
			char[] name = new char[27];
			char[] country = new char[27];
			char[] altitude = new char[6];
			// deal with name
			char[] temp = line[0].toCharArray();
			int i = 0;
			for (; i < temp.length && i != 27; i++) {
				// copy
				name[i] = line[0].charAt(i);
			}
			for (; i < 26; i++) {
				name[i] = '\000';
			}
			// deal with country
			temp = line[1].toCharArray();
			i = 0;
			for (; i < temp.length && i != 27; i++) {
				// copy
				country[i] = line[1].charAt(i);
			}
			for (; i < 26; i++) {
				country[i] = '\000';
			}
			// deal with altitude
			temp = line[2].toCharArray();
			i = 0;
			for (; i < temp.length && i != 6; i++) {
				// copy
				altitude[i] = line[2].charAt(i);
			}
			for (; i < 6; i++) {
				altitude[i] = '\000';// '\000'
			}
			// ---------------------------------------
			// create a record
			char[] record = new char[60];
			// System.out.println(record.length);
			i = 0;
			int j = 0;
			for (; i < 27; i++) {
				record[i] = name[j];
				j++;
			}
			j = 0;
			for (; j < 27; i++) {
				record[i] = country[j];
				j++;
			}
			j = 0;
			for (; j < 6; i++) {
				record[i] = altitude[j];
				j++;
			}
			for (j = 0; j < 60; j++) {
				sectorBuffer[(bufferIndex * 60) + j] = record[j];
			}
			// add the 3 empty records, the index, and the overflow
			// buffer....
			bufferIndex++;
			if (bufferIndex == 5) {
				bufferIndex = 0;
				disk.writeSector(write, sectorBuffer);
				write++;
				for (int l = 0; l < 512; l++) {
					sectorBuffer[l] = '\000';
				}
			}
		}
		disk.writeSector(write, sectorBuffer);
		scan.close();
	}

	/**
	 * creates an indexed file
	 * 
	 * @return IndexedFile that has been created
	 */
	public IndexedFile createIndex() {
		// go over every sector and get a link to all the indexes
		char[][] data = disk.getStore();
		int firstAlloc = disk.getLastWrittenSector();
		ArrayList<Index> indices = new ArrayList<Index>();
		indices.add(new Index(15));
		int lastIndex = 0;
		for (int i = 0; i <= disk.getLastWrittenSector(); i++) {
			if (data[i][0] != '\000') {
				// we have a valid record that isn't null....
				// get the first 27 chars.
				char[] key = new char[27];
				int j = 0;
				for (; j < 27; j++) {
					key[j] = data[i][j];
				}
				char[] number = new char[6];
				for (j = 0; j < 6; j++) {
					number[j] = data[i][j + 27];
				}
				// add the key and if it fails, create a new index to which the
				// item will be added.
				String in = i + "             ";
				char[] lp = new char[7];
				for (int p = 0; p < 7; p++) {
					lp[p] = in.charAt(p);
				}

				if (!indices.get(lastIndex).addData(key, lp)) {
					// the index is full, creating a new one.
					// System.out.println("created new index");
					indices.add(new Index(15));
					lastIndex++;
					indices.get(lastIndex).addData(key, lp);
				}
			}
		}
		// added all the unique keys to Index items.
		for (Index t : indices) {
			disk.writeSector(disk.getLastWrittenSector() + 1, t.toCharArray());
		}
		ArrayList<Index> nextLayer = null;
		while (indices.size() > 15) {
			nextLayer = new ArrayList<Index>();
			nextLayer.add(new Index(15));
			Index local = new Index(15);
			for (int i = 0; i < indices.size(); i += 14) {
				// try to add a new reference to the next layer
				int last = firstAlloc + i;
				char[] derp = (last + "      ").substring(0, 6).toCharArray();

				if (!local.addData(indices.get(i).getKeys()[0], derp)) {
					// if you're in this if statement, then the addition failed
					// because the index was full. time to add this one to
					// nextLayer and go up one.
					nextLayer.add(local);
					disk.writeSector(disk.getLastWrittenSector() + 1,
							local.toCharArray());// write local to disk
					local = new Index(15);// create a new object
					local.addData(indices.get(i).getKeys()[0], derp);
					System.out.println("created a new index");
				}
			}
			// write to file....
			if (local.getKeys()[0] != null) {
				disk.writeSector(disk.getLastWrittenSector() + 1,
						local.toCharArray());// write it to disk if partial
			}
			indices = nextLayer;
		}
		IndexedFile ind = new IndexedFile(disk, 512, 27, 1000, 1588,
				disk.getLastWrittenSector() - firstAlloc,
				disk.getLastWrittenSector(), 3);
		return ind;
	}

}
