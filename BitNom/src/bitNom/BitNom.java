package bitNom;

import bitNom.Server;

public class BitNom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.args = args;
		(new Thread(server)).start();
		
		DownloadManager downloadMgr = new DownloadManager();
		(new Thread (downloadMgr)).start();
		
		PeerLogger PeerLgr = new PeerLogger();
		(new Thread (PeerLgr)).start();
		
		Searcher searcher = new Searcher();
		(new Thread (searcher)).start();
		
		RequestHandler RqstHndlr = new RequestHandler();
		(new Thread (RqstHndlr)).start();
		
	}
	

}
