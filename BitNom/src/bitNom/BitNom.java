package bitNom;

public class BitNom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		server = new Server(args);
		(new Thread(server)).start();
		
		downloadMgr = new DownloadManager();
		(new Thread (downloadMgr)).start();
		
		peerLgr = new PeerLogger();
		(new Thread (peerLgr)).start();
		
		Searcher searcher = new Searcher();
		(new Thread (searcher)).start();
		
		rqstHndlr = new RequestHandler(server, downloadMgr, peerLgr, searcher);	
		rqstHndlr.start();

	}
	
	public static Server server;
	public static DownloadManager downloadMgr;
	public static PeerLogger peerLgr;
	public static Searcher searcher;
	public static RequestHandler rqstHndlr;

}
