import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class VideoFrame extends JInternalFrame {

	public String windowId;
	public VideoFrameCanvas videoCanvas;
	public ShinobiMonitor monitor;
	//private VideoPlayerStream player;
	private MPVManager mpv;
	
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
				mpv = new MPVManager("mpv --input-ipc-server=/tmp/cctv_"+windowId+" --profile=low-latency --speed=1.01 --cache-secs=10  --volume 0 -wid "+windowId+" "+monitor.stream);
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
	 * --playlist-start=index, start playing playlist at index which starts from 0
	 * --playlist=filename, the path to the playlist file /tmp/cctv_playlist_windowID 
	 * -
	*/
	public void createPlaylistFile(Date date) {
		if (monitor.videos.size() > 0) {
			PrintWriter pw;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				pw = new PrintWriter(new File("/tmp/cctv_playlist_"+windowId));
				for (ShinobiVideo video : monitor.videos.get(sdf.format(date))) {
					pw.println(video.href);
				}				
				pw.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}			
		}
	}
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
				
				createPlaylistFile(time);
				
				int videoIndex = monitor.getVideoFileIndex(time);
				
				String video = monitor.getVideoFilename(time, videoIndex);
				if (video != null) {
					int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);
					
					
					// ipc-server for controlling
					// cache-secs dont want mpv to buffer too much otherwise network overhead will make it unstable with multiple videos since it will try to load entire files as fast as possible
					mpv = new MPVManager("mpv --idle=yes --prefetch-playlist=yes --input-ipc-server=/tmp/cctv_"+windowId+" --cache-secs=15 --profile=low-latency --start=+"+seekPos+" -wid "+windowId+" "+monitor.host+"/"+monitor.api_key+"/videos/"+monitor.group_key+"/"+monitor.mid+"/"+video);
					mpv.Start();
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
						System.out.println("Video file is different, requesting new video + seek");
						String url = monitor.host+"/"+monitor.api_key+"/videos/"+monitor.group_key+"/"+monitor.mid+"/"+video;
						System.out.println("Loading URL: "+url);
						int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);
						String result = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"loadfile\", \""+url+"\", \"replace\", \"start="+seekPos+"\"] }' | socat - /tmp/cctv_"+windowId),"data");
						System.out.println("result="+result);
						
					}
				} else {
					videoCanvas._status = videoCanvas._status.NoPlaybackVideo;
					videoCanvas.repaint();
				}
			}
		}		
	}

	
	
}