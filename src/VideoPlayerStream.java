import java.io.BufferedReader;
import java.io.InputStreamReader;


public class VideoPlayerStream implements Runnable {

	public String windowId;
	public String url;
	public String mpv;
	public Thread t1;
	
	public VideoPlayerStream(String windowId, String url) {
		this.windowId = windowId;
		this.url = url;
	}
	
	@Override
	public void run() {
		mpv = "DRI_PRIME=1 mpv --no-cache --volume 0 --keep-open --profile=low-latency -wid "+windowId+" "+url;
        System.out.println(mpv);
        try {
            Process process = 
                new ProcessBuilder(new String[] {"bash","-c", mpv})
                    .redirectErrorStream(true)
                    .start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                String line = null;
                while ( (line = br.readLine()) != null ) {
                }
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void play() {
		t1 =new Thread(this);  
		t1.start();  
	}
	
	public void stop() {
		String cmd = "pkill -9 "+mpv;
		try {
            Process process = 
                new ProcessBuilder(new String[] {"bash","-c", cmd})
                    .redirectErrorStream(true)
                    .start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                String line = null;
                while ( (line = br.readLine()) != null ) {
                   
                }
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
}

