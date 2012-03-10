package bitNom;

import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

//TODO
// Instead of keeping a persistent connection to other nodes,
// we keep two lists of peers (as strings) that we can send requests to.

// One is the list of peers that we have connected to in the past,
// but we do not currently know if they are online. We use this
// to bootstrap into the network, and to also fall back on if we
// end up losing connection to all other nodes.

// The other is the list of peers we have recently and successfully
// connected to, meaning we should send new requests to these nodes
// first.

// The PeerLogger class should manage these lists, as well as provide
// methods to call in order to add or remove peers from the lists.

// It should also provide a method to give a new node (which needs to
// bootstrap) its own list of peers, and forward to those peers that
// a new node joined.

// It should also maintain a table of downloading files. It will keep 
// track of files not completed downloading and the segments of the files 
// that have completed downloading. This class will allow data to be 
// entered and removed from the table when necessary.

public class PeerLogger implements Runnable {
	ArrayList<String> recentPeers = new ArrayList<String>();
	ArrayList<String> allEncountered = new ArrayList<String>();
	File fRecentPeers = new File("PeerList.txt");
	File fAllEncountered = new File("AllEncountered.txt");
	
	public void run(){}
	
	public void addPeertoList(String name, ArrayList<String> list, File file, String filename) {
		
		//Add to ArrayList
		list.add(name);
		
		//Add to .txt file
		try{
			//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    		}
 
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	    bufferWritter.write(name);
    	    bufferWritter.close();
 
	        System.out.println("Done");
 
    	}
		catch(IOException e){
    		e.printStackTrace();
    	}
	}
	
	
	public void removePeer(String name, ArrayList<String> list, File file){
		
		//Remove from ArrayList
		list.remove(name);
		
		//Remove from .txt file
		try {
			if (!file.isFile()) {
				System.out.println("Parameter is not an existing file");
			    return;
			}

			//Construct the new file that will later be renamed to the original filename.
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			String line = null;

			//Read from the original file and write to the new
			//unless content matches data to be removed.
			while ((line = br.readLine()) != null) {
				if (!line.trim().equals(name)) {
			    pw.println(line);
			    pw.flush();
			    }
			}
			pw.close();
			br.close();

			//Delete the original file
			if (!file.delete()) {
			    System.out.println("Could not delete file");
			    return;
			}
			//Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(file))
			    System.out.println("Could not rename file");
			}
			catch (IOException ex) {
			  ex.printStackTrace();
			}
	}
}
