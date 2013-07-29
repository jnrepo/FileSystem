
public class FS_OFT {
	
	public int currentPos;
	public int length;
	public char[] buffer;
	public int index;
	public int currentIndex;
	
	public FS_OFT() {
		currentPos = 0;
		length = -1;	// -1 is empty
		buffer = new char[64];
		index = 0;
		currentIndex = -1;	// store value of current index we are reading in OFT
	}
	
	public void addBuf(int pos, char c) {
		buffer[pos] = c;
	}
}
