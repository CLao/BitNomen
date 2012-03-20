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
		
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		downloadMgr = new DownloadManager(peerLgr);
		(new Thread (downloadMgr)).start();
		
		searcher = new Searcher();	
		startBitNom();
		
	}
	
	public static void test(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		peerLgr.addPeertoList("/chris/", peerLgr.recentPeers);
		downloadMgr.initDownload("/chris/", "cat.jpg", "thread.jpg", 6);
	}
	
	public static void startBitNom(){
		test();
	}

}
