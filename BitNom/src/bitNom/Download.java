package bitNom;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.channels.*;

import org.ccnx.ccn.impl.support.Log;


public class Download implements Runnable {
	
	private String path;
	private String outFile;
	private int nSeg;
	private int doneSegs;
	public List<String> peers;
	private List<SegDownloader> segDownloads;
	public ArrayBlockingQueue<SegDownloader> bstopped;
	private boolean segFin[];
	private boolean done;
	public Dstatus status;
	
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
	}
	
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
				segDownloads.add(new SegDownloader(this, "ccnx:/" + peers.get(i % peers.size()) + path, i));
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
							System.out.println("Download " + Globals.ourHome + outFile + " Finished!");
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
	
	private synchronized String getNewPath(){
		String peer = peers.get(0);
		return peer + path;
	}
	
	public synchronized void addStopped(SegDownloader s){
		//stopped.add(s);
		bstopped.add(s);
	}
	
	synchronized void removeStopped(SegDownloader s){
		//stopped.remove(s);
		bstopped.remove(s);
	}
	
	synchronized void finishSegment(int s){
			if (s < 0) 
				throw new RuntimeException( "Tried to finish nonexistent segment." );
			
			segFin[s] = true;
			doneSegs++;
	}
	

}
