package bitNom;

import org.ccnx.ccn.io.CCNFileInputStream;
import java.io.IOException;
import java.nio.*;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
//import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNInputStream;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.ccnx.ccn.utils.CommonParameters;


//TODO:
// Decide which peer to download from first. i.e. which order to query the peers in.

// The Downloader should provide methods to:
//- Given a list of known locations of a file and a single segment number,
//	attempt to download that segment from any one of the peers.

public class SegDownloader implements Runnable {

	public String dlPath;
	private Dstatus status;
	private Download parent;
	private int seg;
	
	public Dstatus status(){ return status; }
	public int seg() { return seg; }
	
	SegDownloader(Download par, String ccnPath, int segNum){
		parent = par;
		dlPath = ccnPath;
		status = Dstatus.NONE;
		seg = segNum;
	}
	
	public void run(){
		while (status != Dstatus.FINISHED) {
			
			download();
			synchronized (parent) {		
			// If the download failed, wait for the Download to give us a new path.
				if (status == Dstatus.FAILED)
				{ 
					parent.addStopped(this);
				}
				
				if (status == Dstatus.FINISHED)
				{
					parent.finishSegment(seg);
					parent.addStopped(this);
				}
			}
			synchronized (this){
				if (status == Dstatus.FAILED)
					try {
						String oldPath = dlPath;
						while (oldPath == dlPath)
						{
							wait();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
			}	
		}
		
	}
	
	// Try to download from the current peer.
	private void download(){
		status = Dstatus.DOWNLOADING;
		
		try {
			int readsize = 1024; // make an argument for testing...
			// If we get one file name, put as the specific name given.
			// If we get more than one, put underneath the first as parent.
			// Ideally want to use newVersion to get latest version. Start
			// with random version.
			ContentName argName = ContentName.fromURI(dlPath);
			//ContentName argName = ContentName.fromNative(dlPath);
			
			CCNHandle handle = CCNHandle.open();
			/*File theFile = new File(args[CommonParameters.startArg + 1]);
			if (theFile.exists()) {
				System.out.println("Overwriting file: " + args[CommonParameters.startArg + 1]);
			}
			FileOutputStream output = new FileOutputStream(theFile);
			*/
			long starttime = System.currentTimeMillis();
			CCNInputStream input;
			if (CommonParameters.unversioned)
				input = new CCNInputStream(argName, handle);
			else
				input = new CCNFileInputStream(argName, handle);
			/*if (CommonParameters.timeout != null) {
				input.setTimeout(CommonParameters.timeout); 
			}*/
			
			input.setTimeout(10000000);
			
			// Give the other end time to create their file if they still need to.
			
			//readsize = Globals.segSize;
			byte [] buffer = new byte[readsize];
			ByteBuffer buf = ByteBuffer.wrap(buffer);
			
			int readcount = 0;
			long readtotal = 0;
			int readtimes = 0;
			//int timesneeded = (Globals.segSize / readsize);
			input.seek(Globals.segSize * seg);
			//while (!input.eof()) {
			while ((readcount = input.read(buffer)) != -1){
				readtotal += readcount;

				parent.channel.write(buf, (Globals.segSize * seg) + (readsize * readtimes));
				parent.percentDone += readcount/(Globals.segSize * parent.nSeg());
				readtimes++;
			}
			
			// Truncate the file if we're the last segment.
			if (seg == parent.nSeg() - 1)
			{
				parent.channel.truncate(((parent.nSeg() - 1) * Globals.segSize) + readtotal);
			}
			
			//if (readtimes < timesneeded )
			//{
			//	throw new IOException("Download failed! " + readtimes + " is less than " + timesneeded + "!");
			///}
			
			if (Globals.dbDL){
				System.out.println("Segment took: "+(System.currentTimeMillis() - starttime)+"ms");
				System.out.println("Retrieved Segment " + seg + " of " + dlPath + " got " + readtotal + " bytes.");
			}
			status = Dstatus.FINISHED;
			//System.exit(0);

		} catch (ConfigurationException e) {
			System.out.println("Configuration exception in ccngetfile: " + e.getMessage());
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			System.out.println("Malformed name: " + dlPath + " " + e.getMessage());
			synchronized(parent){
				status = Dstatus.FAILED;
				//parent.bstopped.add(this);
			}
		} catch (IOException e) {
			System.out.println("Cannot write file or read content. " + e.getMessage());
			synchronized(parent){
				status = Dstatus.FAILED;
				//parent.bstopped.add(this);
			}
		}
	}
}
