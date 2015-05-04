/**
 * 
 */
package program3;

/**
 * @author Jeroen
 *
 */
public class Index {

	// list of keys
	private char[][] keys;
	// list of sector numbers associated with each key
	private char[][] sectorNumber;

	int count;
	int max;

	/**
	 * constructor
	 * 
	 * @param n
	 *            number of ways the tree can branch
	 */
	public Index(int n) {
		setKeys(new char[n][27]);
		setSectorNumber(new char[n][7]);
		count = 0;
		max = n;
	}

	/**
	 * @return the keys
	 */
	public char[][] getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(char[][] keys) {
		this.keys = keys;
	}

	public boolean addData(char[] key, char[] i) {

		// check if there's room left
		if (count == max) {
			return false;
		}

		// set the key
		keys[count] = key;
		sectorNumber[count] = i;
		// increment the counter
		count++;
		return true;
	}

	/**
	 * @return the sectorNumber
	 */
	public char[][] getSectorNumber() {
		return sectorNumber;
	}

	/**
	 * @param sectorNumbers
	 *            the sectorNumber to set
	 */
	public void setSectorNumber(char[][] sectorNumbers) {
		this.sectorNumber = sectorNumbers;
	}

	public char[] toCharArray() {
		char[] temp = new char[512];

		for (int i = 0; i < 512; i++) {
			temp[i] = '\000';
		}

		int i = 0;
		for (; i < count; i++) {
			int j = 0;
			for (j = 0; j < 27; j++) {// copy the key to temp
				temp[(34 * i) + j] = keys[i][j];
			}

			for (j = 27; j < 33; j++) {// copy the sectornumber

				temp[(34 * i) + j] =  sectorNumber[i][j-27];
			}
//			System.out.println("temp=" + new String(temp));
		}

		return temp;
	}
}
