package bitNom;

import java.io.BufferedReader;
import java.io.Console;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
		peerLgr = new PeerLogger();
		(new Thread (peerLgr)).start();
		
		server = new Server(args, peerLgr); // directory on disk; ccn name
		(new Thread(server)).start();
		
		downloadMgr = new DownloadManager(peerLgr); // peerlist;
		(new Thread (downloadMgr)).start(); // initDownload takes in dest name space, query, what you want to save it (relative to home path), chunks 
		
		//searcher = new Searcher();	
		startBitNom();
		// how do I make a call to download something?
		// 
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
				String file = input.replaceFirst("DOWNLOAD[\\s]+", "");
				String filename = file.replaceFirst("ccnx:/.+/", "");
				String prefix = file.split(filename)[0];
				downloadMgr.initDownload(prefix, filename, filename, 1);
				System.out.println("Downloading " + prefix + " " + filename);
			}
			
			else if(input.matches("SEARCH[\\s]+.*")) {
				System.out.println("Searching for " + input.split("SEARCH[\\s]+.*")[0]);
				// Loop through our list of peers 
				// For each peer
				
				for(String peer : peerLgr.recentPeers)
				{
					String query = ".search " + input.replaceFirst("SEARCH[\\s]+", "");
					Console console = System.console();
					Download q = downloadMgr.initDownload(peer, query, ".results", 1);
					q.waitForMe();
					try {
							FileInputStream fstream = new FileInputStream(Globals.ourHome + "/.results");
							DataInputStream instream = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(instream));
							String strLine;
							
							try {
									while((strLine = br.readLine()) != null)
									{
										System.out.println(strLine);
										System.out.println("To download, type 'y' and press enter. Otherwise, press enter to continue...");
										String reply = console.readLine();
										if(reply == "y")
										{
											Download d = downloadMgr.initDownload(peer, strLine, "download", 1);
											d.waitForMe();
											break; // Exit search
										}
									}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								instream.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
					
				}
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
