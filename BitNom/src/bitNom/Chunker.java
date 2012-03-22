package bitNom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.ccnx.ccn.impl.support.Log;


public class Chunker {
	
	// Takes a filename, opens it, and creates the nth
	//	chunk of the file. For now, it's saved to disk
	//	in order to send it, to get around some sort
	//	of size limit ccn has in uploading files.
	public static void chunk(String file, int n) {
		try {
			RandomAccessFile input = new RandomAccessFile(file, "r");
			FileOutputStream output = new FileOutputStream(file + "." + n + ".");
		
		
			input.getChannel().transferTo(n * Download.chunkSize, Download.chunkSize, output.getChannel());
		} catch (IOException e) {
			Log.warning("Could not chunk file {0}.", file);
		}
	}
	
	public static void chunk(File file, int n) {
		try {
			RandomAccessFile input = new RandomAccessFile(file, "r");
			FileOutputStream output = new FileOutputStream(file + "." + n + ".");
		
		
			input.getChannel().transferTo(n * Download.chunkSize, Download.chunkSize, output.getChannel());
		} catch (IOException e) {
			Log.warning("Could not chunk file {0}.", file);
		}
	}
}
