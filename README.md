FileSystem
==========
This is my project source code for CS143 P_3. It also includes an input file that should be passed as an argument when running the code. The input file contains a series of command (listed below) that allow the user to utitlize the file system. The code is a direct import of my java project files from Eclipse, ADT v21.1. The original *.java files are contained within the "src" folder After the code has been run with the input file succesfully, it will create two output files called "disk.txt" and "disk0.txt" in the directory of original source code. The first output file contains the output of the commands that were run. The second output file contains the state of the disk when save has been called. The disk is saved using a bit map  If you wish to restart form a clean slate, delete both output files. When the code is run again, it will start from a new clean slate.

●      cd <name>
	o    create a new file with the name <name>
	o    Output: file <name> created
●      de <name>
	o    destroy the named file <name>
	o    Output: file <name> destroyed
 
●      op <name>
	o    open the named file <name> for reading and writing; display an index value
	o    Output: file <name> opened, index=<index>
 
●      cl <index>
	o    close the specified file <index>
	o    Output: file <index> closed
 
●      rd <index> <count>
	o    sequentially read a number of bytes <count> from the specified file <index> and display them on the terminal
	o    Output: <count> bytes read: <xx...x>
 
●      wr <index> <char> <count>
	o    sequentially write <count> number of <char>s into the specified file <index> at its current position
	o    Output: <count> bytes written
 
●      sk <index> <pos>
	o    seek: set the current position of the specified file <index> to <pos>
	o    Output: current position is <pos>
 
●      dr
	o    directory: list the names of all files and their lengths
	o    Output: file0 <len0>,..., fileN <lenN>
 
●      in <no_cyl> <no_surf> <no_sect> <sect_len> <disk_cont>
	o    create a disk using the given dimension parameters and initialize it using the file <disk_cont> (copy of disk)
	o    If file does not exist, create and open directory; output: disk initialized
	o    If file does exist, open directory; output: disk restored
 
●      sv <disk_cont>
	o    close all files and save the contents of the disk in the file <disk_cont>
	o    Output: disk saved
 
●      If any command fails, output: error
 

These commands were copied directly from my shell specification from my project guidelines.
Link: http://www.ics.uci.edu/~bic/courses/145B/145B/FS-Project/FS-shell.html
