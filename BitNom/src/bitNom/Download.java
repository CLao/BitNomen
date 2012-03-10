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
			segDownloads.add(new SegDownloader(peers.get(i % peers.size()) + path));
			(new Thread(segDownloads.get(segDownloads.size() - 1))).start();
		}
		
		// If any fail, reverify if that peer exists, and then give the thread a new path
		while (!done) {
			
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
		}
	}
	
	String getNewPath(){
		String peer = peers.get(0);
		return peer + path;
	}
	
	String path;
	int nSeg;
	int doneSegs;
	List<String> peers;
	List<SegDownloader> segDownloads;
	boolean segFin[];
	boolean done;
}
