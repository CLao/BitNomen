package bitNom;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.channels.*;

import org.ccnx.ccn.impl.support.Log;


public class Download implements Runnable {
	
	String path;
	String outFile;
	int nSeg;
	int doneSegs;
	List<String> peers;
	List<SegDownloader> segDownloads;
	ArrayBlockingQueue<SegDownloader> bstopped;
	boolean segFin[];
	boolean done;
	Dstatus status;
	
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
	}
	
	public void run() {
		//Create a file channel for the file
		try {
			file = new RandomAccessFile (Globals.ourHome + outFile, "rwd");
			channel = file.getChannel();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.info("Could not open file for download!");
		}
		// Create a segment downloader for each segment and run them simultaneously
		for (int i = 0; i < nSeg; i++) {
			segDownloads.add(new SegDownloader(this, peers.get(i % peers.size()) + path, i));
			(new Thread(segDownloads.get(segDownloads.size() - 1))).start();
		}
		
		// Wait for a download thread to either finish or fail, and update our bookkeeping
			
		try {
			while (!done)
			{
				// Give a new path to every download thread that failed
				SegDownloader s = bstopped.take();
				if (s.status == Dstatus.FAILED)
					synchronized(s) {
						s.dlPath = getNewPath();
						removeStopped(s);
						s.notify(); 
					}
				else if (s.status == Dstatus.FINISHED)
				{
					if (nSeg == doneSegs)
						done = true;
				}
				else throw new RuntimeException("Impossible");
				
			}
		} catch (InterruptedException e){}
		finally{
			System.out.println("Download " + Globals.ourHome + outFile + " Finished!");
		}
		
		
	}
	
	synchronized String getNewPath(){
		String peer = peers.get(0);
		return peer + path;
	}
	
	synchronized void addStopped(SegDownloader s){
		//stopped.add(s);
		bstopped.add(s);
	}
	
	synchronized void removeStopped(SegDownloader s){
		//stopped.remove(s);
		bstopped.remove(s);
	}
	
	synchronized void finishSegment(SegDownloader s){
			int index = segDownloads.indexOf(s);
			if (index < 0) 
				throw new RuntimeException( "Tried to finish nonexistent segment." );
			
			segFin[index] = true;
			doneSegs++;
			segDownloads.remove(index);
	}
	

}
