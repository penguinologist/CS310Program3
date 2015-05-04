package program3;

/**
 * @author Jeroen
 *
 */
public class IndexedFile {
	private Disk disk; // disk on which the file will be written
	private char[] buffer; // disk buffer --- this is where everything needs to
							// be written to before it gets written to the disk
	// fields describing data portion of file
	private int overflowStart; // sector number where overflow begins
	private int overflowSectors; // count of overflow sectors in use

	// fields describing index portion of file
	private int indexStart; // sector number where index begins
	private int indexRoot; // sector number of root of index

	/**
	 * the constructor taking in a ton of parameters that I don't even use
	 * really....
	 * 
	 * @param disk
	 * @param recordSize
	 * @param keySize
	 * @param firstAllocated
	 * @param indexStart
	 * @param indexSectors
	 * @param indexRoot
	 * @param indexLevels
	 */
	public IndexedFile(Disk disk, int recordSize, int keySize,
			int firstAllocated, int indexStart, int indexSectors,
			int indexRoot, int indexLevels) {
		this.disk = disk;
		this.indexRoot = indexRoot;
		this.indexStart = indexStart;
		this.overflowStart = indexRoot + 1;
		this.overflowSectors = 0;
	}

	/**
	 * this method inserts a record and returns a boolean whether or not it
	 * already existed before or not
	 * 
	 * @param record
	 *            to be inserted
	 * @return boolean value indicating success
	 */
	public boolean insertRecord(char[] record) {
		/*
		 * inserts if the record does not exist on the disk already
		 */
		boolean ret = false;
		if (!findRecord(record)) {
			char[] key = new String(record).substring(0, 27).trim()
					.toCharArray();
			int sector = getSector(new String(key));
			int numEmptySpaces = getEmptySpaces(sector);
			if (numEmptySpaces != 0) {
				// write in sector
				int i = 8 - numEmptySpaces;
				// need to move over this number of spaces to get to the first
				// empty record position
				for (int j = 0; j < 60; j++) {
					buffer[60 * i + j] = record[j];
				}
				disk.writeSector(sector, buffer);
				// System.out.println("written to empty space");
			} else {
				// write in overflow
				numEmptySpaces = getEmptySpaces(overflowStart + overflowSectors);
				if (numEmptySpaces != 0) {
					int i = 8 - numEmptySpaces;
					// need to move over this number of spaces to get to the
					// first
					// empty record position
					for (int j = 0; j < 60; j++) {
						buffer[60 * i + j] = record[j];
					}
					disk.writeSector(overflowSectors + overflowStart, buffer);

				} else {
					overflowSectors++;
					for (int j = 0; j < 60; j++) {
						buffer[overflowSectors + overflowStart + j] = record[j];
					}

					disk.writeSector(overflowSectors + overflowStart, buffer);
				}

			}
			ret = true;

		}

		return ret;

	}

	private int getEmptySpaces(int sector) {
		buffer = disk.readSector(sector);
		int count = 0;
		for (int i = 0; i < 8; i++) {
			if (buffer[i * 60] != '\000') {
				count++;
			}
		}
		return 8 - count;
	}

	/**
	 * this method will tell you if a record exists
	 * 
	 * @param record
	 *            to be sought
	 * @return boolean value indicating success
	 */
	public boolean findRecord(char[] record) {

		// returns true if it finds it, false if otherwise.
		String temp = new String(record).substring(0, 27).trim();

		int sector = getSector(temp);
		char[] key = temp.toCharArray();

		char[] buffer = disk.readSector(sector);
		// System.out.println("sector nr " + sector);
		// System.out.println("buffer="+new String(buffer));
		for (int i = 0; i < 8; i++) {// go through all 8 records in the sector
			char[] local = new char[27];
			int p = 0;
			// copy the key
			for (int j = i * 60; j < (i * 60) + 27; j++) {
				local[p] = buffer[j];
				p++;
			}
			// System.out.println("local="+new String(local));

			// quit when key starts with '\000'
			if (local[0] == '\000') {
				// System.out.println("end of args. breaking...");
				return false;
			}
			// check if the key matches
			if (compare(key, local) == 0) {
				return true;
			}

		}
		// for loop going through the overflow
		System.out.println("continuing to the overflow sectors...");
		sector = overflowStart;
		buffer = disk.readSector(sector);
		while (buffer[0] != '\000') {
			buffer = disk.readSector(sector);
			for (int i = 0; i < 8; i++) {// go through all 8 records in the
											// sector
				char[] local = new char[27];
				int p = 0;
				// copy the key
				for (int j = i * 60; j < (i * 60) + 27; j++) {
					local[p] = buffer[j];
					p++;
				}

				// quit when key starts with '\000'
				if (local[0] == '\000') {
					return false;
				}
				// check if the key matches
				if (compare(key, local) == 0) {
					return true;
				}

			}

			sector++;
		}

		return false;
		/*
		 * 
		 */
	}

	/**
	 * documenting because it's important enough. This method gets the sector
	 * with a key passed in.
	 * 
	 * @param key
	 *            to be used as search tool
	 * @return the sector where the record containing the key should be stored
	 *         (not guaranteed, but this is where it should be stored)
	 */
	private int getSector(String key) // returns sector number
										// indicated by key
	{
		key += "                                   ";// add spaces, enough to
														// cover the 27 minimum
		key = key.substring(0, 27);// get rid of any extra
		char[] k = key.toCharArray();// then convert to char array
		/*
		 * if it's greater or equal to the first key and less than the next key,
		 * move down a layer
		 * 
		 * else move on to the next key and repeat.
		 */
		int datasector = indexRoot;
		char[] sector = new char[512];
		char[][] keys = new char[15][27];
		char[][] sectornrs = new char[15][6];
		sector = disk.readSector(datasector);
		// get the root index...
		for (int i = 0; i < 15; i++) {
			keys[i] = getPartKey(sector, i);
		}
		for (int i = 0; i < 15; i++) {
			sectornrs[i] = getPartIndex(sector, i);
		}

		boolean brk = false;

		for (int i = 0; i < 14 && !brk; i++) {
			// check the key agains the keys we have
			int p = compare(k, keys[i]);
			int q = compare(k, keys[i + 1]);
			if ((p >= 0 && q < 0) || (q >= 0 && keys[i + 1][0] == '\000')) {
				// we've got a match.
				// get the sector nr
				int number = Integer.valueOf(new String(sectornrs[i]).trim());
				if (number > 1000 && number < indexStart) {
					datasector = number;
					brk = true;
				} else {
					/*
					 * proceed to check which one of the next row we need.
					 */
					for (int w = 0; w < 14; w++) {

						sector = disk.readSector(number + w);
						char[] t = getPartKey(sector, 0);
						if (w > 1 && compare(t, keys[w - 1]) >= 0) {
							keys[w] = getPartKey(sector, w);
							sectornrs[w] = getPartIndex(sector, w);
						} else {
							for (int r = 0; r < 27; r++) {
								keys[w][r] = '\000';
							}
							for (int r = 0; r < 6; r++) {
								sectornrs[w][r] = '\000';
							}
						}
					}
					// brk=true;
				}
			}

		}
		// after the code above we end up at the index of the next index where
		// there are 15 more possibilities inside the object. That's what we
		// check here
		// read the next 15 lines into an array of char[]
		char[][] buffers = new char[15][512];
		for (int i = 0; i < 15; i++) {
			char[] temp = disk.readSector(datasector + i);
			for (int j = 0; j < 512; j++) {
				buffers[i][j] = temp[j];
			}
		}

		// get the first key of each buffer
		keys = new char[15][27];
		sectornrs = new char[15][6];

		for (int i = 0; i < 15; i++) {
			int j = 0;
			for (; j < 27; j++) {
				keys[i][j] = buffers[i][j];
			}
			int p = 0;
			for (; j < 33; j++) {
				sectornrs[i][p] = buffers[i][j];
				p++;
			}
		}
		for (int i = 0; i < 14; i++) {
			int p = compare(k, keys[i]);
			int q = compare(k, keys[i + 1]);
			// the item is in this, or falls between the keys.
			if ((p >= 0 && q < 0) || (q >= 0 && keys[i + 1][0] == '\000')) {
				// set all the keys equal to the line at datasector+i
				datasector += i;
				// sector = disk.readSector(datasector);
				i = 15;
			}
		}

		// parse the records in this sector
		keys = new char[15][27];
		sectornrs = new char[15][6];
		for (int i = 0; i < 8; i++) {
			int j = 0;

			char[] strk = getPartKey(sector, i);
			char[] stri = getPartIndex(sector, i);
			for (; j < 27; j++) {
				keys[i][j] = strk[j];
			}
			int p = 0;
			for (; j < 33; j++) {
				sectornrs[i][p] = stri[p];
				p++;
			}
		}

		// go through each record in this index to find where it would belong
		for (int i = 0; i < 15; i++) {
			int p = compare(k, keys[i]);
			int q = compare(k, keys[i + 1]);

			// the item is in this, or falls between the keys.
			if ((p >= 0 && q < 0) || (q >= 0 && keys[i + 1][0] == '\000')) {
				// set all the keys equal to the line at datasector+i
				int num = 0;

				datasector += num;// set it equal to the number followed by the
									// key...
				i = 15;
			}
		}
		// inside this index record, find the sector to which the key would
		// belong
		sector = disk.readSector(datasector);

		for (int i = 0; i < 15; i++) {
			int j = 0;

			char[] strk = getPartKey(sector, i);
			char[] stri = getPartIndex(sector, i);
			for (; j < 27; j++) {
				keys[i][j] = strk[j];
			}
			int p = 0;
			for (; j < 33; j++) {
				sectornrs[i][p] = stri[p];
				p++;
			}
		}
		if (datasector >= 1000 && datasector < indexStart) {
			return datasector;

		}
		for (int i = 0; i < 14; i++) {
			int p = compare(k, keys[i]);
			int q = compare(k, keys[i + 1]);
			// the item is in this, or falls between the keys.
			if ((p >= 0 && q < 0)) {
				// set all the keys equal to the line at datasector+i
				datasector = Integer.parseInt(new String(
						getPartIndex(sector, i)).trim());// set it equal to the
															// number followed
															// by the key...
				i = 15;
			} else if ((q >= 0) && keys[i + 1][0] == '\000') {
				// the next index doesn't exist...
				datasector = Integer.parseInt(new String(getPartIndex(sector,
						i + 1)).trim());// set it equal to the number followed
										// by the key...
				i = 15;
			} else if (i == 13) {
				datasector = Integer.parseInt(new String(getPartIndex(sector,
						i + 1)).trim());
			}
		}
		return datasector;
	}

	private char[] getPartIndex(char[] chunk, int index) {
		char[] part = new char[6];
		for (int i = 0; i < 6; i++) {
			part[i] = chunk[34 * index + 27 + i];
		}
		return part;
	}

	private char[] getPartKey(char[] chunk, int index) {
		char[] part = new char[27];
		for (int i = 0; i < 27; i++) {
			part[i] = chunk[index * 34 + i];
		}
		return part;
	}

	private int compare(char[] k1, char[] k2) {
		String t1 = new String(k1).trim();
		String t2 = new String(k2).trim();
		return t1.compareToIgnoreCase(t2);

	}

}