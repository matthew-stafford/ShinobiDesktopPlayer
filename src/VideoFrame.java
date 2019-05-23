import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class VideoFrame extends JInternalFrame {

	public String windowId;
	public VideoFrameCanvas videoCanvas;
	public JPanel transparentPanel;
	public ShinobiMonitor monitor;
	//private VideoPlayerStream player;
	private VideoPlaybackController controller;
	private MPVManager mpv;
	
	public VideoFrame(ShinobiMonitor monitor) {
		super();
		
		this.monitor = monitor;
				
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        ((BasicInternalFrameUI) getUI()).setNorthPane(null);
        setBorder(null);
        setResizable(true);    
        setBounds(0,0,640,320);

        getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
        		
		// create canvas
        videoCanvas = new VideoFrameCanvas();
		videoCanvas.setSize(getWidth(),getHeight());	
		videoCanvas.setIgnoreRepaint(false);
		transparentPanel = new JPanel();
		transparentPanel.setSize(getWidth(),getHeight());
		transparentPanel.setBackground(new Color(0,0,0,0));
		transparentPanel.setOpaque(false);
		transparentPanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {			}
			
			@Override
			public void mousePressed(MouseEvent e) {			}
			
			@Override
			public void mouseExited(MouseEvent e) {			}
			
			@Override
			public void mouseEntered(MouseEvent e) {			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
		        	e.consume();
				
		        	System.out.println("Double click detected!");
				}
			}
		});
		add(videoCanvas);
		add(transparentPanel);
		
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

		transparentPanel.setSize(width,height);	
		videoCanvas.setSize(width,height);	
		
	}
	
	public void playStream() {
		if (mpv == null) {
			if (windowId != null && monitor != null && monitor.stream != null) {
				mpv = new MPVManager("mpv --no-cache --volume 0 --keep-open --profile=low-latency -wid "+windowId+" "+monitor.stream);
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
	
	public void PlayerCleanup() {
		if (controller != null) {
			controller.cleanup();
		}
	}

	public void playVideoPlayback(Date time, boolean removeable) {
		if (mpv == null) {
			if (windowId != null && monitor != null) {
				// first open
				monitor.LoadVideos(time);
				
				int videoIndex = monitor.getVideoFileIndex(time);
				
				String video = monitor.getVideoFilename(time, videoIndex);
				if (video != null) {
					int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);
					
					
					// ipc-server for controlling
					// cache-secs dont want mpv to buffer too much otherwise network overhead will make it unstable with multiple videos since it will try to load entire files as fast as possible
					mpv = new MPVManager("mpv --input-ipc-server=/tmp/cctv_"+windowId+" --cache-secs=10 --profile=low-latency --start=+"+seekPos+" -wid "+windowId+" "+monitor.host+"/"+monitor.api_key+"/videos/"+monitor.group_key+"/"+monitor.mid+"/"+video);
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
	
	
	private void writeNoVideoOnCanvas(VideoFrameCanvas c) {
		System.out.println("no video");
		c._status = c._status.NoPlaybackVideo;
		c.repaint();
	}
	
}