import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class VideoFrame extends JPanel implements Runnable {

	public String windowId;
	public VideoFrameCanvas videoCanvas;
	public ShinobiMonitor monitor;
	private MPVManager mpv;
	private ArrayList<String> videoPlaylist;
	private boolean overlay = true;
	
	
	private JButton btnFull, btnRemove, btnMute, btnEvents, btnDownload, btnPlay;
	private JTextPane txtName;
	private JPanel overlayPanel = new JPanel();
	private boolean paused = false;
	private boolean audio = false;
	
	private int buttonSize = 34;
	
	private VideoLayout vl;
	
	public VideoFrame(ShinobiMonitor monitor, VideoLayout vl) {
		super();
		
		this.monitor = monitor;				
		this.vl = vl;

		btnFull = new JButton();
		btnFull.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-widescreen-32.png")));
		btnFull.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				vl.fullScreen(monitor);
				
			}
		});
		
		txtName = new JTextPane();
		txtName.setText(monitor.name);
		txtName.setBackground(Color.black);
		txtName.setForeground(Color.white);
		// center align text pane text
		StyledDocument doc = txtName.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);

		
		btnPlay = new JButton();
		btnPlay.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-pause-32.png")));
		btnPlay.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (paused) {
					resumeStream();
				} else {
					pauseStream();
				}
			}
		});
		
		btnRemove = new JButton();
		btnRemove.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-delete-32.png")));
		btnRemove.setBounds(getWidth()-101,1,buttonSize,buttonSize);
		btnRemove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				vl.removeVideoStream(monitor);
			}
		});
		
		btnMute = new JButton();
		btnMute.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-mute-32.png")));
		btnMute.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(audio) {
					setVolume(0);
				} else {
					setVolume(PlayerUI.GLOBAL_VOLUME);
				}
			}
		});
		
		
		
		if (PlayerUI.PLAY_MODE == PlayerUI.PlayMode.Playback) {
			btnDownload = new JButton();
			btnDownload.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-download-from-cloud-32.png")));
			btnDownload.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					downloadCurrentFile();
				}
			});
			
			btnEvents = new JButton();
			btnEvents.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-motion-detector-32.png")));
			btnEvents.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					new EventsUI(vl.playerUI, monitor.mid).setVisible(true);
				}
			});
		}
		overlayPanel.setBackground(new Color(5,5,5));
		overlayPanel.add(btnFull);
		overlayPanel.add(btnRemove);
		overlayPanel.add(btnMute);
		if (PlayerUI.PLAY_MODE == PlayerUI.PlayMode.Playback) {
			overlayPanel.add(btnEvents);
			overlayPanel.add(btnDownload);
		}
		overlayPanel.add(btnPlay);
		overlayPanel.add(txtName);
		
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
       	
        setBorder(null);
        setBounds(0,0,640,320);
        
		// create canvas
        videoCanvas = new VideoFrameCanvas(monitor.mid, monitor.name);
		videoCanvas.setSize(getWidth(),getHeight());	
		videoCanvas.setIgnoreRepaint(false);
        		
		
        setVisible(true);
        
      
		add(videoCanvas);
		
		add(overlayPanel);
		
		overlayPanel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				if (arg0.getY() < 0) {
					// mouse moved up to canvas, dont hide
					//hideOverlay();
				} else if (arg0.getX() >= getWidth()) {
					hideOverlay();
				} else if (arg0.getX() <= 0) {
					hideOverlay();
				} else if (arg0.getY() >= buttonSize) {
					System.out.println("Mouse exit="+arg0.getY());
					hideOverlay();
				}
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				resizeVideoFrame();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				resizeVideoFrame();
				
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				resizeVideoFrame();
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
	}
	
	protected void showOverlay() {
		overlay = true;
		resizeVideoFrame();
	}
	
	public void hideOverlay() {
		overlay = false;
		resizeVideoFrame();
	}

	// record previous values to prevent doing this too often, reduce cpu usage
	int previousWidth=-1, previousHeight=-1;
	boolean previousOverlay = overlay;
	
	public void resizeVideoFrame() {
		if (getWidth() != previousWidth || getHeight() != previousHeight || previousOverlay != overlay) {
			
			System.out.println("Resize w="+getWidth()+" h="+getHeight()+" overlay="+overlay+" name="+monitor.name);
			previousWidth = getWidth();
			previousHeight = getHeight();
			previousOverlay = overlay;
			
			setSize(getWidth(), getHeight());
			setBorder(null);
			setLayout(null);
			
			videoCanvas.setBounds(0,0,getWidth(),(overlay?getHeight()-(buttonSize+5):getHeight()));
			videoCanvas.setSize(getWidth(),(overlay?getHeight()-(buttonSize+5):getHeight()));
			videoCanvas.setPreferredSize(new Dimension(getWidth(),(overlay?getHeight()-(buttonSize+5):getHeight())));
			
			overlayPanel.setBorder(null);
			overlayPanel.setLayout(null);
			overlayPanel.setBounds(0,(overlay? getHeight()-(buttonSize+5): 0), (overlay? getWidth() : 0),(overlay ? (buttonSize+5) : 0));
			overlayPanel.setPreferredSize(new Dimension(getWidth(), (overlay ? (buttonSize+5) : 0) ));
			
			
			btnPlay.setBounds((buttonSize*0)+5,1,buttonSize,buttonSize);
			btnPlay.setPreferredSize(new Dimension(buttonSize,buttonSize));
			
			btnMute.setBounds((buttonSize*1)+5,1,buttonSize,buttonSize);
			btnMute.setPreferredSize(new Dimension(buttonSize,buttonSize));
			if (PlayerUI.PLAY_MODE == PlayerUI.PlayMode.Playback) {	

				btnEvents.setBounds((buttonSize*2)+5,1,buttonSize,buttonSize);
				btnEvents.setPreferredSize(new Dimension(buttonSize,buttonSize));
				
				btnDownload.setBounds((buttonSize*3)+5,1,buttonSize,buttonSize);
				btnDownload.setPreferredSize(new Dimension(buttonSize,buttonSize));			
				
				txtName.setBounds((buttonSize*4)+5,6,getWidth()-((buttonSize*2)+10), buttonSize);
			} else {
				txtName.setBounds((buttonSize*2)+10,6,getWidth()-((buttonSize*4)+10), buttonSize);
			}
			
			btnFull.setBounds(getWidth()-(buttonSize*1)-5,1,buttonSize,buttonSize);
			btnFull.setPreferredSize(new Dimension(buttonSize,buttonSize));
			
			btnRemove.setBounds(getWidth()-(buttonSize*2)-5,1,buttonSize,buttonSize);
			btnRemove.setPreferredSize(new Dimension(buttonSize,buttonSize));

			videoCanvas.repaint();
			videoCanvas.revalidate();
		}
	}
	
	/**
	 * --demuxer-readahead-secs=0.1 && --cache-secs=0.1 (fixes some streams from having issues where they freeze and then speed up once cache is filled)
	 * 
	 */
	public void playStream() {
		if (mpv == null) {
			if (windowId != null && monitor != null && monitor.stream != null) {
				mpv = new MPVManager("mpv --no-osc --profile=low-latency --cache-secs=0.1 --demuxer-readahead-secs=0.1 --input-ipc-server=/tmp/cctv_"+windowId+" --speed=1.01 --no-cache  --volume 0 -wid "+windowId+" "+monitor.stream);
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
			btnPlay.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-play-32.png")));
			paused = true;
		}
	}
	
	public void resumeStream() {
		if (mpv != null) {
			mpv.sendCommand("echo '{\"command\": [\"set_property\",\"pause\",false]}' | socat - /tmp/cctv_"+windowId);
			btnPlay.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-pause-32.png")));
			paused = false;
		}
	}
	
	public void downloadCurrentFile() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
		    	String videoBeingPlayed = mpv.getValueFromResult(mpv.sendCommand("echo '{ \"command\": [\"get_property\", \"filename\"] }' | socat - /tmp/cctv_"+windowId),"data");
				System.out.println("video="+videoBeingPlayed);
				Desktop.getDesktop().browse(new URI(monitor.site.getBaseURL()+"/"+monitor.site.apiKey+"/videos/"+monitor.site.groupKey+"/"+monitor.mid+"/"+videoBeingPlayed));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setSpeed(double speed) {
		if (mpv != null) {
			mpv.sendCommand("echo '{\"command\": [\"set_property\",\"speed\","+speed+"]}' | socat - /tmp/cctv_"+windowId);
		}
	}
	
	public void setVolume(int volume) {
		if (mpv != null) {
			if (volume == 0) {
				audio = false;
				btnMute.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-mute-32.png")));
			} else {
				audio = true;
				btnMute.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-audio-32.png")));
			}
			System.out.println("Setting volume to "+volume);
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

					videoCanvas._status = videoCanvas._status.Loading;
					videoCanvas.repaint();
					
					int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);					
					
					// ipc-server for controlling
					// cache-secs dont want mpv to buffer too much otherwise network overhead will make it unstable with multiple videos since it will try to load entire files as fast as possible
					//mpv = new MPVManager("mpv --idle=yes --keep-open=no --reset-on-next-file=all --prefetch-playlist=yes --input-ipc-server=/tmp/cctv_"+windowId+" --cache-secs=15 --profile=low-latency --wid "+windowId+" "+monitor.host+"/"+monitor.api_key+"/videos/"+monitor.group_key+"/"+monitor.mid+"/"+video);
					mpv = new MPVManager("mpv --idle=yes --keep-open=no --prefetch-playlist=yes --input-ipc-server=/tmp/cctv_"+windowId+" --cache-secs=15  --start="+seekPos+" --profile=low-latency --wid "+windowId+" "+monitor.site.getBaseURL()+"/"+monitor.site.apiKey+"/videos/"+monitor.site.groupKey+"/"+monitor.mid+"/"+video);
					
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
							if (loops > 5) {
								videoCanvas._status = videoCanvas._status.ErrorMpv;
								videoCanvas.repaint();
								break;
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					System.out.println("Loops="+loops);
					
					
					// add rest of playlist to mpv
					addFilesToPlaylist(videoPlaylist);				
					
					// resume any settings set to file such as speed/volume/etc
					
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

					videoCanvas._status = videoCanvas._status.Loading;
					videoCanvas.repaint();
					
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
						String url = monitor.site.getBaseURL()+"/"+monitor.site.apiKey+"/videos/"+monitor.site.groupKey+"/"+monitor.mid+"/"+video;
						int playlistIndex = getPlaylistIndex(video);
						
						int seekPos = monitor.getVideoFileSeekPos(time, videoIndex);
						
						mpv.kill();
						mpv = new MPVManager("mpv --idle=yes --keep-open=no --prefetch-playlist=yes --input-ipc-server=/tmp/cctv_"+windowId+" --cache-secs=15  --start="+seekPos+" --profile=low-latency --wid "+windowId+" "+monitor.site.getBaseURL()+"/"+monitor.site.apiKey+"/videos/"+monitor.site.groupKey+"/"+monitor.mid+"/"+video);
						
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
								loops++;
								if (loops > 5) {
									videoCanvas._status = videoCanvas._status.ErrorMpv;
									videoCanvas.repaint();
									
									break;

								}
							} catch (InterruptedException e) {
							}
						}
												
						// add rest of playlist to mpv
						addFilesToPlaylist(videoPlaylist);						
						
						// re-apply any settings that are set
						// if video frame has audio OR global audio is set to true
						
						if (audio || vl.playerUI.audio) {
							setVolume(PlayerUI.GLOBAL_VOLUME);
						}
						// playback speed
						if (vl.playerUI.speed) {
							setSpeed(vl.playerUI.getSpeed());
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
			String file = monitor.site.getBaseURL()+url;
			if (playlistData.contains(file)) {
				System.out.println("playlist already contains url skipping="+file);
			} else {
				//String ipcCommand = "echo '{ \"command\": [\"loadfile\", \""+file+"\", \"append\"] }' | socat - /tmp/cctv_"+windowId;
				String ipcCommand = "echo '{ \"command\": [\"loadfile\", \""+file+"\", \"append\", \"start=0\"] }' | socat - /tmp/cctv_"+windowId;
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