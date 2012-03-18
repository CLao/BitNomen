package bitNom;

public class Upload implements Runnable{
	
	CCNFileProxy proxy;
	private boolean done;
	
	
	Upload(CCNFileProxy fp){
		proxy = fp;
		done = false;
	}
	
	public void run() {
		
	}
	
}
