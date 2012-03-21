package bitNom;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.content.Collection;
import org.ccnx.ccn.protocol.ContentName;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Download implements Runnable{
	public static final int chunkSize = 524288;
	public static final int maxActiveChunks = 10;
	
	private boolean _finished;
	public int _amountDone;
	private int _fileSize;
	private int _nChunks;
	private String _inName;
	private String _outName;
	private List<ChunkDownload> chunkLoaders;
	private ArrayBlockingQueue<ChunkDownload> _activeChunks;
	
	public List<String> _peers;
	
	private ArrayBlockingQueue<Boolean> _waitToken;
	private int _waiters;
	
	public float percentDone() { return _amountDone/_fileSize; }
	
	// The regular expression in which chunk numbers must appear in filename
	//	Under this implementation, a file chunk is named, for example, file.txt.001.
	public static final String chunkEncoding = ".*\\.[0-9]+\\..*";
	public static final String chunkNumberEncoding = "\\.[0-9]+\\.";
	
	/*	===========================================================================================
	 * 
	 * 	Static functions for handling chunk related things.
	 * 		Might want to move these to a different class.
	 * 
	 * 	===========================================================================================
	 */
	
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
	/*	===========================================================================================
	 * 
	 * 	The actual Download class methods
	 * 
	 * 	===========================================================================================
	 */
	
	Download(String inName, String outName, List<String> recentPeers, int nChunks) {
		_inName = inName;
		_outName = outName;
		_peers = recentPeers;
		_nChunks = nChunks;
		_activeChunks = new ArrayBlockingQueue<ChunkDownload>(maxActiveChunks);
		
		// Helping variables to allow a thread to blocking wait on this download.
		_waitToken = new ArrayBlockingQueue<Boolean>(1);
		_waitToken.add(true);
		_waiters = 0;
	}
	
	private void concatenateChunks(){
		try {
			RandomAccessFile output = new RandomAccessFile(_inName, "rwd");
			FileChannel outputChannel = output.getChannel();
			int i = 0;
			// Open every chunk file and transfer the data to the output file
			try {
				for (; i < _nChunks; i++) {
					FileInputStream curChunk = new FileInputStream(_inName + "." + i +".");
					FileChannel inputChannel = curChunk.getChannel();
					outputChannel.transferFrom(inputChannel, outputChannel.position(), chunkSize);
				}
				outputChannel.truncate(_fileSize);
			} catch (FileNotFoundException e) {
				Log.warning("Missing chunk {0} of file {1}.", i, _inName);
			} catch (IOException e) {
				Log.warning("Error concatenating file {0}", _inName);
			}
		
		} catch (FileNotFoundException e) {
			Log.warning("Could not open file {0} for Reading or writing.", _inName);
		}
	}
	
	// Wait on this download to finish. Implemented by trying to push something to the
	//	blocking queue. Since the pusher waits until the queue has space, we only empty
	//	the queue if the download is finished. Call this from a separate thread only.
	public void waitForMe(){
		_waiters++;
		try {
			_waitToken.put(true);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void cleanUp() {
		// "Notify" the waiters that we're done.
		try {
			
			while (_waiters > 0 && _waitToken.take()){
				_waiters--;
			}
			_finished = true;
			
		} catch (InterruptedException e) {
			Log.severe("Download waiting indefinitely.");
		}
	}
	
	public void printStatus(){}
	
	// Attempt to start a ChunkDownloader. If the number of active downloaders is met,
	//	the ChunkDownloader will block.
	public synchronized void addToActive(ChunkDownload c) throws InterruptedException {
		_activeChunks.put(c);
	}
	
	public void run() {
		// Create a chunk downloader for every chunk
		chunkLoaders = Collections.synchronizedList(new ArrayList<ChunkDownload>());
		
		for (int i = 0; i < _nChunks; i++) {
			// Number of segments in the current chunk
			int nSegments = (int) ((i == _nChunks - 1) ? (_fileSize % chunkSize / Globals.segSize) : chunkSize / Globals.segSize);
			ChunkDownload temp = new ChunkDownload(_inName, _outName, _peers, nSegments, this);
			chunkLoaders.add(temp);
			(new Thread(temp)).start();
		}
		
		// Wait for all of them to finish.
		for (int i = 0; i < _nChunks; i++) {
			chunkLoaders.get(i).waitForMe();
		}
		
		// Piece all the chunks together
		concatenateChunks();
		
		// Clean up, wake up waiting threads, etc.
		cleanUp();
	}
}
