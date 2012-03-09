package bitNom;

//TODO:
// The DownloadManager, given a known location of a file, looks for identical
// instances of that file over the network. While it's doing this, it
// creates a thread (Downloader) for each segment of the file, and attempts
// to either run them all, or a subset of them all to download the file.

// The idea is to hash the file, and ask other peers if they have a file with that hash.

public class DownloadManager implements Runnable {
	public void run(){}
}
