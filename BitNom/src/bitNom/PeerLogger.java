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

public class PeerLogger implements Runnable {
	public List<String> recentPeers = Collections.synchronizedList(new ArrayList<String>());
	public List<String> allEncountered = Collections.synchronizedList(new ArrayList<String>());
	public File fRecentPeers = new File("PeerList.txt");
	public File fAllEncountered = new File("AllEncountered.txt");
	
	public void run(){}
	
	List<String> returnRecent(){
		return recentPeers;
	}
	
	List<String> returnAll(){
		return allEncountered;
	}
	
	public synchronized void appendRecentToAllList(){
		//Append to ArrayList
		allEncountered.addAll(recentPeers);
	}
	
	//Add to ArrayList
	public synchronized void addPeertoList(String name, ArrayList<String> list) {
		
		list.add(name);
	}
		
	//Remove from ArrayList
	public synchronized void removePeerList(String name, ArrayList<String> list){
			
		list.remove(name);
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
