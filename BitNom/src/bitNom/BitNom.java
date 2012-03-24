package bitNom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
	
	public static void test(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		peerLgr.addPeertoList("/chris/", peerLgr.recentPeers);
		downloadMgr.initDownload("/chris/", "epic.jpg", "con.jpg", 1);
	}
	
	public static void startBitNom(){
		//test();
		Boolean quit = false;
		String input;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("BitNomen v0.0.1\n COMMANDS:\n\tDOWNLOAD <ccn filepath>\n\tSEARCH <query>\n\tSTATUS\n\tQUIT");
		
		while(!quit){
			try {
				input = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Error reading standard input.");
				input = "";
			}
			
			if(input.matches("DOWNLOAD[\\s]+.*")) {
				System.out.println("Downloading " + input.split("DOWNLOAD[\\s]+.*")[0]);
			}
			
			else if(input.matches("SEARCH[\\s]+.*")) {
				System.out.println("Searching for " + input.split("SEARCH[\\s]+.*")[0]);
			}
			
			else if(input.matches("STATUS")) {
				downloadMgr.printStatus(false);
			}
			
			else if(input.matches("QUIT")) {
				System.out.println("Goodbye!");
				quit = true;
				System.exit(0);
			}
			
			else {
				System.out.println("Invalid input: " + input);
				input = "";
			}
		}
	}

}
