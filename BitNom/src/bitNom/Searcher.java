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
import java.lang.String;
/*
 * Currently, searchDirectory() is called in the constructor of the Searcher class. searchDirectory()
 * is responsible for creating .search, a file which contains a list of all filenames within a given 
 * directory (namespace). In keeping like with like, I'm comfortable with keeping searchDirectory() here.
 * However, in order for searches to be performed by other peers, .search should be created at the earliest
 * possible moment - the Searcher class must be initialized as early as possible.
 * 
 * An actual search is performed with the searchThroughPeers() function. It is a public function that can
 * be called whenever. It requires a list of peers. It functions by downloading each peer's .search file,
 * truncating that .search file based on the query and adding it to a .results file. At the end of this function,
 * all results are displayed on the console for the user to choose from. The selection is then downloaded.
 */

public class Searcher implements Runnable{
	File root;
	
	public Searcher(String home)
	{
		super();
		root = new File(home);
		searchDirectory(root);
	}
	
	/* searchDirectory() searches recursively through a given directory (root).
	 * It records all filenames a local file, called .search.
	 */
	private void searchDirectory(File root)
	{
		File[] entries = root.listFiles();
		if(entries != null)
		{
			for(File entry : entries)
			{
				//String filename = entry.getPath();
				//boolean match = filename.substring(filename.lastIndexOf("/") + 1).compareTo(pattern) == 0;
				if(entry.isDirectory())
				{
					searchDirectory(entry);
				}
				else if(entry.isFile())
				{
					writeToSearch(entry);
				}				
			}
		}
	}
	
	/* writeToSearch() is a helper function for searchDirectory().
	 * It handles the actual creation and writing to .search.
	 */
	private void writeToSearch(File file)
	{
		File searchFile = new File(root.getAbsolutePath() + "/.search");
		// Create the search file, if one does not already exist.
		if(!searchFile.exists())
		{
			try {
				searchFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Append filename to .search
		try {
			FileWriter fstream = new FileWriter(searchFile.getAbsolutePath(), true); // true -> append
			BufferedWriter out = new BufferedWriter(fstream);
			
			String relative =  root.toURI().relativize(file.toURI()).getPath();
			String pathname = root + "/" + relative;
			try {
				System.out.println("Writing to: " + searchFile.getAbsolutePath());
				out.write(pathname);
				out.newLine();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/* searchThroughPeers() looks through the .search files of its peers,
	 * for a given query. It outputs a list of matches for the user to choose
	 * from.
	 */
	public void searchThroughPeers(ArrayList<String> peers)
	{
		File resultsFile = new File(root.getAbsolutePath() + "/.results");
		File searchHelper = new File(root.getAbsolutePath() + "/.searchhelper");
		// Create the .results file, if one does not already exist.
		if(!resultsFile.exists())
		{
			try {
				resultsFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Create the .searchhelper file
		if(!searchHelper.exists())
		{
			try {
				searchHelper.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(String peer : peers)
		{
			// Download .search from peer. Name the file .searchhelper
			// We will download each .search file sequentially.
			// Concatenate .searchhelper with .results
		}
		// Output to screen the file list.
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(root.getAbsolutePath() + "/.results");
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
	
	/* truncateToQuery() is a helper function for searchThroughPeers().
	 * searchThroughPeers() passes a query to truncateToQuery(). truncateToQuery()
	 * truncates .searchhelper based on this query.
	 */
	private void truncateToQuery(String query)
	{
		
	}
	public void run(){}
}
