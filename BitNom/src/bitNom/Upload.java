package bitNom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.CCNFileOutputStream;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.CCNTime;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;

public class Upload implements Runnable{
	
	CCNFileProxy proxy;
	public boolean done;
	private Interest outInterest;

	
	Upload(CCNFileProxy fp, Interest interest){
		proxy = fp;
		done = false;
		outInterest = interest;

	}
	
	public void run() {
		
		// Add ourselves to the uploader set. If the proxy already has
		//	maxUploads uploaders running, we'll block until there is a
		//	free spot.
		proxy.addUploader(this);
		
		try {
			writeFile(outInterest);
			done = true;
		} catch (IOException e) {
			System.out.println("Upload failed!");
		}
	}
	
	// The stand-in version of writeFile. Does the same thing as the normal
	//	FileProxy's writeFile.
	
	protected boolean writeFile(Interest outstandingInterest) throws IOException {
			
			File fileToWrite = proxy.ccnNameToFilePath(outstandingInterest.name());
			if (Globals.dbFP)Log.info("CCNFileProxy: extracted request for file: " + fileToWrite.getAbsolutePath() + " exists? ", fileToWrite.exists());
			if (!fileToWrite.exists()) {
				Log.warning("File {0} does not exist. Ignoring request.", fileToWrite.getAbsoluteFile());
				return false;
			}
			
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(fileToWrite);
			} catch (FileNotFoundException fnf) {
				Log.warning("Unexpected: file we expected to exist doesn't exist: {0}!", fileToWrite.getAbsolutePath());
				return false;
			}
			
			// Set the version of the CCN content to be the last modification time of the file.
			CCNTime modificationTime = new CCNTime(fileToWrite.lastModified());
			ContentName versionedName = 
				VersioningProfile.addVersion(new ContentName(proxy._prefix, 
							outstandingInterest.name().postfix(proxy._prefix).components()), modificationTime);
	
			// CCNFileOutputStream will use the version on a name you hand it (or if the name
			// is unversioned, it will version it).
			CCNFileOutputStream ccnout = new CCNFileOutputStream(versionedName, proxy._handle);
			
			// We have an interest already, register it so we can write immediately.
			ccnout.addOutstandingInterest(outstandingInterest);
			
			byte [] buffer = new byte[CCNFileProxy.BUF_SIZE];
			
			int read = fis.read(buffer);
			while (read >= 0) {
				ccnout.write(buffer, 0, read);
				read = fis.read(buffer);
			} 
			fis.close();
			ccnout.close(); // will flush
			
			return true;
		}

	
	// The version of writeFile we want to use, which assumes a segment = a chunk
	//	Unusable as long as we can't send a single stream greater than 500KB
	/*
	protected boolean writeFile(Interest outstandingInterest) throws IOException {
			
		long segNum = 0; 
		if (SegmentationProfile.isSegment(outInterest.name()))
			segNum = SegmentationProfile.getSegmentNumber(outInterest.name());
		long segPos = segNum * Globals.segSize;
		
			File fileToWrite =  proxy.ccnNameToFilePath(outstandingInterest.name());
			if (Globals.dbFP)Log.info("ThreadUpload: extracted request for file: " + fileToWrite.getAbsolutePath() + " exists? ", fileToWrite.exists());
			if (!fileToWrite.exists()) {
				Log.warning("File {0} does not exist. Ignoring request.", fileToWrite.getAbsoluteFile());
				return false;
			}
			
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(fileToWrite);
			} catch (FileNotFoundException fnf) {
				Log.warning("Unexpected: file we expected to exist doesn't exist: {0}!", fileToWrite.getAbsolutePath());
				return false;
			}
			
			// Set the version of the CCN content to be the last modification time of the file.
			CCNTime modificationTime = new CCNTime(fileToWrite.lastModified());
			ContentName versionedName = 
				VersioningProfile.addVersion(new ContentName(proxy._prefix, 
							outstandingInterest.name().postfix(proxy._prefix).components()), modificationTime);
	
			// CCNFileOutputStream will use the version on a name you hand it (or if the name
			// is unversioned, it will version it).
			CCNFileOutputStream ccnout = new CCNFileOutputStream(versionedName, proxy._handle);
			
			// Set the size of the segment
			//ccnout.setBlockSize((int)Globals.segSize);
			
			// We have an interest already, register it so we can write immediately.
			ccnout.addOutstandingInterest(outstandingInterest);
			
			byte [] buffer = new byte[CCNFileProxy.BUF_SIZE];
			boolean succeeded = false;
			
			// Skip the amount of bytes until we get to the next segment
			long skipped = fis.skip(segNum * Globals.segSize);
			if (skipped == segNum * Globals.segSize){
				succeeded = true;
				int read = fis.read(buffer);
				int total = read;
				while (read >= 0 && total < Globals.segSize) {
					ccnout.write(buffer, 0, read);
					read = fis.read(buffer);
					total += read;
				} 
			}
			fis.close();
			ccnout.close(); // will flush
			
			// Let the proxy know we can give up our space.
			proxy.removeUploader();

			return succeeded;
		}
	 */
}
