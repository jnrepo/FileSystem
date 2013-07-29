/*
 * Directory Entry object. This object contains information needed for directory entries.
 * will be a array object within FS. Contains pointers on where to write and read.
 * 
 * Contains:
 * name of file
 * index of file correlated file descriptor
 * 
 * location in memory = locInBlock + (indexInDE mod 2)*4
 */

public class FS_DirectoryEntry {

	public String name_s;
	public char[] name;
	public int fdIndex;
	public _IO IO;

	public int whichBlock; // whichBlock = dirIndex/8
	public int locInBlock; // locInBlock = (dirIndex mod 8)*8
	public int dirIndex;

	public FS_DirectoryEntry(_IO IO, int dirIndex) {
		this.IO = IO;
		this.dirIndex = dirIndex;

		// location of the directory entry in memory
		// needs + 8 for which block, since directory entries are in the first file ldisk[k+1]
		this.whichBlock = dirIndex / 8 + 8;
		this.locInBlock = (dirIndex % 8) * 8;

		// these values mean that there is no directory entry
		this.name = null;
		this.fdIndex = -1;
	}
	
	/**
	 * Writes the directory entry to the disk, based on values
	 * 
	 * @param filename
	 * @param fdLoc
	 */
	public void write(char[] filename, int fdLoc ) {
		
		name = new char[4];
		name = filename;
		
		name_s = name.toString();
		fdIndex = fdLoc;
		
		byte[] filenameBytes = new byte[4];
		for ( int i = 0; i < 4; i++ ) {
			filenameBytes[i] = (byte) name[i];
		}
		
		IO.write_blockInt(whichBlock, locInBlock, filenameBytes);
		IO.write_blockInt(whichBlock, locInBlock + 4, pack(fdLoc));
	}
	
	/*
	 * Below methods are only used to convert integer into a byte[]
	 * for easier write and read.
	 */

	// Pack the 4-byte integer val into the four bytes mem[loc]...mem[loc+3].
	// The most significant porion of the integer is stored in mem[loc].
	// Bytes are masked out of the integer and stored in the array, working
	// from right(least significant) to left (most significant).
	public byte[] pack(int val) {
		byte[] intInByte = new byte[4];

		final int MASK = 0xff;
		for (int i = 3; i >= 0; i--) {
			intInByte[i] = (byte) (val & MASK);
			val = val >> 8;
		}

		return intInByte;
	}

	// Unpack the four bytes mem[loc]...mem[loc+3] into a 4-byte integer,
	// and return the resulting integer value.
	// The most significant porion of the integer is stored in mem[loc].
	// Bytes are 'OR'ed into the integer, working from left (most significant)
	// to right (least significant)
	int unpack(byte[] convertToInt) {
		final int MASK = 0xff;
		int v = (int) convertToInt[0] & MASK;
		for (int i = 1; i < 4; i++) {
			v = v << 8;
			v = v | ((int) convertToInt[i] & MASK);
		}
		return v;
	}
}
