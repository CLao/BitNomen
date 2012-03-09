package bitNom;

import org.ccnx.ccn.impl.support.Log;


public class Server implements Runnable {
	String args[];
	public void run(){
		if (args.length < 1) {
			CCNFileProxy.usage();
			return;
		}
		
		String filePrefix = args[0];
		String ccnURI = (args.length > 1) ? args[1] : CCNFileProxy.DEFAULT_URI;
		System.out.println("It compiles!");
		
		try {
			proxy = new CCNFileProxy(filePrefix, ccnURI);
			
			// All we need to do now is wait until interrupted.
			proxy.start();
				
			while (!proxy.finished()) {
				// we really want to wait until someone ^C's us.
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		} catch (Exception e) {
			Log.warning("Exception in ccnFileProxy: type: " + e.getClass().getName() + ", message:  "+ e.getMessage());
			Log.warningStackTrace(e);
			System.err.println("Exception in ccnFileProxy: type: " + e.getClass().getName() + ", message:  "+ e.getMessage());
			e.printStackTrace();
		}
	}
	
	static private CCNFileProxy proxy;
}
