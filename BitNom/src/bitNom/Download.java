package bitNom;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.channels.*;

import org.ccnx.ccn.impl.support.Log;


public class Download implements Runnable {
	
	// Only have at most maxThreads running at a time
	public static final int maxThreads = 20;
	
	private String path;
	private String outFile;
	private int nSeg;
	private int doneSegs;
	
	public List<String> peers;
	private List<SegDownloader> segDownloads;
	public ArrayBlockingQueue<SegDownloader> bstopped;
	public ArrayBlockingQueue<SegDownloader> active;
	
	private boolean segFin[];
	private boolean done;
	public Dstatus status;
	public float percentDone;
	public ArrayBlockingQueue<Boolean> waitToken;
	private int waiters;
	
	RandomAccessFile file;
	FileChannel channel;
	
	Download (String dpath, String output, List<String> recentPeers, int segments){
		path = dpath;
		nSeg = segments;
		peers = recentPeers;
		segFin = new boolean[nSeg];
		doneSegs = 0;
		status = Dstatus.DOWNLOADING;
		outFile = output;
		
		segDownloads = new ArrayList<SegDownloader>(segments);
		bstopped = new ArrayBlockingQueue<SegDownloader>(segments);
		active = new ArrayBlockingQueue<SegDownloader>(maxThreads);
		
		// Helping variables to allow a thread to blocking wait on this download.
		waitToken = new ArrayBlockingQueue<Boolean>(1);
		waitToken.add(true);
		waiters = 0;
		
		percentDone = 0;	
	}
	
	// Methods just for reading private members
	public int nSeg() { return nSeg; }
	public int doneSegs() { return doneSegs; }
	public String outFile() { return outFile; }
	public boolean done() { return done; }
	
	public void run() {
		//Create a file channel for the file
		try {
			file = new RandomAccessFile (Globals.ourHome + outFile, "rwd");
			channel = file.getChannel();
			
			
			// Create a segment downloader for each segment and run them simultaneously
			for (int i = 0; i < nSeg; i++) {
				segDownloads.add(new SegDownloader(this, "ccnx:" + peers.get(i % peers.size()) + path, i));
				(new Thread(segDownloads.get(segDownloads.size() - 1))).start();
			}
			
			// Wait for a download thread to either finish or fail, and update our bookkeeping			
			try {
				while (!done)
				{
					// Get a new segment downloader off the stopped list.
					SegDownloader s = bstopped.take();
					
					// Give a new path to every download thread that failed.
					if (s.status() == Dstatus.FAILED)
					{
						synchronized(s) {
							s.dlPath = getNewPath();
							removeStopped(s);
							s.notify(); 
						}
					}
					
					// Check if that segment finishes off the download.
					else if (s.status() == Dstatus.FINISHED)
					{
						if (nSeg == doneSegs){
							done = true;
							percentDone = 100;
							System.out.println("Download " + Globals.ourHome + outFile + " Finished!");
							
							// "Notify" the waiters that we're done.
							while (waiters > 0 && waitToken.take()){
								waiters--;
							}
						}
					}
					else throw new RuntimeException("Impossible");
					
				}
			} catch (InterruptedException e){
				status = Dstatus.FAILED;
			}
			
		} catch (IOException e) {
			Log.info("Could not open file for download!");
			status = Dstatus.FAILED;
		}
	}
	
	// Give a new download path to a downloader.
	public synchronized String getNewPath(){
		String peer = peers.get(0);
		return peer + path;
	}
	
	public synchronized void addStopped(SegDownloader s){
		try {
			bstopped.put(s);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void removeStopped(SegDownloader s){
		bstopped.remove(s);
	}
	
	// Only call this from a downloader thread.
	//	If the active queue is full, the downloader will
	//	block until a spot frees up.
	public void addActive(SegDownloader s){
		try {
			active.put(s);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeActive(SegDownloader s){
		try {
			if (doneSegs < nSeg)
				active.take();
		} catch (InterruptedException e) {
			// Nothing
		}
	}
	
	public synchronized void finishSegment(int s){
			if (s < 0) 
				throw new RuntimeException( "Tried to finish nonexistent segment." );
			segFin[s] = true;
			doneSegs++;
	}
	
	public void printStatus(){
		System.out.println("File: " + outFile() + "\n\tDownload : " + percentDone  + "% completed.");
	}
	
	// Wait on this download to finish. Implemented by trying to push something to the
	//	blocking queue. Since the pusher waits until the queue has space, only empty
	//	the queue if the download is finished.
	public void waitForMe(){
		waiters++;
		try {
			waitToken.put(true);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
