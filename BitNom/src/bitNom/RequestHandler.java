package bitNom;

// TODO:
// When we receive an interest, we read the header to determine if it's a:
// Search request	(Searcher)
// Content request	(CCNFileProxy)
// Bootstrap request (PeerLogger)
// and send it to the proper class for processing.

// The content requests are queued here and serviced in a manner roughly
// following the tit-for-tat plan.

public class RequestHandler implements Runnable{
	RequestHandler(Server sv, DownloadManager dm, PeerLogger pl, Searcher sc){
		server = sv;
		downloadMgr = dm;
		peerLgr = pl;
		searcher = sc;
	}
	
	public void run(){}
	
	Server server;
	DownloadManager downloadMgr;
	PeerLogger peerLgr;
	Searcher searcher;
}
