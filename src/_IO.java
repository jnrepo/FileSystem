/*
 * IO System
 * 
 * First creates disk that has 64 blocks, each that are composed of IO_block (64 byte array)
 * 
 * has public methods read_block and write_block
 */
public class _IO {

	/*
	 * the disk we will write to and read from
	 * 
	 * disk[0] = bitmap disk[1] - disk[7] = blocks for descriptors disk[8] -
	 * disk[10] = directory entries disk[10] - disk[63] = data blocks
	 */
	public IO_block[] disk = new IO_block[64];

	public _IO() {
		// initialize each of the blocks
		for (int i = 0; i < disk.length; i++) {
			disk[i] = new IO_block();
		}
	}

	/**
	 * Uses parameter blockIndex to find the block to read next. Loads the read
	 * data into data. Needs to convert byte to char[]
	 * 
	 * @param blockIndex
	 *            = index of the block to write to
	 * @param data
	 *            = data to write into the block
	 */
	public void read_block(int blockIndex, char[] data) {
		System.out.println("(Read_block) Called");
		System.out.println("(Read_block) blockIndex: " + blockIndex + " -------------------------------" );

		// scans the block and places into parameter that was passed as data
		for (int i = 0; i < data.length; i++) {
			data[i] = (char) (disk[blockIndex].data[i]);
		}
		
		System.out.println("(Read_block) read buffer: " + String.valueOf(data).toString());
	}

	/**
	 * Same as read_block, but accepts a byte array instead. Used to input
	 * integers.
	 * 
	 * @param blockIndex
	 * @param data
	 */
	public byte[] read_blockInt(int whichBlock, int locInBlock) {
		byte[] scan = new byte[4];
		for (int i = 0; i < 4; i++) {
			scan[i] = disk[whichBlock].data[locInBlock + i];
		}

		return scan;
	}

	/**
	 * Data is written into the specified blockIndex. Needs to convert byte into
	 * char[]
	 * 
	 * @param blockIndex
	 *            = index of the block to write to
	 * @param data
	 *            = data to write into the block
	 */
	public void write_block(int blockIndex, char[] data) {
		// out of bounds test, if the array of data is larger than 64B
		if (data.length > 64)
			System.out.println("WEIRD DATA IS BIGGER THAN 64 B. ERRROR");

		System.out.println("(Write_Block) Block Index: " + blockIndex);
		System.out.println("(Write_Block) Data: "
				+ String.valueOf(data).toString());

		for (int i = 0; i < data.length; i++) {

			disk[blockIndex].data[i] = (byte) data[i];
		}
		
		// Test
		char[] dataTest = new char[disk[blockIndex].data.length];
		for (int x = 0; x < disk[blockIndex].data.length; x++) {
			dataTest[x] = (char) disk[blockIndex].data[x];
		}
		
		System.out.println("(Write_Block) Disk: " + String.valueOf(dataTest).toString());
	}

	public void write_blockInt(int whichBlock, int locInBlock, byte[] data) {
		// writes in value in specified location in block
		for (int i = 0; i < data.length; i++) {
			disk[whichBlock].data[locInBlock + i] = data[i];
		}

	}
}
