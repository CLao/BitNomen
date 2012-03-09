package bitNom;

// TODO:
// When we receive an interest, we read the header to determine if it's a:
// Search request	(Searcher)
// Hash check request (Also Searcher)
// Content request	(CCNFileProxy)
// Bootstrap request (PeerLogger)
// and send it to the proper class for processing.

// The content requests are queued here and serviced in a manner roughly
// following the tit-for-tat plan.

public class RequestHandler implements Runnable{
	public void run(){}
}
