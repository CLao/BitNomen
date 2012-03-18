package bitNom;

import java.util.*;

//TODO:
// The DownloadManager, given a known location of a file, looks for identical
// instances of that file over the network. While it's doing this, it
// creates a thread (Downloader) for each segment of the file, and attempts
// to either run them all, or a subset of them all to download the file.

// The idea is to hash the file, and ask other peers if they have a file with that hash.

public class DownloadManager implements Runnable {
	
	static int nDownloads;
	
	DownloadManager (PeerLogger pl){
		peerLgr = pl;
		nDownloads = 5;
		downloads = Collections.synchronizedList(new ArrayList<Download>());
	}
	
	public void run(){
		
		System.out.println("Starting BitNomen Download Manager...");
		
		// Update data about each download here.
		while (true) {
			for (int i = 0; i < downloads.size(); i++)
			{
				Download cur = downloads.get(i);
				float percent = (cur.doneSegs()/cur.nSeg()) * 100;
				System.out.println("Download " + i + ": " + percent  + "% completed. \n\tFile: " + cur.outFile());
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}
	
	// Starts to download a download.
	// Parameters: The ccn prefix of a guaranteed location we can find a file.
	public synchronized Download initDownload(String prefix, String path, String outPath, int segments){
		// Start a thread for the file.
			downloads.add(new Download(path, "/" + outPath, peerLgr.recentPeers, segments));
			(new Thread(downloads.get(downloads.size() - 1))).start();
			
			return downloads.get(downloads.size()-1);
	}
	
	List<Download> downloads;
	PeerLogger peerLgr;
}
