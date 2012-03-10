package bitNom;

//TODO:
// Decide when we "stop" the search. Perhaps give the query a total lifespan.
//		or maybe record the path we've already taken.

// Given a search query, search your own node to see if you have any relevant
// files. If so, return a list of paths to your relevant files, and the number
// of segments in that file.

// Depending on if the content is found on this machine, or some query lifetime,
// dunno yet, forward the query to other peers and forward their results back to
// the original requestor.

// Also takes care of finding a file with a specific hash.

public class Searcher implements Runnable{
	public void run(){}
	
	Searcher(PeerLogger pl){
		peerLgr = pl;
	}
	
	PeerLogger peerLgr;
}
