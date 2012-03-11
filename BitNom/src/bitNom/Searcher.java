package bitNom;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.lang.String;
//TODO:
// Decide when we "stop" the search. Perhaps give the query a total lifespan.
//		or maybe record the path we've already taken.

// Given a search query, search your own node to see if you have any relevant
// files. If so, return a list of paths to your relevant files.

// Depending on if the content is found on this machine, or some query lifetime,
// dunno yet, forward the query to other peers and forward their results back to
// the original requestor.

// Also takes care of finding a file with a specific hash.

public class Searcher implements Runnable{
	File root;
	Collection<File> queryResults;
	
	public Searcher(String home, String query)
	{
		super();
		root = new File(home);
		queryResults = matchingFiles(root, query);
		
		// Write query results to .search file on the local repo
		writeToSearch();
	}
	
	private Collection<File> matchingFiles(File root, String pattern)
	{
		File[] entries = root.listFiles();
		Vector<File> files = new Vector<File>();
		if(entries != null)
		{
			for(File entry : entries)
			{
					String filename = entry.getPath();
					boolean match = filename.substring(filename.lastIndexOf("/") + 1).compareTo(pattern) == 0;
					if(entry.isDirectory())
					{
						files.addAll(matchingFiles(entry, pattern));
					}
					else if(match)
					{
						files.add(entry);
					}				
			}
		}
		return files;
	}
	
	private void writeToSearch()
	{
		try {
			FileWriter fstream = new FileWriter(root.getPath() + "/.search");
			BufferedWriter out = new BufferedWriter(fstream);
			for(File file : queryResults)
			{
				String relative =  root.toURI().relativize(file.toURI()).getPath();
				String pathname = root + "/" + relative;
				try {
					System.out.println("Writing to: " + root.getPath() + "/.search");
					out.write(pathname);
					out.newLine();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				out.close();
				System.out.println("Done.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void searchThroughPeers(ArrayList<String> peers)
	{
		// Create a .results file
		for(String peer : peers)
		{
			// Download .search from peer. Name the file .searchhelper
			// We will download each .search file sequentially.
			// Concatenate .searchhelper with .results
		}
		// Output to screen the file list.
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(root.getPath() + "/.results");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			try {
				while((strLine = br.readLine()) != null)
				{
					System.out.println(strLine);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Optionally, we could check to see if there is only one result and
		// download that automatically.
	}
	public void run(){}
}
