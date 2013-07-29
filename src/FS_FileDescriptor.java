/*
 * File Descriptor Object
 * 
 * When created send _IO object here. All descriptors will have pointers to correct place in disk.
 * 
 * FD has 4 integers = 16 B
 * 1 block = 64 B
 * 4 FD/block
 * 
 * 24 FD total
 * 
 * FD will be a 2 dimensional array FD[index][index in FD]
 * 
 * index/4 = which block
 * (index mod 4) * 16 = location in block
 * (location in block, location in block + 3) area where first integer (length) is stored
 * location in block + 3 + (index in FD mod 4)*4 = location of integer specified within block
 */

public class FS_FileDescriptor {

	public int whichBlock;
	public int locInBlock;
	
	// save these three
	public int index;
	public int length;
	public int[] fdInfo = new int[4];

	public _IO IO;

	// IO is passed, so we are writing on same disk.
	// index determines which block it belongs to, and the location of
	// where the fileDescriptor is within the block
	public FS_FileDescriptor(_IO IO, int index) {
		this.index = index;
		this.IO = IO;

		// add one to which block, since the first is where BitMap is located
		this.whichBlock = index / 4 + 1;

		// there are 4 file descriptors in each block, they are 16 B each
		// this determines where inside the block the file descriptor is
		this.locInBlock = (index % 4) * 16;

		// if the value is -1, that means it is empty
		for (int i = 0; i < fdInfo.length; i++) {
			fdInfo[i] = -1;
		}
	}

	/**
	 * This field writes the value into the specificity index in FD. Also writes
	 * the value into the disk. changes state to TAKEN if FREE. Need to convert
	 * from int into char[]
	 * 
	 * @param indexInFD
	 *            = where to write the value
	 * @param value
	 *            = value to write
	 */
	public void write(int indexInFD, int value) {
		fdInfo[indexInFD] = value;
		
		// if indexInFD = 0, we are inputting length, save for later
		if ( indexInFD == 0 ) {
			length = value;
		}
		
		int memLocInFD = locInBlock + (indexInFD % 4)*4;
		IO.write_blockInt(whichBlock, memLocInFD, pack(value));
	}

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

	/**
	 * This method reads the value located in the specified indexInFD
	 * 
	 * @param indexInFD
	 * @return index of block if applicable
	 */
	public int read(int indexInFD) {
		int scan = fdInfo[indexInFD];
		
		// test to see if it is correctly written in memory
		//int memLocInFD = locInBlock + (indexInFD % 4)*4;
		//int test = unpack(IO.read_blockInt(whichBlock, memLocInFD));
		//System.out.println("TESTING READ INT FROM MEMORY: " + test);
		//System.out.println(whichBlock + "  " + memLocInFD);
		
		return scan; // means its empty, also null value
	}
}
