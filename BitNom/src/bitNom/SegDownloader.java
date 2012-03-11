package bitNom;

import org.ccnx.ccn.io.CCNFileInputStream;

//TODO:
// Make this class run on a thread.
// Decide which peer to download from first. i.e. which order to query the peers in.

// The Downloader should provide methods to:
//- Given a list of known locations of a file and a single segment number,
//	attempt to download that segment from any one of the peers.

public class SegDownloader implements Runnable {

	SegDownloader(Download par, String ccnPath, int segNum){
		parent = par;
		dlPath = ccnPath;
		status = Dstatus.NONE;
		seg = segNum;
	}
	
	public void run(){
		
		while (status != Dstatus.FINISHED) {
			
				download();
				synchronized (parent) {		
				// If the download failed, wait for the Download to give us a new path.
				if (status == Dstatus.FAILED)
				{ 
					parent.addStopped(this);
				}
				
				if (status == Dstatus.FINISHED)
				{
					parent.finishSegment(this);
				}
				
				synchronized (this){
					if (status == Dstatus.FAILED)
						try {
							wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
				}	
			}
		}
		
	}
	
	// Try to download from the current peer.
	public void download(){
		
	}
	
	String dlPath;
	Dstatus status;
	Download parent;
	int seg;
}
