package bitNom;
import java.io.File;
import java.io.FilenameFilter;
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
	//static File directory;
	
	public Searcher()
	{
		super();
		//directory = new File(path);
	}
	
	public static Collection<File> matchingFiles(File root, String pattern)
	{
		File[] entries = root.listFiles();
		Vector<File> files = new Vector<File>();
		if(entries != null)
		{
			for(File entry : entries)
			{
					if(entry.isDirectory())
					{
						files.addAll(matchingFiles(entry, pattern));
					}
					else if(entry.getName().contains(pattern))
					{
						files.add(entry);
					}				
			}
		}
		return files;
	}
	
	public void run(){}
}
