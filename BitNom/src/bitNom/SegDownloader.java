package bitNom;

import org.ccnx.ccn.io.CCNFileInputStream;
import java.io.IOException;
import java.nio.*;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
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
		parent.addActive(this);
		while (status != Dstatus.FINISHED) {
			String oldPath = dlPath;
			download();
			synchronized (parent) {		
				// If the downloading failed, notify the Download to give us a new path.
				if (status == Dstatus.FAILED)
				{ 
					parent.addStopped(this);
				}
				
				// If the downloading succeeded, notify the Download we're done
				else if (status == Dstatus.FINISHED)
				{
					parent.finishSegment(seg);
					parent.removeActive(this);
					parent.addStopped(this);
				}
			}
			synchronized (this){
				if (status == Dstatus.FAILED)
					try {
						// Wait for our download path to update.
						// This is in a while loop in case the Download gives us a new download
						//	before we even start waiting, or if we get woken up by
						//	something besides the Download somehow.
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
	// This section is mostly copied and pasted from ccngetfile.
	//	Except the file output stream was changed into a file channel
	//	in order to facilitate skipping by some byte offset.
	private void download(){
		status = Dstatus.DOWNLOADING;
		
		try {
			int readsize = 1024; // make an argument for testing...
			// If we get one file name, put as the specific name given.
			// If we get more than one, put underneath the first as parent.
			// Ideally want to use newVersion to get latest version. Start
			// with random version.
			ContentName argName = ContentName.fromURI(dlPath);
			CCNHandle handle = CCNHandle.open();
			long starttime = System.currentTimeMillis();
			CCNInputStream input;
			if (CommonParameters.unversioned)
				input = new CCNInputStream(argName, handle);
			else
				input = new CCNFileInputStream(argName, handle);

			// Set the stream timeout to be pretty high. It takes a while
			//	for the other end to generate the segment.
			input.setTimeout(100000);
			
			// Buffer to read from the ccn stream
			byte [] buffer = new byte[readsize];
			ByteBuffer buf = ByteBuffer.wrap(buffer);
			
			int readcount = 0;
			int readtotal = 0;
			long readtimes = 0;
			
			// Seek the stream to the point where our segment starts
			// This gets done in both the uploader and downloader.
			//	The uploader does it to serve the segment it needs to,
			//	and the downloader does it to automatically make the stream
			//	request the correct segment. A seek attempts to not get
			//	more bytes than is necessary from the stream.
			long position = (Globals.segSize * seg);
			input.seek(position);
			

			
			// While we can still read bytes from the ccn stream*
			//	*The method auto-blocks. It *should* only fail when we reach the end.
			while ((readcount = input.read(buffer)) != -1 && readtotal < Globals.segSize){
				
				readtotal += readcount;
				parent.channel.write(buf, position + readtimes * readsize);
				buf.position(0);	
				
				// Update the percent we have downloaded.
				parent.percentDone += 1 + readcount/(Globals.segSize * parent.nSeg());
				readtimes++;
			}
			
			// Truncate the file if we're the last segment, since we wrote past the actual end of the file.
			if (seg == parent.nSeg() - 1)
			{
				parent.channel.truncate(((parent.nSeg() - 1) * Globals.segSize) + readtotal);
			}
			
			
			if (Globals.dbDL || Globals.dbSD){
				System.out.println("Segment took: "+(System.currentTimeMillis() - starttime)+"ms");
				System.out.println("Retrieved Segment " + seg + " of " + dlPath + " got " + readtotal + " bytes.");
			}
			status = Dstatus.FINISHED;

			// Exceptions mostly untouched from ccngetfile, except instead of exiting, we tell the
			//	Download that we failed.
		} catch (ConfigurationException e) {
			System.out.println("Configuration exception in ccngetfile: " + e.getMessage());
			synchronized(parent){
				status = Dstatus.FAILED;
				//parent.bstopped.add(this);
			}
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
