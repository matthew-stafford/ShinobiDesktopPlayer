import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

public class VideoPlaybackController implements Runnable{

	private ShinobiMonitor monitor;
	private String windowId;
	private Thread controller, player;
	private String baseUrl;
	private volatile boolean exit = false;
	private status status;
	private Date date;
	private boolean launchedMpv = false;
	
	private String video;
	private int seekPos;
		
	private enum status {
		WAITING,
		READY
	}
	
	// load video at date on each monitor and wait for all to load
	// play all at same time
	public VideoPlaybackController(ShinobiMonitor monitor, Date date, String windowId) {
		this.monitor = monitor;
		this.date = date;
		monitor.LoadVideos(date);
		this.windowId = windowId;
	}
	
	@Override
	public void run() {
		// load videos data
		
		while (!exit) {	
			try {
				if (!monitor.loadingVideos) {	

					if (video == null) {
						int videoIndex = monitor.getVideoFileIndex(date);
						if (videoIndex >= 0) { 
							video = monitor.getVideoFilename(date, videoIndex);
							seekPos = monitor.getVideoFileSeekPos(date, videoIndex);
							
							System.out.println("opening video " + video + " seekpos "+seekPos);
							
							MPVRunner mpv = new MPVRunner(windowId, monitor.host+"/"+monitor.api_key+"/videos/"+monitor.group_key+"/"+monitor.mid+"/"+video,seekPos);
							
						}			
					}
					Thread.sleep(100);
				} else {
					status = status.WAITING;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		cleanup();
	}
	
	public void cleanup() {
		exit = true;
		
		removePipe();
		
		if (player != null) {
			player.stop();
			player = null;
		}
		if (controller != null) {
			controller.stop();
			controller = null;
		}
	}
	
	public void play() {
		if (controller != null) {
			cleanup();
		}
		
		exit = false;
		
		controller = new Thread(this);
		controller.start();
	}
	
	private class MPVRunner implements Runnable {
		String url;
		String windowId;
		int seekPos;
		
		public MPVRunner(String windowId, String url, int seekPos) {
			this.url = url;
			this.windowId = windowId;
			this.seekPos = seekPos;
			
			Thread t1 = new Thread(this);
			t1.start();
		}
		
		@Override
		public void run() {
			//--input-ipc-server=/tmp/cctv_"+windowId+" 
			String cmd = "mpv --profile=low-latency --start=+"+seekPos+" -wid "+windowId+" "+url;
			System.out.println(cmd);
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
	
	private void removePipe() {
		String cmd = "rm /tmp/cctv_"+windowId;
		System.out.println(cmd);
		try {
            Process process = 
                new ProcessBuilder(new String[] {"bash","-c", cmd})
                    .redirectErrorStream(true)
                    .start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                String line = null;
                while ( (line = br.readLine()) != null ) {
                	System.out.println(line);
                }
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
}
