package bitNom;

import org.ccnx.ccn.io.CCNFileInputStream;

//TODO:
// Make this class run on a thread.
// Decide which peer to download from first. i.e. which order to query the peers in.

// The Downloader should provide methods to:
//- Given a list of known locations of a file and a single segment number,
//	attempt to download that segment from any one of the peers.

public class Downloader implements Runnable{
	public void run(){}
}
