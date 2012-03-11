package bitNom;

import java.util.*;
import java.io.*;

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
	List<String> recentPeers = Collections.synchronizedList(new ArrayList<String>());
	List<String> allEncountered = Collections.synchronizedList(new ArrayList<String>());
	File fRecentPeers = new File("PeerList.txt");
	File fAllEncountered = new File("AllEncountered.txt");
	
	public void run(){}
	
	List<String> returnRecent(){
		return recentPeers;
	}
	
	List<String> returnAll(){
		return allEncountered;
	}
	
	public void appendRecentToAll(){
		//Append to ArrayList
		allEncountered.addAll(recentPeers);
		
		//Append to file
		try{
			  InputStream in = new FileInputStream(fRecentPeers);
			  OutputStream out = new FileOutputStream(fAllEncountered,true);

			  byte[] buf = new byte[1024];
			  int len;
			  while ((len = in.read(buf)) > 0){
				  out.write(buf, 0, len);
			  }
			  in.close();
			  out.close();
		}
		catch(FileNotFoundException ex){
			  System.out.println(ex.getMessage() + " in the specified directory.");
			  System.exit(0);
		}
		catch(IOException e){
			  System.out.println(e.getMessage());  
		}
	}
	//Add to ArrayList
	public void addPeertoList(String name, ArrayList<String> list) {
		
		list.add(name);
	}
	
	//Add to .txt file
	public void addPeertoFile(String name, File file){
		
		try{
    		FileWriter fw = new FileWriter(file.getName(),true);
    	    BufferedWriter bw = new BufferedWriter(fw);
    	    bw.write(name);
    	    bw.close();
    	}
		catch(IOException e){
    		e.printStackTrace();
    	}
	}
		
	//Remove from ArrayList
	public void removePeerList(String name, ArrayList<String> list){
			
		list.remove(name);
	}
	
	//Remove from .txt file
	public void removePeerFile(String name, File file){
		
		try {
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
			catch (IOException e) {
			  e.printStackTrace();
			}
	}
	
	public void updateFile(List<String> list, File file){
		try{
    		FileWriter fw = new FileWriter(file.getName());
    	    BufferedWriter bw = new BufferedWriter(fw);
    	    for (int i = 0; i < list.size(); i++){
    	    	bw.write(list.get(i));
    	    	bw.write("\r\n");
    	    }
    	    bw.close();
    	}
		catch(IOException e){
    		e.printStackTrace();
		}
	}
}
