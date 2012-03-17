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
// Make this class run on a thread.
// Decide which peer to download from first. i.e. which order to query the peers in.

// The Downloader should provide methods to:
//- Given a list of known locations of a file and a single segment number,
//	attempt to download that segment from any one of the peers.

public class SegDownloader implements Runnable {

	String dlPath;
	Dstatus status;
	Download parent;
	int seg;
	
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
					parent.finishSegment(this);
				}
			}
			synchronized (this){
				if (status == Dstatus.FAILED)
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
			}	
		}
		
	}
	
	// Try to download from the current peer.
	public void download(){
		status = Dstatus.DOWNLOADING;
		
		try {
			int readsize = 1024; // make an argument for testing...
			// If we get one file name, put as the specific name given.
			// If we get more than one, put underneath the first as parent.
			// Ideally want to use newVersion to get latest version. Start
			// with random version.
			//ContentName argName = ContentName.fromURI(args[CommonParameters.startArg]);
			ContentName argName = ContentName.fromNative(dlPath);
			
			CCNHandle handle = CCNHandle.open();
			/*
			File theFile = new File(args[CommonParameters.startArg + 1]);
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
			if (CommonParameters.timeout != null) {
				input.setTimeout(CommonParameters.timeout); 
			}
			byte [] buffer = new byte[readsize];
			ByteBuffer buf = ByteBuffer.wrap(buffer);
			
			int readcount = 0;
			long readtotal = 0;
			int readtimes = 0;
			
			//while (!input.eof()) {
			while ((readcount = input.read(buffer)) != -1 && readtotal < Globals.segSize){
				//readcount = input.read(buffer);
				readtotal += readcount;
				
				parent.channel.write(buf, (Globals.segSize * seg) + (readsize * readtimes));
				readtimes++;
				//output.write(buffer, 0, readcount);
				//output.flush();
			}
			
			
			if (Globals.dbDL){
				System.out.println("Segment took: "+(System.currentTimeMillis() - starttime)+"ms");
				System.out.println("Retrieved Segment " + seg + " of " + dlPath + " got " + readtotal + " bytes.");
			}
			//System.exit(0);

		} catch (ConfigurationException e) {
			System.out.println("Configuration exception in ccngetfile: " + e.getMessage());
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			System.out.println("Malformed name: " + dlPath + " " + e.getMessage());
			synchronized(parent){
				status = Dstatus.FAILED;
				parent.bstopped.add(this);
			}
		} catch (IOException e) {
			System.out.println("Cannot write file or read content. " + e.getMessage());
			synchronized(parent){
				status = Dstatus.FAILED;
				parent.bstopped.add(this);
			}
		}
	}
}
