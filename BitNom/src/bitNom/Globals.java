package bitNom;

public class Globals {
	// Set these booleans to true if you want to have
	//	debug print statements for that particular class,
	//	otherwise false if you want to suppress them.
	public static boolean dbFP = false;			// FileProxy
	public static boolean dbDM = false;			// DownloadManager
	public static boolean dbPL = false;			// PeerLogger
	public static boolean dbSR = false;
	public static boolean dbDL = false;
	public static boolean dbSD = false;
	
	// Path variables
	public static String ccnHome = "ccnx:/";	// Home directory on ccn
	public static String ourHome;				// Home directory on disk
	
	// Segmentation variables
	public static int segSize = 524288;			// File segment size, in KiB
}
