package bitNom;

public class BitNom {
	
	public static Server server;
	public static DownloadManager downloadMgr;
	public static PeerLogger peerLgr;
	public static Searcher searcher;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 1) {
			CCNFileProxy.usage();
			return;
		}
		
		Globals.ourHome = args[0];
		
		server = new Server(args);
		(new Thread(server)).start();
		
		peerLgr = new PeerLogger();
		(new Thread (peerLgr)).start();
		
		downloadMgr = new DownloadManager(peerLgr);
		(new Thread (downloadMgr)).start();
		
		searcher = new Searcher();	
		startBitNom();
		
	}
	
	public static void startBitNom(){}

}
