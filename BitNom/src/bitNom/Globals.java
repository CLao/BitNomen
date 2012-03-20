package bitNom;
import org.ccnx.ccn.profiles.SegmentationProfile;

public class Globals {
	// Set these booleans to true if you want to have
	//	debug print statements for that particular class,
	//	otherwise false if you want to suppress them.
	public static boolean dbFP = true;			// FileProxy
	public static boolean dbDM = false;			// DownloadManager
	public static boolean dbDL = true;			// Individual downloads
	public static boolean dbSD = true;			// Segment Downloader
	public static boolean dbPL = false;			// PeerLogger
	public static boolean dbSR = false;			// Searcher
	
	// Path variables
	public static String ccnHome = "ccnx:/";	// Home directory on ccn
	public static String ourHome = "/home/Christian/Desktop/";		// Home directory on disk
	
	// Segmentation variables
	public static final long segSize = SegmentationProfile.DEFAULT_BLOCKSIZE;//532480;			// File segment size, in Bytes
}
