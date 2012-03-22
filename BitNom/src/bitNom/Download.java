package bitNom;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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
	
	public float percentDone() { return 100 * _amountDone/_fileSize; }
	public boolean finished() { return _finished; }
	
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
		
		copy = copy.substring(1, copy.length() - 1);
		
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
	
	public static final File getChunkFilename(File f){
		return new File(getChunkName(f.toString()));
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
		
		// Right now, this is just an estimate of the file size.
		_fileSize = nChunks * chunkSize;
		
		// Helping variables to allow a thread to blocking wait on this download.
		_waitToken = new ArrayBlockingQueue<Boolean>(1);
		_waitToken.add(true);
		_waiters = 0;
	}
	
	private void concatenateChunks(){
		try {
			RandomAccessFile output;
			output = new RandomAccessFile(Globals.ourHome + _outName, "rwd");
			FileChannel outputChannel = output.getChannel();
			int i = 0;
			
			// Open every chunk file and transfer the data to the output file
			//	For now, we leave the extra chunks on disk, but now is where
			//	we would delete them.
			try {
				for (; i < _nChunks; i++) {
					byte [] buffer = new byte[Download.chunkSize];
					ByteBuffer buf = ByteBuffer.wrap(buffer);
					FileInputStream curChunk = new FileInputStream(Globals.ourHome + _outName + "." + i +".");
					FileChannel inputChannel = curChunk.getChannel();
					
					int w, l;
					l = inputChannel.read(buf);
					buf.position(0);
					w = outputChannel.write(buf);
					outputChannel.truncate(_fileSize - (Download.chunkSize - l));
					if (Globals.dbDL) Log.info("Chunk: Read " + l + " bytes, wrote "+ w + " bytes.");
					
				}
	
			} catch (FileNotFoundException e) {
				Log.warning("Missing chunk {0} of file {1}.", i, e.getMessage());
			} catch (IOException e) {
				Log.warning("Error concatenating file {0}", _outName);
			}
		}catch (IOException e) {
			System.out.println("I don't get it" + e.getMessage());
			//Log.warning("Missing chunk {0} of file {1}.", i, _outName);
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
	
	public void printStatus(){
		Log.info("Download: {0}\n\t {1}% complete!", _outName, percentDone());
	}
	
	// Attempt to start a ChunkDownloader. If the number of active downloaders is met,
	//	the ChunkDownloader will block.
	public synchronized void addToActive(ChunkDownload c) throws InterruptedException {
		_activeChunks.put(c);
	}
	
	public void run() {
		// Create a chunk downloader for every chunk
		chunkLoaders = Collections.synchronizedList(new ArrayList<ChunkDownload>());
		
		for (int i = 0; i < _nChunks; i++) {
			String chunkExtension = "." + i + ".";
			
			// Number of segments in the current chunk
			//int nSegments = (int) ((i == _nChunks - 1) ? (_fileSize % chunkSize / Globals.segSize) : chunkSize / Globals.segSize);
			
			int nSegments = 1;
			ChunkDownload temp = new ChunkDownload(_inName + chunkExtension, _outName + chunkExtension, _peers, nSegments, this);
			chunkLoaders.add(temp);
			(new Thread(temp)).start();
		}

		
		// Wait for all of them to finish.
		for (int i = 0; i < _nChunks; i++) {
			chunkLoaders.get(i).waitForMe();
			_amountDone += chunkSize;
		}

		
		// Piece all the chunks together
		concatenateChunks();
		
		// Clean up, wake up waiting threads, etc.
		cleanUp();
	}
}
