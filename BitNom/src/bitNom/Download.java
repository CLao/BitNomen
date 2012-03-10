package bitNom;

import java.util.*;

public class Download implements Runnable {
	Download (String dpath, List<String> recentPeers, int segments){
		path = dpath;
		nSeg = segments;
		peers = recentPeers;
		segFin = new boolean[nSeg];
		doneSegs = 0;
	}
	
	public void run() {
		// Create a segment downloader for each segment and run them simultaneously
		for (int i = 0; i < nSeg; i++) {
			segDownloads.add(new SegDownloader(this, peers.get(i % peers.size()) + path));
			(new Thread(segDownloads.get(segDownloads.size() - 1))).start();
		}
		
		// Wait for a download thread to either finish or fail, and update our bookkeeping

		while (!done) {
			synchronized (this) {
				try {
					wait();
					
					if (failed.isEmpty() && doneSegs == nSeg)
					{ 
						done = true;
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			
			while (!failed.isEmpty())
			{
				// Give a new path to every download thread that failed
				SegDownloader s = failed.get(0);
				s.dlPath = getNewPath();
				removeFailed(s);
				synchronized(s) { s.notify(); }
			}
		}
		
		// If any fail, reverify if that peer exists, and then give the thread a new path
		/*while (!done) {
			
			int tempDone = 0;
			for (int i = 0; i < nSeg; i++) {
				SegDownloader cur = segDownloads.get(i);
				synchronized(cur){
					if (cur.status == Dstatus.FINISHED)
						{ tempDone++; }
					if (cur.status == Dstatus.FAILED)
					{
						cur.dlPath = getNewPath();
						notify();
					}
				}
			}
			
			doneSegs = tempDone;
			if (doneSegs == nSeg)
				done = true;
			else try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
	
	String getNewPath(){
		String peer = peers.get(0);
		return peer + path;
	}
	
	void addFailed(SegDownloader s){
		failed.add(s);
	}
	
	void removeFailed(SegDownloader s){
		failed.remove(s);
	}
	
	void finishSegment(SegDownloader s){
		int index = segDownloads.indexOf(s);
		if (index < 0) 
			throw new RuntimeException( "Tried to finish nonexistent segment." );
		
		segFin[index] = true;
		doneSegs++;
		segDownloads.remove(index);
	}
	
	String path;
	int nSeg;
	int doneSegs;
	List<String> peers;
	List<SegDownloader> segDownloads;
	List<SegDownloader> failed;
	boolean segFin[];
	boolean done;
}
