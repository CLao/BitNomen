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
		downloads = new ArrayList<Download>();
	}
	
	public void run(){
		// Update data about each download here.
		while (true)
		for (int i = 0; i < downloads.size(); i++)
		{
			Download cur = downloads.get(i);
			float percent = cur.doneSegs/cur.nSeg;
			System.out.println("Download " + i + ": " + percent  + "% completed.");
		}
	}
	
	// Starts to download
	public void initDownload(String prefix, String path, int segments){
		// Start a thread for each segment of the file.

			downloads.add(new Download(path, peerLgr.recentPeers, segments));
			(new Thread(downloads.get(downloads.size() - 1))).start();
	}
	
	List<Download> downloads;
	PeerLogger peerLgr;
}
