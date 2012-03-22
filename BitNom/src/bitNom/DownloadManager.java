package bitNom;

import java.util.concurrent.LinkedBlockingQueue;

//TODO:
// The DownloadManager, given a known location of a file, looks for identical
// instances of that file over the network. While it's doing this, it
// creates a thread (Downloader) for each file, and attempts
// to either run them all, or a subset of them all to download the file.

// The idea is to hash the file, and ask other peers if they have a file with that hash.

public class DownloadManager implements Runnable {
	
	static int nDownloads;
	
	DownloadManager (PeerLogger pl){
		peerLgr = pl;
		nDownloads = 5;
		downloads = new LinkedBlockingQueue<Download>();
	}
	
	public void run(){
		
		System.out.println("Starting BitNomen Download Manager...");
		
		// Update data about each download here.
		while (true) {
			
			// Print the status of each download. Block if we have no downloads.
			printStatus(true);
		
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}
	
	// Prints all download statuses. 
	//	If withBlock is true, thread blocks until
	//	there is at least one download.
	public void printStatus(boolean withBlock){
		if (!downloads.isEmpty() || withBlock)
			try {
				Download cur;
				cur = downloads.take();
				cur.printStatus();
				if(cur.percentDone() != 100)
				{
					downloads.add(cur);
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
			}
	}
	
	// Starts to download a download, and returns a reference to it in case you want to wait on it.
	// Parameters: The ccn prefix of a guaranteed location we can find a file, the path to the file, 
	//	the filepath to save it in, and the number of segments in the file.
	public synchronized Download initDownload(String prefix, String path, String outPath, int chunks){
		// Start a thread for the file.
		
			Download newDownload = new Download(path, "/" + outPath, peerLgr.recentPeers, chunks);
			downloads.add(newDownload);
			(new Thread(newDownload)).start();
			return newDownload;
	}
	
	//List<Download> downloads;
	LinkedBlockingQueue<Download> downloads;
	PeerLogger peerLgr;
}
