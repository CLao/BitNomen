package bitNom;

public class BitNom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		server = new Server(args);
		(new Thread(server)).start();
		
		peerLgr = new PeerLogger();
		(new Thread (peerLgr)).start();
		
		downloadMgr = new DownloadManager(peerLgr);
		(new Thread (downloadMgr)).start();
		
		Searcher searcher = new Searcher(peerLgr);
		(new Thread (searcher)).start();
		
		rqstHndlr = new RequestHandler(server, downloadMgr, peerLgr, searcher);	
		(new Thread (rqstHndlr)).start();
		
		startBitNom();
		
	}
	
	public static void startBitNom(){}
	
	public static Server server;
	public static DownloadManager downloadMgr;
	public static PeerLogger peerLgr;
	public static Searcher searcher;
	public static RequestHandler rqstHndlr;

}
