import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.StringTokenizer;

public class _Driver {

	// static File input = new File("c:\\input.txt");
	// static File output = new File("c:\\jheo.txt");

	/**
	 * the main class. If we have an argument for a input file, then we set the
	 * Scanner to input from the user. else we set it so that input file
	 * 
	 * @param args
	 *            = the input file _Driver will parse
	 * @throws IOException
	 *             if file could not be found or path could not be found
	 */
	public static void main(String[] args) throws IOException {

		// _Driver term = new _Driver();
		_FS FS = null;

		Scanner inputFileScanner;

		if (args.length >= 1) {
			File input = new File(args[0]);
			String wholePath = input.getAbsolutePath();
			String filePath = wholePath.substring(0,
					wholePath.lastIndexOf(File.separator));

			String outPath = filePath + "disk.txt";
			outPath = "disk.txt";
			File output = new File(outPath);

			try {

				PrintStream outputFileStream = new PrintStream(output);
				inputFileScanner = new Scanner(input);

				// Based on input data_block, parse, run and output to a
				// data_block
				while (inputFileScanner.hasNextLine()) {
					String scanLine = inputFileScanner.nextLine();
					StringTokenizer parseLine = new StringTokenizer(scanLine);

					while (parseLine.hasMoreTokens()) {
						String scanToken = parseLine.nextToken();

						// if command is init
						if (scanToken.equals("in")) {
							FS = new _FS(filePath);

							// read the next input for drive size
							int cyl = Integer.valueOf(parseLine.nextToken());
							int sur = Integer.valueOf(parseLine.nextToken());
							int sec = Integer.valueOf(parseLine.nextToken());
							int secLength = Integer.valueOf(parseLine
									.nextToken());
							char[] disk_name = parseLine.nextToken()
									.toCharArray();

							if (FS._init(cyl, sur, sec, secLength, disk_name)) {
								outputFileStream.println("disk restored");
								System.out.println("\tdisk restored");
							} else {
								outputFileStream.println("disk initialized");
								System.out.println("\tdisk initialized");
							}

						}

						// if command is create
						else if (scanToken.equals("cr")) {
							String filename = parseLine.nextToken();
							char[] fname = filename.toCharArray();
							if (!FS.create(fname)) {

								outputFileStream.println("error");
								System.out.println("\terror");
							} else {
								outputFileStream.println("file "
										+ filename + " created");
								System.out.println("\tfile " + filename + " created");
							}
						}

						// if command is destroy
						else if (scanToken.equals("de")) {
							String filename = parseLine.nextToken();
							char[] fname = filename.toCharArray();
							if (FS.destroy(fname)) {
								outputFileStream.println("file "
										+ filename + " destroyed");
								System.out.println("\tfile "
										+ filename + " destroyed");

							} else {
								outputFileStream.println("error");
								System.out.println("\terror");
							}
						}

						// if command is open
						else if (scanToken.equals("op")) {

							String filename = parseLine.nextToken();
							char[] fname = filename.toCharArray();

							int openedIndex = FS.open(fname);
							if (openedIndex >= 0) {

								outputFileStream.println("file "
										+ filename + " opened, index="
										+ openedIndex);
								System.out.println("\tfile "
										+ filename + " opened, index="
										+ openedIndex);
							} else {
								outputFileStream.println("error");
								System.out.println("\terror");
							}
						}

						// if command is close
						else if (scanToken.equals("cl")) {
							int index = Integer.parseInt(parseLine.nextToken());
							if (FS.close(index)) {

								outputFileStream.println("file " + index
										+ " closed");
								System.out.println("\tfile " + index
										+ " closed");
							} else {
								outputFileStream.println("error");
								System.out.println("\terror");

							}
						}

						// if command is read
						else if (scanToken.equals("rd")) {
							System.out.println("(Reading) Begins");
							char[] mem = new char[192];
							for (int i = 0; i < 192; i++) {
								mem[i] = ' ';
							}
							int index = Integer.parseInt(parseLine.nextToken());
							int count = Integer.parseInt(parseLine.nextToken());
							if (FS.read(index, mem, count)) {

								outputFileStream.print(count + " bytes read: ");
								System.out.print(count + " bytes read: ");
								for (int i = 0; i < 192; i++) {

									if (mem[i] == ' ') {
										break;
									}
									outputFileStream.print(mem[i]);
									System.out.print(mem[i]);
								}

								outputFileStream.println();
								System.out.println();

							} else {
								outputFileStream.println("error");
								System.out.println("\terror");
							}

						}

						// if command is write
						else if (scanToken.equals("wr")) {
							// index of block in fd to write too
							int index = Integer.parseInt(parseLine.nextToken());

							// char to write into block
							char ch = parseLine.nextToken().charAt(0);

							// how many times to write char
							int count = Integer.parseInt(parseLine.nextToken());

							// needs to create a new char array to input into
							// the block from char "ch"
							// all other blocks will have an empty space
							char[] mem = new char[192];
							for (int i = 0; i < 192; i++) {
								mem[i] = ' ';
							}
							for (int i = 0; i < count; i++) {
								mem[i] = ch;
							}

							// if write was successful, print out how many bytes
							// were written
							// else print out error
							int byteWritten = FS.write(index, mem, count);
							if (byteWritten > 0) {

								outputFileStream.println(byteWritten
										+ " bytes written");
								System.out.println("\t"+byteWritten +
								 " bytes written");
							} else {
								outputFileStream.println("error");
								System.out.println("\terror");
							}

						}

						// if command is seek
						else if (scanToken.equals("sk")) {
							// System.out.println("Seeking");
							int index = Integer.parseInt(parseLine.nextToken());
							int pos = Integer.parseInt(parseLine.nextToken());
							// System.out.println("Seeking");
							if (FS.lseek(index, pos)) {
								outputFileStream.println("current position is "
										+ pos);
								System.out
										.println("\tcurrent position is " + pos);
							} else {
								outputFileStream.println("error");
								System.out.println("\terror");
							}
						}

						// if command is dr
						else if (scanToken.equals("dr")) {
							FS.directory(outputFileStream);
						}

						// if command is save
						else if (scanToken.equals("sv")) {
							if (FS.save(parseLine.nextToken())) {
								outputFileStream.println("disk saved");
								System.out.println("\tdisksaved");
							} else {
								outputFileStream.println("error");
								System.out.println("\terror");
							}
						} else {
							outputFileStream.println("error");
							System.out.println("\terror");
						}
					}
				}
			} catch (Exception e) {
				System.out.println("ERROR: could not find input file");
			}
		} else {
			/*
			 * This condition is true when no arguments are passed when running
			 * _Driver for input.txt
			 */

			System.out.println("ERROR: (Driver) No input.txt passed.");
		}
	}
}
