import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.StringTokenizer;

/*
 * This is the file system class
 * 
 * the driver will use this to..
 * create
 * destroy
 * open
 * close
 * rea
 * write
 * lseek
 * 
 * and print a list of directories
 * or be able to restore/save the disk
 * 
 * Note to Self: Don't use Directory Entry object, use write and read from OFT table to parse.
 */

public class _FS {

	public _IO IO = new _IO();
	public FS_OFT[] OFT = new FS_OFT[4];
	public int fdBlockLength;
	public int fdSize;
	public int maxDirLength;
	public int bufferSize; // 64 bytes
	public int posInBuffer;
	public int fd_index;
	public int bmSize;
	public boolean restoreDisk;
	public int[] bm;
	public int[] mask = new int[32];
	public FS_FileDescriptor[] FD;

	// restore disk variables
	public int r_length;

	public String disk_name;
	public String filePath;

	public _FS(String filePath) {
		this.filePath = filePath;
		restoreDisk = false;
		r_length = 0;
	}

	/*
	 * setup FD[0] for directory initialize OFT[0] as directory file
	 */
	public boolean _init(int cyl, int sur, int sec, int secLength,
			char[] disk_name) {
		this.disk_name = String.valueOf(disk_name).toString();
		String dname = String.valueOf(disk_name).toString();
		File dFile = new File( dname);

		fdBlockLength = 0;
		fdSize = 0;
		maxDirLength = 0;
		bufferSize = secLength; // 64 bytes
		posInBuffer = 0;
		fd_index = 0;
		bmSize = 0;

		if (dFile.isFile() && dFile.canRead()) {
			try {
				Scanner scan = new Scanner(dFile);
				restoreDisk = true;

				// initialize mask
				int maskCount = mask.length - 1;
				for (int j = 0; j < mask.length; j++) {
					mask[j] = (int) Math.pow(2, maskCount);
					if (j == 0) {
						mask[j] = -1 * mask[j];
					}
					maskCount--;
				}

				bufferSize = secLength;
				// initialize each of the FD's
				// find FD length
				// FD_length = (3*secLength)/32
				fdBlockLength = (3 * secLength) / (2 * (secLength / 4));
				// System.out.println("FD Block Size: " + fdBlockLength);

				fdSize = fdBlockLength * 4;
				// System.out.println("FD Size: " + fdSize + " Descriptor(s).");

				FD = new FS_FileDescriptor[fdSize];

				maxDirLength = (fdSize * 8);

				for (int i = 0; i < FD.length; i++) {
					FD[i] = new FS_FileDescriptor(IO, i);
				}

				// initialize OFT's
				for (int x = 0; x < OFT.length; x++) {
					OFT[x] = new FS_OFT();
				}

				// initialize the BitMap
				// find BM size
				bmSize = (cyl * sur * sec) / 32;
				// System.out.println("BM Size: " + bmSize + " Integer(s).");
				bm = new int[bmSize];
				for (int y = 0; y < bm.length; y++) {
					if (y == 0)
						bm[y] = -33554432; // java is unsigned 32-bit
											// int, this int
											// represents first 7 bits as 1
											// those are used for bitmap, fd,
											// and
											// dir
					else
						bm[y] = 0;
				}

				FD[0].write(0, 0);
				// FD[0].write(1, 8);
				// FD[0].write(2, 9);
				// FD[0].write(3, 10);

				// set up OFT[0] for directory
				// load first block buffer into OFT buffer
				OFT[0].index = 0; // index of file descriptor
				OFT[0].length = FD[0].read(0); // grabs length of the file from
												// dir

				lseek(0, 0);
				System.out.println("(Test) seek finished");

				while (scan.hasNextLine()) {
					String scanLine = scan.nextLine();
					StringTokenizer parseLine = new StringTokenizer(scanLine);

					while (parseLine.hasMoreTokens()) {
						String scanToken = parseLine.nextToken();

						// if bitmap
						// sets up bitmap
						if (scanToken.equals("BM")) {
							int bm_raydex = Integer.parseInt(parseLine
									.nextToken());
							int bm_value = Integer.parseInt(parseLine
									.nextToken());

							bm[bm_raydex] = bm_value;
						}

						// if DIR
						else if (scanToken.equals("DIR")) {
							char[] dir_name = parseLine.nextToken()
									.toCharArray();
							System.out.println(dir_name);
							int dir_fd_index = Integer.parseInt(parseLine
									.nextToken());
							System.out.println(dir_fd_index);

							// update length of dir in oft
							// automatically updated in create;

							// update the length in dir's file descirptor
							FD[0].fdInfo[0] = dir_fd_index * 8;

							createDirEntry(dir_name, dir_fd_index);

						}
						// if FD
						else if (scanToken.equals("FD")) {

							int fd_r_index = Integer.parseInt(parseLine
									.nextToken());
							int fd_r_length = Integer.parseInt(parseLine
									.nextToken());
							int fd_r_block1 = Integer.parseInt(parseLine
									.nextToken());
							int fd_r_block2 = Integer.parseInt(parseLine
									.nextToken());
							int fd_r_block3 = Integer.parseInt(parseLine
									.nextToken());

							FD[fd_r_index].fdInfo[0] = fd_r_length;
							FD[fd_r_index].fdInfo[1] = fd_r_block1;
							FD[fd_r_index].fdInfo[2] = fd_r_block2;
							FD[fd_r_index].fdInfo[3] = fd_r_block3;

						}
						// if DAT
						else if (scanToken.equals("DAT")) {
							int io_index = Integer.parseInt(parseLine
									.nextToken());
							char[] io_data = parseLine.nextToken()
									.toCharArray();

							IO.write_block(io_index, io_data);
						}
					}
				}
			} catch (IOException ex) {
				System.out
						.println("ERROR: (init) FileInputStream could not find file.");
				ex.printStackTrace();
			}

			restoreDisk = false;
			return true;
		} else if (!dFile.isFile()) {
			// System.out.println("LOG: No Existing disk file found.");

			// initialize mask
			int maskCount = mask.length - 1;
			for (int j = 0; j < mask.length; j++) {
				mask[j] = (int) Math.pow(2, maskCount);
				if (j == 0) {
					mask[j] = -1 * mask[j];
				}
				maskCount--;
			}

			// initialize each of the FD's
			// find FD length
			// FD_length = (3*secLength)/32
			fdBlockLength = (3 * secLength) / (2 * (secLength / 4));
			// System.out.println("FD Block Size: " + fdBlockLength);

			fdSize = fdBlockLength * 4;
			// System.out.println("FD Size: " + fdSize + " Descriptor(s).");

			FD = new FS_FileDescriptor[fdSize];

			maxDirLength = (fdSize * 8);

			for (int i = 0; i < FD.length; i++) {
				FD[i] = new FS_FileDescriptor(IO, i);
			}

			// initialize OFT's
			for (int x = 0; x < OFT.length; x++) {
				OFT[x] = new FS_OFT();
			}

			// initialize the BitMap
			// find BM size
			bmSize = (cyl * sur * sec) / 32;
			// System.out.println("BM Size: " + bmSize + " Integer(s).");
			bm = new int[bmSize];
			for (int y = 0; y < bm.length; y++) {
				if (y == 0)
					bm[y] = -33554432; // java is unsigned 32-bit
										// int, this int
										// represents first 7 bits as 1
										// those are used for bitmap, fd, and
										// dir
				else
					bm[y] = 0;
			}

			// setup directory file descriptor entry on initialization
			// directory
			// length = 192
			// 1st block index = 8
			// 2nd block index = 9
			// 3rd block index = 10
			FD[0].write(0, 0);
			// FD[0].write(1, 8);
			// FD[0].write(2, 9);
			// FD[0].write(3, 10);

			// set up OFT[0] for directory
			// load first block buffer into OFT buffer
			OFT[0].index = 0; // index of file descriptor
			OFT[0].length = FD[0].read(0); // grabs length of the file from dir
											// FD
			// fd_index = FD[0].read(1);
			// IO.read_block(fd_index, OFT[0].buffer); // loading first block
			// into
			// OFT
			// buffer

		}
		return false;
	}

	public boolean createDirEntry(char[] file_name, int file_fd_index) {
		System.out.println("(TesT) create dir entry started");

		write(0, file_name, 4);

		System.out.println("(Test) entered file_name: "
				+ String.valueOf(file_name).toString());
		System.out.println("(Test) updated currentPos: " + OFT[0].currentPos);
		byte[] bytesIndex = pack(file_fd_index);
		char[] charIndex = new char[bytesIndex.length];
		for (int p = 0; p < bytesIndex.length; p++) {
			charIndex[p] = (char) bytesIndex[p];
		}

		write(0, charIndex, 4);
		System.out.println("(Test) entered file's fd index: "
				+ unpack2(charIndex));
		System.out.println("(Test) updated currentPos: " + OFT[0].currentPos);

		return false;
	}

	/**
	 * scans all the bits in the bm until a free block is found. returns the
	 * index location of the free block. if there are not free blocks returns
	 * -1.
	 * 
	 * @return the index if free block available, else -1
	 */
	public int findNewBlock() {

		System.out.println("(BM) find new block started");
		for (int x = 0; x < bm.length; x++) {
			System.out.println("(BM) mask length: " + (mask.length - 1));
			for (int y = 0; y < mask.length; y++) {
				int test = (bm[x] & mask[y]);

				// System.out.println("(BM) finding new block, bm[" + x + "]: "
				// + bm[x]);
				// System.out.println("(BM) finding new block, mask[" + y +
				// "]: " + mask[y]);

				if (test == 0) {
					System.out.println("(BM) found new block, bm[" + x + "]: "
							+ bm[x]);
					System.out.println("(BM) found new block, mask[" + y
							+ "]: " + mask[y]);
					// calculate index from x and y
					int index = (x % 2) * 32 + y;

					// update BM, where index is update
					bm[x] = bm[x] | mask[y];

					return index;
				}
			}
		}

		return -1; // return -1 if there are no blocks available
	}

	/**
	 * sets the bufferReadPos of the OFT based on oft_index to newPos. First
	 * checks if block of newPos exists, then checks if currentPos and newPos
	 * are on the same block. when they aren't we find the block newPos is on
	 * and write current block to disk and read the block with newPos. then we
	 * set the currentPos to newPos and return true. We return false if block
	 * doesn't exist that has newPos
	 * 
	 * @param oft_index
	 * @param newPos
	 * @return
	 */
	public boolean lseek(int oft_index, int newPos) {
		// find the block number of newPos and currentPos
		int fd_indexNew = newPos / bufferSize + 1;
		int fd_indexBuf = OFT[oft_index].currentPos / bufferSize + 1;

		// we have read to end of the file, so reset fd_indexBuf to 3
		if (fd_indexBuf == 4) {
			fd_indexBuf = 3;
		}

		// System.out.println("(Seek) OFT currentPos: " +
		// OFT[oft_index].currentPos);
		System.out.println("(Seek) FD INDEX: " + fd_indexBuf);
		System.out.println("(Seek) new FD INDEX: " + fd_indexNew);
		// check if block numbers are the same
		// if they aren't we write current block to disk
		// then load appropriate block into memory
		if (fd_indexNew != fd_indexBuf) {
			// System.out.println("Seeking1");
			if (FD[OFT[oft_index].index].fdInfo[fd_indexBuf] != -1) {
				IO.write_block(FD[OFT[oft_index].index].fdInfo[fd_indexBuf],
						OFT[oft_index].buffer);
				// System.out.println("Seeking2");
				
			}
			
			IO.read_block(FD[OFT[oft_index].index].fdInfo[fd_indexNew],
					OFT[oft_index].buffer);
			// System.out.println("Seeking3");
		}

		OFT[oft_index].currentPos = newPos;

		return true;
	}

	/**
	 * creates a new file. first checks to see that there is a free fd second
	 * checks to see name isn't taken already third checks to see that there is
	 * a free directory entry if all checks pass, then we create a new file set
	 * new fd length set directoryEntry name and index of file descriptor
	 * 
	 * @param filename
	 * @return
	 */
	public boolean create(char[] filename) {
		int newfd_index = 1; // 0 is reserved for directory
		char[] memory = new char[4];

		boolean isFreeFD = false;
		boolean isFreeEntry = false;
		boolean uniqueName = true;

		// first look for free file descriptor
		while (!isFreeFD) {
			if (newfd_index > FD.length) {
				return false;
			}
			if (FD[newfd_index].fdInfo[0] == -1) {
				// we have found the correct fd_index that is free
				System.out.println("FOUND FREE FD");
				isFreeFD = true;
			} else
				newfd_index++;
		}

		// second check to see there are no matching names in dir entries
		lseek(0, 0); // this resets the currentPos scanning directory file
						// back
						// to 0
		while (OFT[0].currentPos < OFT[0].length) {
			read(0, memory, 4);

			// if same name exists, we exit loop and return error saying
			// name
			// already exists
			if (String.valueOf(memory).toString().trim()
					.equals(String.valueOf(filename).toString())) {
				System.out.println("NOT UNIQUE NAME CANNOT CREATE");
				uniqueName = false;
				return false;
			}
		}

		// third look for free entry
		lseek(0, 0); // reset back to 0 and read from beginning of dir until
						// empty dir is found
		// if there is nothing written in dir yet, find the newest block and
		// set
		// the index
		System.out.println("(Create) OFT's length of dir = " + OFT[0].length);
		if (OFT[0].length == 0) {
			isFreeEntry = true;
			System.out.println("WE HAVE FOUND FREE DIR ENTRY");
		} else {
			while (OFT[0].currentPos < maxDirLength) {
				read(0, memory, 4);
				System.out.println("(Create) file name read from memory: "
						+ String.valueOf(memory).toString());
				if (String.valueOf(memory).toString().trim().equals("")) {
					System.out
							.println("(Create) current position after finding free dir entry: "
									+ OFT[0].currentPos);
					lseek(0, OFT[0].currentPos - 4); // set it back 4 bytes, so
														// we
														// can write name in
														// correct
														// place
					isFreeEntry = true;
					System.out.println("WE HAVE FOUND FREE DIR ENTRY");
					break;
				}

				// read int to increase currentPos, so we don't read int
				OFT[0].currentPos += 4;
			}
		}

		// if all conditions are true
		if (isFreeFD && uniqueName && isFreeEntry) {
			// set file descriptor, sets length to 0
			FD[newfd_index].fdInfo[0] = 0;

			// set directory entry
			// first convert newfd_index to char[]
			// if filename length != 4 set it to 4
			byte[] bytesIndex = pack(newfd_index);
			char[] charIndex = new char[bytesIndex.length];
			for (int p = 0; p < bytesIndex.length; p++) {
				charIndex[p] = (char) bytesIndex[p];
			}

			// test
			for (int u = 0; u < charIndex.length; u++) {
				bytesIndex[u] = (byte) charIndex[u];
			}
			// System.out.println("Double checking casting to char array and back: "
			// + unpack(bytesIndex));

			char[] correctFileName = new char[4];
			for (int z = 0; z < filename.length; z++) {
				correctFileName[z] = filename[z];
			}

			// System.out.println("corrected file name: " +
			// String.valueOf(correctFileName).toString());
			write(0, correctFileName, 4);
			System.out.println("currentPos of the dir buffer: "
					+ OFT[0].currentPos);
			// System.out.println("should be (a), first char of buffer: " +
			// String.valueOf(OFT[0].buffer).toString());
			write(0, charIndex, 4);

			// System.out.println("FILE WRITTEN SUCCESSFULLY!");
			// System.out.println("(Create) length of new directory fd: " +
			// OFT[0].length);
			return true;

		} else {
			System.out.println("ERROR WHILE CREATE");
			return false;
		}

	}

	public boolean destroy(char[] filename) {

		char[] memory = new char[4];
		int indexToDestroy = -1;
		boolean foundFD = false;

		// find index of file descriptor of file_name and remove directory entry
		lseek(0, 0);
		while (OFT[0].currentPos < OFT[0].length) {
			read(0, memory, 4);
			if (String.valueOf(memory).toString().trim()
					.equals(String.valueOf(filename).toString())) {
				System.out.println("DESTROY: FOUND DIRECTORY ENTRY.");
				read(0, memory, 4); // memory now contains file descriptor
									// index, change to byte using unpack2
				indexToDestroy = unpack2(memory);

				// destroy directory entry
				memory = new char[4]; // setting memory to a null value
				lseek(0, OFT[0].currentPos - 8);
				write(0, memory, 4);
				write(0, memory, 4);
				foundFD = true;
				break;
			}

		}

		if (!foundFD) {
			return false;
		} else {
			// update bit map if something has been written
			if (FD[indexToDestroy].fdInfo[0] != 0) {

				int writtenBlocks = FD[indexToDestroy].fdInfo[0] / 64 + 1;
				int blockScan = 1; // first block is at index 1
				while (blockScan <= writtenBlocks) {
					int bm_index = FD[indexToDestroy].fdInfo[blockScan] / 64; // find
																				// out
																				// which
																				// block
																				// its
																				// at
					int bm_blockLoc = FD[indexToDestroy].fdInfo[blockScan] % 64;

					System.out.println(writtenBlocks);
					System.out.println(bm_index);
					System.out.println(bm_blockLoc);
					bm[bm_index] = bm[bm_index] & ~mask[bm_blockLoc];
				}

			}

			/*
			 * Free file descriptor
			 * 
			 * Change length to -1 Change block indexes to -1
			 */
			FD[indexToDestroy].fdInfo[0] = -1;
			FD[indexToDestroy].fdInfo[1] = -1;
			FD[indexToDestroy].fdInfo[2] = -1;
			FD[indexToDestroy].fdInfo[3] = -1;
			return true;
		}
	}

	/**
	 * read method. based on index in the open file table reads from buffer and
	 * prints into memory until count is reached or end of file is reached.
	 * 
	 * @param oft_index
	 * @param memory
	 * @param count
	 */
	public boolean read(int oft_index, char[] memory, int count) {
		int startCount = 0;
		boolean done = false;

		// compute the position inside the buffer
		if (OFT[oft_index].currentPos != 64) {
			posInBuffer = OFT[oft_index].currentPos % bufferSize;
		} else {
			posInBuffer = 64;
		}
		System.out.println("(Reading) position in buffer: " + posInBuffer);

		while (!done) {

			// check if the specified count has been reached or
			// end of file has been reached
			if (startCount == count
					|| OFT[oft_index].currentPos == maxDirLength) {
				// Test
				// if (startCount == count)
				// System.out.println("reading - count has been reached");
				// if (OFT[oft_index].currentPos == OFT[oft_index].length) {
				// System.out.println("reading - End of File Reached");
				// }
				done = true;
				break;
			}

			// end of buffer reached
			if (posInBuffer == 64) {

				// find the next block
				fd_index = (OFT[oft_index].currentPos - 1) / bufferSize + 1;

				// reset position in buffer back to 0
				posInBuffer = 0;

				System.out.println("(Reading) current position: "
						+ OFT[oft_index].currentPos);
				System.out.println("(Reading) FD Index: " + fd_index);

				// write the block, then read the next and load data into buffer
				System.out.println("(Reading) Block Index Stored in FD: "
						+ FD[OFT[oft_index].index].fdInfo[fd_index]);
				IO.write_block(FD[OFT[oft_index].index].fdInfo[fd_index++],
						OFT[oft_index].buffer);
				System.out
						.println("(Reading) Ended Write_Block, starting Read_block");
				System.out.println("(Reading) FD Index: " + fd_index);
				System.out.println("(Reading) Block Index Stored in FD: "
						+ FD[OFT[oft_index].index].fdInfo[fd_index]);
				if (FD[OFT[oft_index].index].fdInfo[fd_index] != -1) {
					IO.read_block(FD[OFT[oft_index].index].fdInfo[fd_index],
							OFT[oft_index].buffer);
				} else {
					fd_index--;
					done = true;
					break;
				}

			}

			memory[startCount] = (OFT[oft_index].buffer)[posInBuffer];

			posInBuffer++;
			OFT[oft_index].currentPos++;
			startCount++;
		}
		return true;

	}

	/**
	 * write function. based on index of open file it prints memory into disk
	 * until count is reached, or there is no more space to write too.
	 * 
	 * @param oft_index
	 * @param memory
	 * @param count
	 */
	public int write(int oft_index, char[] memory, int count) {
		int startPos = 0;
		boolean done = false;
		int oldLength = OFT[oft_index].length;
		int freeDisk_index = -1;

		// find position within the buffer
		posInBuffer = OFT[oft_index].currentPos % bufferSize;
		// System.out.println("WRITE- Position in Buffer: " + posInBuffer);

		// if we don't have a block allocated into FD, search for a block
		// if we cannot find we return -1 showing that we had an error
		fd_index = OFT[oft_index].length / bufferSize + 1;
		System.out.println("(Write) FD index: " + fd_index);
		if (fd_index == 4) {
			fd_index = 3;
			System.out.println("(Write) FD index was 4, so changed to 3");
		}
		System.out.println("(Write) index stored in FD of Block: "
				+ FD[OFT[oft_index].index].fdInfo[fd_index]);

		// conditional for if in the FD the block index is -1 (EMPTY)
		if (FD[OFT[oft_index].index].fdInfo[fd_index] == -1) {
			// find a new block
			freeDisk_index = findNewBlock();
			if (freeDisk_index == -1) {
				System.out.println("(Write) NO AVAILABLE FREE BLOCKS ON DISK");
			}
			System.out.println("(Write) found free disk index: "
					+ freeDisk_index);
			FD[OFT[oft_index].index].fdInfo[fd_index] = freeDisk_index;

		}

		while (!done) {

			// check to see if reached count
			if (startPos == count) {
				// System.out.println("writing - count has been reached");
				done = true;
				break;
			}

			if (posInBuffer == 64) {
				System.out.println("(Write) position in buffer has reached: "
						+ posInBuffer);

				// reset position in buffer
				posInBuffer = 0;

				// find the fd_index of current block we are reading
				System.out.println("(Write) OFT[oft_index].length: "
						+ OFT[oft_index].length);
				fd_index = (OFT[oft_index].length - 1) / bufferSize + 1;

				// save the buffer onto current block index
				// write buffer into disk
				System.out.println("(Write) saving current buffer to: "
						+ FD[OFT[oft_index].index].fdInfo[fd_index]);
				IO.write_block(FD[OFT[oft_index].index].fdInfo[fd_index],
						OFT[oft_index].buffer);

				// reset the buffer
				OFT[oft_index].buffer = new char[bufferSize];
				for (int b = 0; b < OFT[oft_index].buffer.length; b++) {
					OFT[oft_index].buffer[b] = ' ';
				}

				// now we check to see if there is another block we can write
				// too
				// first check our current FD index to make sure it is less than
				// 4
				// if we pass the check fd_index + 1
				// second if first check passes, then we look for a free block
				// and set that block index to the new FD index
				if (fd_index < 3) {
					fd_index++;
					System.out
							.println("(Write) FD disk map is not filled, next index: "
									+ fd_index);

					// check that the new disk map index for fd is empty
					if (FD[OFT[oft_index].index].fdInfo[fd_index] == -1) {
						freeDisk_index = findNewBlock();
						if (freeDisk_index == -1) {
							System.out
									.println("NO AVAILABLE FREE BLOCKS ON DISK");
							break;
						}
						FD[OFT[oft_index].index].fdInfo[fd_index] = freeDisk_index;
					} else {
						System.out
								.println("ERROR: (Write) next index is not empty!");
					}
				} else {
					System.out.println("(Write) File has written MAX");
				}
				/*
				 * else { freeDisk_index =
				 * FD[OFT[oft_index].index].fdInfo[fd_index]; }
				 */

			}

			// write from memory into buffer
			OFT[oft_index].addBuf(posInBuffer, memory[startPos]);
			// System.out.println("inputting this char into buffer: " +
			// String.valueOf(memory).charAt(startPos));
			// System.out.println("after add to buffer, buffer has: " +
			// String.valueOf(OFT[oft_index].buffer).toString());

			startPos++;
			posInBuffer++;
			OFT[oft_index].length++;
			OFT[oft_index].currentPos++;
		}

		int difference = OFT[oft_index].length - oldLength;
		// update the FD's length
		int newLength = FD[OFT[oft_index].index].length + difference;
		FD[OFT[oft_index].index].write(0, newLength);

		return difference;
	}

	public boolean directory(PrintStream ps) {
		lseek(0, 0);
		while (OFT[0].currentPos < maxDirLength) {
			char[] memory = new char[4];
			read(0, memory, 4);
			System.out.println("(Dir) Dir Name: "
					+ String.valueOf(memory).toString());
			if (!String.valueOf(memory).toString().trim().equals("")) {
				String dir_name = String.valueOf(memory).toString();

				read(0, memory, 4);
				int dir_fd_index = unpack2(memory);

				int file_length = FD[dir_fd_index].fdInfo[0];
				// System.out.println("(Save) FD Index: " + unpack2(memory));

				ps.print(dir_name + " " + file_length + ", ");
				System.out.print("\t" + dir_name + " " + file_length + ", ");
			} else {
				OFT[0].currentPos += 4;
			}

		}
		ps.println();
		return false;
	}

	public int open(char[] filename) {
		int fd_indexToFind = -1; // if -1 that means we the file does not exist.
		char[] memory = new char[4];
		boolean found = false;

		lseek(0, 0);
		while (OFT[0].currentPos < OFT[0].length) {
			System.out.println("(Open) current length of directories in OFT: "
					+ (OFT[0].length / 8) + " files");
			read(0, memory, 4);
			if (String.valueOf(memory).toString().trim()
					.equals(String.valueOf(filename).toString())) {
				System.out.println("(Open) Name read in file: "
						+ String.valueOf(memory).toString());
				System.out.println("Current Position: " + OFT[0].currentPos);
				System.out.println("OPEN: FOUND DIRECTORY ENTRY.");
				read(0, memory, 4); // memory now contains file descriptor
									// index, change to byte using unpack2
				fd_indexToFind = unpack2(memory);
				System.out.println("(Open) fd_indexToFind: " + fd_indexToFind);
				found = true;
				break;
			}

		}

		if (!found) {
			System.out.println("OPEN: DIRECTORY ENTRY NOT FOUND");
			return -1;
		}

		int oft_index = 1;

		while (oft_index < OFT.length) {
			if (OFT[oft_index].length == -1) {
				if (FD[fd_indexToFind].fdInfo[1] != -1) {
					IO.read_block(FD[fd_indexToFind].fdInfo[1],
							OFT[oft_index].buffer);
				}
				OFT[oft_index].currentPos = 0;
				OFT[oft_index].index = fd_indexToFind;
				OFT[oft_index].length = FD[fd_indexToFind].fdInfo[0];
				break;
			}
			oft_index++;
		}

		if (oft_index == 4) {
			System.out.println("OPEN: NO OFT ENTRY AVAILABLE.");
			return -1;
		}

		return fd_indexToFind; // -1 means we could not find the file
	}

	public boolean close(int oft_indexToClose) {

		// checking out of bounds condition on oft
		// only 4 files can be open at once
		if (oft_indexToClose > 3) {
			return false;
		}

		if (OFT[oft_indexToClose].length >= 0) {
			// we need +1 since 0 is length field
			int fd_blockIndex = OFT[oft_indexToClose].currentPos / bufferSize
					+ 1;
			if (fd_blockIndex == 4) {
				fd_blockIndex = 3;
			}

			if (OFT[oft_indexToClose].length != 0) {
				// this means that the file was created, but no blocks were
				// written to, therefore it would error if we tried to write to
				// block

				IO.write_block(
						FD[OFT[oft_indexToClose].index].fdInfo[fd_blockIndex],
						OFT[oft_indexToClose].buffer);

			}
			// update file length
			FD[OFT[oft_indexToClose].index].fdInfo[0] = OFT[oft_indexToClose].length;

			// free the OFT entry
			// -1 means its empty
			// 0 means there has nothing been written into the file
			OFT[oft_indexToClose].length = -1;
			OFT[oft_indexToClose].index = 0;
			OFT[oft_indexToClose].currentPos = 0;

			return true;
		}

		return false;
	}

	public boolean save(String saveName) {
		File f = new File(saveName);
		try {
			PrintStream out = new PrintStream(f);

			// save bit map
			for (int b = 0; b < bm.length; b++) {

				// saved in format BM <index> <value at index>
				out.println("BM " + b + " " + bm[b]);
			}

			for (int g = 0; g < FD.length; g++) {
				// this string array contains all the data that is written in
				// the blocks
				String[] blocksToSave = new String[3];

				// search for all FD that don't have length -1
				if (FD[g].fdInfo[0] != -1) {
					out.print("FD " + FD[g].index);
					out.print(" " + FD[g].fdInfo[0]);
					for (int p = 1; p < FD[g].fdInfo.length; p++) {
						System.out.println("(Save) FD's info : "
								+ FD[g].fdInfo[p]);
						out.print(" " + FD[g].fdInfo[p]);
						if (FD[g].fdInfo[p] != -1) {
							char[] entire_block = new char[bufferSize];
							IO.read_block(FD[g].read(p), entire_block);
							blocksToSave[p - 1] = "DAT "
									+ String.valueOf(FD[g].read(p)) + " "
									+ String.valueOf(entire_block).toString();
						} else {
							blocksToSave[p - 1] = null;
						}
					}

					out.println();
				}

				for (int bs = 0; bs < blocksToSave.length; bs++) {
					if (blocksToSave[bs] != null) {
						System.out.println(blocksToSave[bs]);
						out.println(blocksToSave[bs]);
					}
				}
			}

			// save directories
			lseek(0, 0);
			while (OFT[0].currentPos < OFT[0].length) {
				char[] memory = new char[4];
				read(0, memory, 4);
				String dir_name = String.valueOf(memory).toString();
				// System.out.println("(Save) Dir Name: "+
				// String.valueOf(memory).toString());
				read(0, memory, 4);
				int dir_fd_index = unpack2(memory);
				// System.out.println("(Save) FD Index: " + unpack2(memory));

				out.println("DIR " + dir_name + " " + dir_fd_index);
				System.out.println("DIR " + dir_name + " " + dir_fd_index);

			}

			// close all open files and write all buffers to disk
			for (int o = 0; o < OFT.length; o++) {
				if (OFT[o].length != -1) {
					close(o);
				}
			}

			// save dir
			// use bit map to find all blocks written and save

		} catch (FileNotFoundException e) {
			System.out
					.println("ERROR: (Save) couldn't find the output file to save");
			e.printStackTrace();
			return false;
		}

		return true;
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

	int unpack2(char[] convertToInt) {
		final int MASK = 0xff;
		int v = (int) convertToInt[0] & MASK;
		for (int i = 1; i < 4; i++) {
			v = v << 8;
			v = v | ((int) convertToInt[i] & MASK);
		}
		return v;
	}

}
