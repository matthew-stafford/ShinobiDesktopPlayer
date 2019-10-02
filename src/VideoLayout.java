import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class VideoLayout extends JPanel {

	private Layout currentLayout = Layout.LayoutGrid;
	private PlayMode playMode = PlayMode.Live;
	private ArrayList<String> windowIds = new ArrayList<String>();
	public PlayerUI playerUI;
	
	public enum Layout {
		LayoutGrid
	}
	
	public enum PlayMode {
		Live,
		Playback
	}
	
	public VideoLayout(PlayerUI playerUI) {
		this.playerUI = playerUI;
		//capture mouse enter/exit events
		long eventMask = AWTEvent.MOUSE_EVENT_MASK;

    	Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
    	{
    	    public void eventDispatched(AWTEvent e)
    	    {
    	    	if (e instanceof java.awt.event.MouseEvent) {
    	    		MouseEvent me = (MouseEvent) e;
    	    	
	    	    	// 505 = mouse_exit
	    	    	// 504 = mouse_enter
	    	    	if (e.getID() == 505) {
	    	    		// mouse exit
	    	    		if (e.getSource() instanceof VideoFrameCanvas) {
	    	    			VideoFrameCanvas vfc = (VideoFrameCanvas) e.getSource();
		    	    		for (Component c : getComponents() ) {
		    	    			if (c instanceof VideoFrame) {	    	    				
		    	    				VideoFrame f = (VideoFrame) c;
		    	    				if (f.getComponentAt(me.getX(), me.getY()) == null || !SwingUtilities.isDescendingFrom((Component)e.getSource(), f)) {		    	    				
			    	    				if (f.monitor.mid.equals(vfc.mid)) {
			    	    					f.hideOverlay();			    	    					
			    	    				}
		    	    				}
		    	    			}
		    	    		}
	    	    		}
	    	    	} else if (e.getID() == 504) {
	    	    		// mouse enter
	    	    		if (e.getSource() instanceof VideoFrameCanvas) {
	    	    			VideoFrameCanvas vfc = (VideoFrameCanvas) e.getSource();
		    	    		for (Component c : getComponents() ) {
		    	    			if (c instanceof VideoFrame) {	    	    				
		    	    				VideoFrame f = (VideoFrame) c;
		    	    				if (f.monitor.mid.equals(vfc.mid)) {
		    	    					f.showOverlay();		    	    					
		    	    				}
		    	    			}
		    	    		}
	    	    		}
	    	    	}
    	    	}
    	    }
    	}, eventMask);
    	
    	
    	
		
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				updateLayout();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				updateLayout();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				System.out.println("component moved");
				updateLayout();
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				
			}
		});
		
	}
	
	private void updateLayout() {
    	if (isFullScreen) {
    		// if full screen, get component with a size > 0 and set to full size of panel
    		for (Component c: getComponents()) {
    			if (c.getSize().width > 0 && c.getSize().height > 0) {
    				c.setBounds(0,0,getWidth(), getHeight());
    				break;
    			}
    		}
    		return;
    	}
		if (currentLayout == Layout.LayoutGrid) {
			int numberOfComponents = getComponents().length;

			Dimension element_size = calculateSizeForGridLayout(numberOfComponents);
			Dimension grid_size = calculateGridLayoutRowsAndColumns(numberOfComponents);
			
			int component = 0;
			for (Component c : getComponents()) {
				if (c instanceof VideoFrame) {
					VideoFrame f = (VideoFrame) c;
					int x = element_size.width*calculateColumnPositionInGridLayout(component, grid_size.width, grid_size.height);
					int y = element_size.height*calculateRowPositionInGridLayout(component, grid_size.width, grid_size.height);

					System.out.println("component="+component+", x="+x+", y="+y+", w="+element_size.width+", h="+element_size.height+" rows="+grid_size.width+", cols="+grid_size.height);
					
					f.setBounds( x, y , element_size.width, element_size.height);
					component++;
				}
			}
		}
	}
	
	public void addVideoPlayback(ShinobiMonitor monitor, Date time) {
		playMode = PlayMode.Playback;
		
		if (isMonitorPlaying(monitor)) {
			// remove
			System.out.println("Monitor is already playing, removing from VideoLayout");
			removeVideoPlayback(monitor);			
			return;
		}
		
		System.out.println("Creating video frame");
		// create frame for video		
		VideoFrame frame = new VideoFrame(monitor,this);
		add(frame);
		
		// get its window Id for passing to mpv
		addWindowId(frame);
		System.out.println("Getting its windowId="+frame.windowId);
		
		System.out.println("Launching mpv");
		// load stream using mpv		
		frame.playVideoPlayback(time, true);
		// updateLayout
		updateLayout();
		
		System.out.println("Attempting to fix width/height");
		
		frame.videoCanvas.setSize(frame.getWidth(), frame.getHeight());
	}
	
	
	public void removeVideoStreams() {
		for (Component c : getComponents()) {
			if (c instanceof VideoFrame) {
				VideoFrame f = (VideoFrame) c;
				f.stopStream();
				remove(c);
			}
		}
	}
	
	public void removeVideoPlayback() {
		for (Component c : getComponents()) {
			if (c instanceof VideoFrame) {
				VideoFrame f = (VideoFrame) c;
				f.stopStream();
				remove(c);
			}
		}
	}
	
	private boolean isFullScreen = false;
	public void fullScreen(ShinobiMonitor monitor) {
		if (getMonitorComponent(monitor) == null) {
			return ;
		}
		
		if (!isFullScreen) {
			for (Component c : getComponents()) {
				if (c instanceof VideoFrame) {
					VideoFrame f = (VideoFrame) c;
					if (f.monitor.mid.equalsIgnoreCase(monitor.mid)) {
						f.setBounds(0, 0, getWidth(), getHeight());
						f.resizeVideoFrame();
						isFullScreen = true;
					} else {
						f.setSize(0,0);
					}
				}
			}
		} else {
			isFullScreen = false;
			updateLayout();
		}
	}

	public void addVideoStream(ShinobiMonitor monitor) {
		playMode = PlayMode.Live;
		if (isMonitorPlaying(monitor)) {
			// remove
			System.out.println("Monitor is already playing, removing from VideoLayout");
			removeVideoStream(monitor);		
			return;
		}
		
		System.out.println("Creating video frame");
		// create frame for video		
		VideoFrame frame = new VideoFrame(monitor, this);
		add(frame);
		
		// get its window Id for passing to mpv
		addWindowId(frame);
		System.out.println("Getting its windowId="+frame.windowId);
		
		System.out.println("Launching mpv");
		// load stream using mpv		
		frame.playStream();
		// updateLayout
		updateLayout();
		
		frame.videoCanvas.setSize(frame.getWidth(), frame.getHeight());
	}
	
	private void addWindowId(VideoFrame frame) {
		ArrayList<String> newWindowIds = getWindowIds();
		for (String newWindowId : newWindowIds) {
			if (!windowIds.contains(newWindowId)) {
				frame.windowId = newWindowId;
				this.windowIds.add(newWindowId);
				break;
			}
		}
	}
	
	public void pauseAll(boolean paused) {
		if (paused) {
			// resume play on all
			for (Component c: getComponents()) {
				if (c instanceof VideoFrame) {
					VideoFrame f = (VideoFrame) c;
					f.resumeStream();
				}
			}
		} else {
			// pause all
			for (Component c: getComponents()) {
				if (c instanceof VideoFrame) {
					VideoFrame f = (VideoFrame) c;
					f.pauseStream();
				}
			}			
		}
	}



	private boolean isMonitorPlaying(ShinobiMonitor monitor) {
		return (getMonitorComponent(monitor) == null ? false : true);
	}
	
	private Component getMonitorComponent(ShinobiMonitor monitor) {
		for (Component c : getComponents()) {
			if (c instanceof VideoFrame) {
				VideoFrame f = (VideoFrame) c;
				if (f.monitor.name.equalsIgnoreCase(monitor.name)) {
					return c;
				}
			}
		}
		return null;
	}

    
    private int calculateRowPositionInGridLayout(int componentNumber, int rows, int columns) {
    	if (componentNumber == 1 && rows == 2 && columns == 1) {
    		return 1;
    	}
    	return (componentNumber / columns);
    }
    private int calculateColumnPositionInGridLayout(int componentNumber, int rows, int columns) {
    	if (componentNumber == 1 && rows == 2 && columns == 1) {
    		return 0;
    	}
    	return (componentNumber % rows);
    }
    
    private static Dimension calculateGridLayoutRowsAndColumns(int numberOfComponents) {
    	if (numberOfComponents <= 1) {
    		return new Dimension(1,1);
    	} else if (numberOfComponents <= 2) {
    		return new Dimension(2,1);
    	} else if (numberOfComponents <= 4) {
    		return new Dimension(2,2);
    	} else if (numberOfComponents <= 9) {
    		return new Dimension(3,3);   		
    	} else if (numberOfComponents <= 16) {
    		return new Dimension(4,4);    		
    	} else if (numberOfComponents <= 25) {
    		return new Dimension(5,5);    		
    	} else if (numberOfComponents <= 36) {
    		return new Dimension(6,6);    		
    	}
		return new Dimension(1,1);    	
    }
    
    private Dimension calculateSizeForGridLayout(int numberOfComponents) {
    	Dimension size = getSize();
    	if (numberOfComponents <= 1) {
    		return size;
    	} else if (numberOfComponents <= 2) {
    		return new Dimension(size.width, size.height/2);
    	} else if (numberOfComponents <= 4) {
    		return new Dimension(size.width/2, size.height/2);
    	} else if (numberOfComponents <= 9) {
    		return new Dimension(size.width/3, size.height/3);    		
    	} else if (numberOfComponents <= 16) {
    		return new Dimension(size.width/4, size.height/4);    		
    	} else if (numberOfComponents <= 25) {
    		return new Dimension(size.width/5, size.height/5);    		
    	} else if (numberOfComponents <= 36) {
    		return new Dimension(size.width/6, size.height/6);    		
    	}
    	
    	return size;
    }
    
    public ArrayList<String> getWindowIds() {
		ArrayList<String> ids = new ArrayList<String>();	
		try {				
			
			String cmd = "xwininfo -all -name \""+PlayerUI.windowTitle+"\" | grep sun-awt-X11-XCanvasPeer | awk '{print $1}'";
			System.out.println(cmd);
		
            Process process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmd})
                    .redirectErrorStream(true)
                    .start();

            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null ) {
                ids.add(line);
            }
            
			
        } catch (Exception e) {
	         e.printStackTrace();
        }
		return ids;
	
	}

	public void removeVideoStream(ShinobiMonitor monitor) {
		VideoFrame c = (VideoFrame) getMonitorComponent(monitor);
		c.stopStream();
		remove(c);
		updateLayout();
	}

	public void removeVideoPlayback(ShinobiMonitor monitor) {
		VideoFrame c = (VideoFrame) getMonitorComponent(monitor);
		c.stopStream();
		remove(c);		
		updateLayout();
	}

	public void setVolume(int volume) {
		for (Component c: getComponents()) {
			if (c instanceof VideoFrame) {
				VideoFrame f = (VideoFrame) c;
				f.setVolume(volume);
			}
		}			
	}
	
	public void setPlaybackSpeed(double speed) {
		for (Component c: getComponents()) {
			if (c instanceof VideoFrame) {
				VideoFrame f = (VideoFrame) c;
				f.setSpeed(speed);
			}
		}			
	}

	public void seekAll(int pos) {
		for (Component c: getComponents()) {
			if (c instanceof VideoFrame) {
				VideoFrame f = (VideoFrame) c;
				f.seek(pos);
			}
		}			
	}

}