import java.awt.Color;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class VideoFrame extends JInternalFrame implements Runnable {

	public String windowId;
	public VideoFrameCanvas videoCanvas;
	public ShinobiMonitor monitor;
	private MPVManager mpv;
	private ArrayList<String> videoPlaylist;
	
	
	public VideoFrame(ShinobiMonitor monitor) {
		super();
		
		this.monitor = monitor;
				
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        ((BasicInternalFrameUI) getUI()).setNorthPane(null);
        setBorder(null);
        setBounds(0,0,640,320);
        
        getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

		// create canvas
        videoCanvas = new VideoFrameCanvas();
		videoCanvas.setSize(getWidth(),getHeight());	
		videoCanvas.setIgnoreRepaint(false);
        		
		add(videoCanvas);
		
        setVisible(true);
        
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				reSize(e.getComponent().getWidth(),e.getComponent().getHeight());
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				reSize(e.getComponent().getWidth(),e.getComponent().getHeight());
				
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				reSize(e.getComponent().getWidth(),e.getComponent().getHeight());
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
	}
	
	public void reSize(int width, int height) {
		videoCanvas.setSize(width,height);			
	}
	
	public void playStream() {
		if (mpv == null) {
			if (windowId != null && monitor != null && monitor.stream != null) {
				mpv = new MPVManager("mpv --no-osc --input-ipc-server=/tmp/cctv_"+windowId+" --profile=low-latency --speed=1.01 --cache-secs=10  --volume 0 -wid "+windowId+" "+monitor.stream);
				mpv.Start();
			} else {
				System.out.println("Null found on WindowId="+windowId+", monitor.stream="+monitor.stream);
			}
		} else {			
			stopStream();
		}
		
		
	}
	
	public void stopStream() {
		if (mpv != null) {
			mpv.kill();
			monitoring = false;
		}
	}
	
	public void pauseStream() {
		if (mpv != null) {
			mpv.sendCommand("echo '{\"command\": [\"set_property\",\"pause\",true]}' | socat - /tmp/cctv_"+windowId);
		}
	}
	
	public void resumeStream() {
		if (mpv != null) {
			mpv.sendCommand("echo '{\"command\": [\"set_property\",\"pause\",false]}' | socat - /tmp/cctv_"+windowId);
		}
	}
	
	public void setSpeed(double speed) {
		if (mpv != null) {
			mpv.sendCommand("echo '{\"command\": [\"set_property\",\"speed\","+speed+"]}' | socat - /tmp/cctv_"+windowId);
		}
	}
	
	public void setVolume(int volume) {
		if (mpv != null) {
			mpv.sendCommand("echo '{\"command\": [\"set_property\",\"volume\","+volume+"]}' | socat - /tmp/cctv_"+windowId);
		}
	}
	
	/**
	 * Generate a playlist file with one URL per line for all the video files in the day
	 *	this is only used for the initial mpv load, afterwards playlist is controlled using IPC
	 *
	 * --playlist-start=index, start playing playlist at index which starts from 0
	 * --playlist=filename, the path to the playlist file /tmp/cctv_playlist_windowID 
	 * -
	*/
		

	
	/**
	 * Seek within the current video or prev/next playlist file 
	 * @param pos the amount of seconds to seek e.g. +30 -30 +10 -10 +5 -5
	 */
	public void seek(int pos) {
		if (mpv != null) {
			// get current position
			String currentPos = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"time-pos\"] }' | socat - /tmp/cctv_"+windowId),"data");
			if (currentPos != null && currentPos.length() > 0 ) {				
				// is seek position in this file? or next? or previous
				
				// if (previous)
				
				// if (next)
				
				// seek
				mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"set_property\", \"time-pos\", "+(Double.parseDouble(currentPos)+pos)+"] }' | socat - /tmp/cctv_"+windowId),"data");
			}
		}
	}
	
	
	/**
	 * mpv parameters
	 * --idle=yes stops mpv closing once playback has finished
	 * --prefetch-playlist=yes mpv will start buffering the next file in the playlist once the current video has fully loaded so mpv can seamlessly switch to next video
	 * --input-ipc-server=file is the JSON IPC file for mpv (so we can control it)
	 * --cache-secs=X is the amount of seconds mpv will buffer (better not use too large otherwise will use a lot of bandwidth and prevent simultaneous loading of videos)
	 * --profile=low-latency mpv built-in optimizations for fast playback
	 * --start=X the amount of seconds to seek ahead in the video i.e. 90 will start playback at 1:30.
	 * --wid the windowId of the canvas object which mpv will write the video output to
	 * 
	 * @param time
	 * @param removeable
	 */
	public void playVideoPlayback(Date time, boolean removeable) {
		if (mpv == null) {
			if (windowId != null && monitor != null) {
				// first open
				monitor.LoadVideos(time);	
				
				// generate initial playlist
				videoPlaylist = monitor.generatePlaylist();
				Collections.sort(videoPlaylist);
				
				int videoIndex = monitor.getVideoFileIndex(time);				
				String video = monitor.getVideoFilename(time, videoIndex);
				
				if (video != null) {
					int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);					
					
					// ipc-server for controlling
					// cache-secs dont want mpv to buffer too much otherwise network overhead will make it unstable with multiple videos since it will try to load entire files as fast as possible -start=+"+seekPos+" 
					mpv = new MPVManager("mpv --idle=yes --keep-open=no --reset-on-next-file=all --prefetch-playlist=yes --input-ipc-server=/tmp/cctv_"+windowId+" --cache-secs=15 --profile=low-latency --wid "+windowId+" "+monitor.host+"/"+monitor.api_key+"/videos/"+monitor.group_key+"/"+monitor.mid+"/"+video);
					mpv.Start();
					
					// give mpv a chance to load
					int loops = 0;
					while (true) {
						try {
							loops++;
							ArrayList<String> r = mpv.getPlaylist(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"playlist\"] }' | socat - /tmp/cctv_"+windowId));
							if (r != null && r.size() > 0) {
								break;
							}
							Thread.sleep(150);
						} catch (InterruptedException e) {
						}
					}
					
					System.out.println("Loops="+loops);
					
					
					// add rest of playlist to mpv
					addFilesToPlaylist(videoPlaylist);						
					
					// reset seek position of file  --start has a bug where each subsequent file is opened at (i.e. 15:55pm 16:55pm 17:55pm etc)
					loops = 0;
					while (true) {
						try {
							String result = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"set_property\", \"time-pos\", "+seekPos+"] }' | socat - /tmp/cctv_"+windowId),"error");
							if (result.equalsIgnoreCase("success")) {
								break;
							}
							Thread.sleep(100);
						} catch (Exception e) {
							
						}
					}
					//mpv.sendCommand("echo '{ \"command\": [\"set_property\", \"start\", \"none\"] }' | socat - /tmp/cctv_"+windowId);
					System.out.println("Sent start cmd");
					
					// monitor, time, windowId
				} else {
					// video unknown
					videoCanvas._status = videoCanvas._status.NoPlaybackVideo;
					videoCanvas.repaint();
				}
				
			}
		} else {			
			if (removeable) {
				// remove
				mpv.kill();
			} else {
				System.out.println("ATTEMPTING IPC SEEK");
				// seek to new video OR different time in current video
				monitor.LoadVideos(time);
				ArrayList<String> list = monitor.generatePlaylist();
				
				addFilesToPlaylist(list);
				
				int videoIndex = monitor.getVideoFileIndex(time);
				
				if (videoIndex != -1) {
					String video = monitor.getVideoFilename(time, videoIndex);
					String videoBeingPlayed = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"filename\"] }' | socat - /tmp/cctv_"+windowId),"data");
					if (video.equalsIgnoreCase(videoBeingPlayed)) {
						// seek within current file
						System.out.println("Video file matches current playback, requesting seek");
						int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);
						String result = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"set_property\", \"time-pos\", "+seekPos+"] }' | socat - /tmp/cctv_"+windowId),"data");
						System.out.println("result="+result);
					} else {
						// play new file 
						System.out.println("Video file is different, requesting playlist pos move + seek");
						String url = monitor.host+"/"+monitor.api_key+"/videos/"+monitor.group_key+"/"+monitor.mid+"/"+video;
						System.out.println("Loading URL: "+url);
						int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);
						System.out.println("Video="+video+" seekPos="+seekPos+" videoIndex="+videoIndex);
						int playlistIndex = getPlaylistIndex(video);
						String result = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"set_property\", \"playlist-pos\", "+playlistIndex+"] }' | socat - /tmp/cctv_"+windowId),"data");
						System.out.println("result="+result);
						while (true) {
							try {
								result = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"set_property\", \"time-pos\", "+seekPos+"] }' | socat - /tmp/cctv_"+windowId),"error");
								if (result.equalsIgnoreCase("success")) {
									break;
								}
								Thread.sleep(100);
							} catch (Exception e) {
								
							}
						}
					}
				} else {
					videoCanvas._status = videoCanvas._status.NoPlaybackVideo;
					videoCanvas.repaint();
				}
			}
		}		
	}
	
	
	private int getPlaylistIndex(String video) {
		ArrayList<String> playlistData = mpv.getPlaylist(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"playlist\"] }' | socat - /tmp/cctv_"+windowId));
		int i = 0;
		for (String s : playlistData) {
			String file = s.substring(s.lastIndexOf("/")+1, s.length());
			System.out.println("File="+file);
			if (file.equals(video)) {
				System.out.println("Index found @ "+i);
				return i;
			}
			i++;
		}
		return 0;
	}

	public void addFilesToPlaylist(ArrayList<String> files) {
		ArrayList<String> playlistData = mpv.getPlaylist(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"playlist\"] }' | socat - /tmp/cctv_"+windowId));
		
		// add all urls which are not already in playlistData
		for (String url : videoPlaylist) {
			String file = monitor.host+url;
			if (playlistData.contains(file)) {
				System.out.println("playlist already contains url skipping="+file);
			} else {
				String ipcCommand = "echo '{ \"command\": [\"loadfile\", \""+file+"\", \"append\"] }' | socat - /tmp/cctv_"+windowId;
				System.out.println(ipcCommand);
				mpv.sendCommand(ipcCommand);
			}
		}
		
		// get new playlist data and sort it
		sortPlaylist();
	}

	
	private void sortPlaylist() {
		ArrayList<String> playlistData = mpv.getPlaylist(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"playlist\"] }' | socat - /tmp/cctv_"+windowId));
		String[] playlist = playlistData.toArray(new String[0]);
		
		int n = playlist.length; 
        for (int i = 1; i < n; ++i) { 
            String key = playlist[i]; 
            int j = i - 1; 
  
            /* Move elements of arr[0..i-1], that are 
               greater than key, to one position ahead 
               of their current position */
            while (j >= 0 && playlist[j].compareTo(key) > 0) { 
                playlist[j + 1] = playlist[j]; 
                mpv.sendCommand("echo '{ \"command\": [\"playlist-move\","+j+","+(j+2)+"] }' | socat - /tmp/cctv_"+windowId);
                
                j = j - 1; 
            } 
            playlist[j + 1] = key; 
        } 
		
		for (String s : playlist) {
			System.out.println("Sorted="+s);
		}
		
		ArrayList<String> actualData = mpv.getPlaylist(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"playlist\"] }' | socat - /tmp/cctv_"+windowId));
		for (String s : actualData) {
			System.out.println("Actual="+s);
		}
		
	}
	
	private boolean monitoring = true;
	@Override
	public void run() {
		while (monitoring) {
			if (mpv != null) {
				// check date of current file playing
				// if videos are not loaded for day before or after, load them
				String videoBeingPlayed = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"filename\"] }' | socat - /tmp/cctv_"+windowId),"data");
				// video files are named in the format YYYY-mm-ddThh:mm:ss
				String date = videoBeingPlayed.substring(0, videoBeingPlayed.indexOf('T'));
				System.out.println("[MonitorThread] VideoBeingPlayed="+videoBeingPlayed+", date="+date);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}

	
	
}