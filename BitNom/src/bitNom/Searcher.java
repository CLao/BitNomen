package bitNom;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
 * Currently, truncateToQuery() looks for files names with an exact postfix match. For example, if I perform
 * a search for a file named "foo" and had the files:
 * 	folder/foo
 * 	folder/foo.txt
 * 	
 * Only folder/foo would return as a result. The reverse is also true. I'm considering implementing an option 
 * to ignore extensions...
 * 
 * An actual search is performed with the searchThroughPeers() function. It is a public function that can
 * be called whenever. It requires a list of peers. It functions by downloading each peer's .search file,
 * truncating that .search file based on the query and adding it to a .results file. At the end of this function,
 * all results are displayed on the console for the user to choose from. The selection is then downloaded.
 */

public class Searcher{
	File root;
	String query;
	String returnFile;
	
	public Searcher(String rf)
	{
		super();
		root = new File(Globals.ourHome);
		int offset = rf.lastIndexOf('/') + 9;
		String temp = rf.substring(offset);
		offset = temp.lastIndexOf('.');
		temp = temp.substring(0, offset);
		offset = temp.lastIndexOf('.');
		query = temp.substring(0, offset);
		
		offset = rf.lastIndexOf('/') + 1;
		temp = rf.substring(offset);
		offset = temp.lastIndexOf('.');
		temp = temp.substring(0, offset);
		offset = temp.lastIndexOf('.');
		returnFile = temp.substring(0, offset);
		//System.out.println("Download to " + returnFile);
	}
	
	/* searchDirectory() searches recursively through a given directory (root).
	 * It records all filenames a local file, called ".search query."
	 */
	public void searchDirectory(File root)
	{
		File[] entries = root.listFiles();
		File searchFile = new File(root.getAbsolutePath() + "/" + returnFile);
		// Delete the .search file, if one already exists.
		if(searchFile.exists())
		{
			searchFile.delete();
		}
		if(entries != null)
		{
			for(File entry : entries)
			{
				if(entry.isDirectory())
				{
					searchDirectory(entry);
				}
				else if(entry.isFile())
				{
					int offset = entry.getPath().lastIndexOf("/") + 1;
					if(entry.getPath().substring(offset).contains(query))
					{
						String relative =  root.toURI().relativize(entry.toURI()).getPath();
						System.out.println("Apending to " + searchFile.getAbsolutePath());
						//String pathname = root + "/" + relative;
						appendToFile(searchFile.getAbsolutePath(), relative);
					}
				}				
			}
		}
	}
	
	/* appendToFile() is a helper function for searchDirectory() and truncateToQuery.
	 * It takes in a filename to write to and a string to append. It handles the 
	 * actual creation of and writing to files.
	 */
	private void appendToFile(String file, String string)
	{
		File toAppend = new File(file);
		// Create the file, if one does not already exist.
		if(!toAppend.exists())
		{
			try {
				toAppend.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Append filename to file
		try {
			FileWriter fstream = new FileWriter(toAppend.getAbsolutePath(), true); // true -> append
			BufferedWriter out = new BufferedWriter(fstream);
						
			try {
				System.out.println("Writing to: " + string);
				out.write(string);
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
		
		// Delete the .results file, if one already exist.
		if(resultsFile.exists())
		{
			resultsFile.delete();
		}
		
		// Create the .searchhelper file
		/* This is created by the download function?
		if(!searchHelper.exists())
		{
			try {
				searchHelper.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		for(String peer : peers)
		{
			// Download .search from peer. Name the file .searchhelper
			// We will download each .search file sequentially.
			// Call truncateToQuery
			// Delete .searchhelper
		}
		
		// Output to screen the file list.	
		try {
			FileInputStream fstream = new FileInputStream(root.getAbsolutePath() + "/.results");
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
	public void truncateToQuery(String query)
	{
		// Make sure this file is closed before calling this function
		// For testing, using .search
		File searchHelper = new File(root.getAbsolutePath() + "/.search");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(searchHelper));
			String currentLine;
			
			while((currentLine = reader.readLine()) != null) 
			{
				boolean match = currentLine.substring(currentLine.lastIndexOf("/") + 1).compareTo(query) == 0;
			    if(match)
			    {
			    	// Append match to .results
					appendToFile(root.getAbsolutePath() + "/.results", currentLine);
			    }
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
