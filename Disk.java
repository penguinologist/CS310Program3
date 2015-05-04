package program3;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Disk class Stores all the data
 * 
 * @author Jeroen
 */
public class Disk {
	private int sectorCount; // sectors on the disk
	private int sectorSize; // characters in a sector
	private char[][] store; // all disk data is stored here (array(index) of
							// char arrays)
	private int lastWrittenSector;

	/**
	 * the default constructor
	 */
	public Disk() {
		// for default sectorCount and sectorSize
		// default size is 10,000 sectors on a disk with 512 characters in each
		// sector.
		//
		sectorCount = 10000;
		sectorSize = 512;
		store = new char[10000][512];
		for (char[] i : store) {
			for (char j : i) {
				j = '\000';
			}
		}
		lastWrittenSector = 0;
	}

	/**
	 * a useless constructor... but hey, if you want modularity, this is the way
	 * to go
	 * 
	 * @param sectorCount
	 * @param sectorSize
	 */
	public Disk(int sectorCount, int sectorSize) {
		// sectorCount is the number of sectors in the disk and sectorSize is
		// the number of characters in a sector. char[][] store is a reference
		// to the 2-dimensional array which must be allocated by the constructor
		this.sectorCount = sectorCount;
		this.sectorSize = sectorSize;
	}

	/**
	 * returns the sector's content with the provided sector number
	 * 
	 * @param sectorNumber
	 *            to be searched
	 * @return char[] char array of the sector's contents
	 */
	public char[] readSector(int sectorNumber) {
		// sector to buffer
		char[] local = null;
		if (sectorNumber > 0 && sectorNumber < sectorCount - 1) {
			local = store[sectorNumber];
		}
		return local;

	}

	/**
	 * write the buffer to the store at the given sectorNumber location
	 * 
	 * @param sectorNumber
	 *            where the buffer needs to be written
	 * @param buffer
	 *            char array to be written
	 */
	public void writeSector(int sectorNumber, char[] buffer) {
		// buffer to sector
		// store[sectorNumber] = buffer;
		for (int i = 0; i < 512; i++) {
			store[sectorNumber][i] = buffer[i];
		}
		lastWrittenSector = sectorNumber;
	}

	/**
	 * gets the sectorCount
	 * 
	 * @return int value of the number of sectors
	 */
	public int getSectorCount() {
		return sectorCount;
	}

	/**
	 * gets the size of the sector
	 * 
	 * @return int value of the sector size
	 */
	public int getSectorSize() {
		return sectorSize;
	}

	/**
	 * gets the store variable. Only used for the creation of the index
	 * 
	 * @return char[][] double char array with the contents of the disk
	 */
	public char[][] getStore() {
		return store;
	}

	/**
	 * gets the last written sector number
	 * 
	 * @return int value of the last written sector
	 */
	public int getLastWrittenSector() {
		return lastWrittenSector;
	}

	/**
	 * this method just writes the entire disk to a file (simple representation)
	 */
	public void write() {
		try {
			String filename = "output.txt";
			FileWriter fw = new FileWriter(filename, false);
			for (int i = 0; i < sectorCount; i++) {
				fw.write("sector #" + i + ":" + new String(store[i]) + "\n");
			}
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}

	}
}