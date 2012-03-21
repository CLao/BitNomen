package bitNom;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.*;
import java.nio.channels.FileChannel;

public class Download {
	public static final int chunkSize = 524288;
	
	// The regular expression in which chunk numbers must appear in filename
	//	Under this implementation, a file chunk is named, for example, file.txt.001.
	public static final String chunkEncoding = ".*\\.[0-9]+\\..*";
	public static final String chunkNumberEncoding = "\\.[0-9]+\\.";

	
	// Return true if the filename indicates we have a chunk of a file.
	public static final boolean isChunk(String s){
		return s.matches(chunkEncoding);
	}
	
	public static final boolean isChunk(ContentName n){
		return isChunk(n.toString());
	}
	
	// Extract the chunk number from the name of 
	public static final int getChunkNumber(String s){
		String copy = s;
		int retNum = -1;
		
		// Get the region that isn't the number
		String complement[] = copy.split(chunkNumberEncoding);
		
		// Remove that region from the string.
		int j = complement.length;
		for (int i = 0; i < j; i++) {
			copy = copy.replace(complement[i], "");
		}
		
		retNum = Integer.parseInt(copy);
		
		return retNum;
	}
	
	public static final int getChunkNumber(ContentName n){
		return getChunkNumber(n.toString());
	}
	
	public static final String getChunkName(String s){
		String name = s;
		return name.replaceFirst("\\.[0-9]+\\.", "");
	}
	
	public static final String getChunkName(ContentName n){
		return getChunkName(n.toString());
	}
	
	public static final void concatenateChunks(String filename, int totalChunks){
		try {
			RandomAccessFile output = new RandomAccessFile(filename, "rwd");
			FileChannel outputChannel = output.getChannel();
			
			// Open every chunk file and transfer the data to the output file
			for (int i = 0; i < totalChunks; i++) {
				try {
					FileInputStream curChunk = new FileInputStream(filename + "." + i +".");
					FileChannel inputChannel = curChunk.getChannel();
					outputChannel.transferFrom(inputChannel, outputChannel.position(), chunkSize);
					
				} catch (FileNotFoundException e) {
					Log.warning("Missing chunk {0} of file {1}.", i, filename);
				} catch (IOException e) {
					Log.warning("Error concatenating file {0}", filename);
				}
				
				
			}
			
		} catch (FileNotFoundException e) {
			Log.warning("Could not open file {0} for Reading or writing.", filename);
		}
	}
}
