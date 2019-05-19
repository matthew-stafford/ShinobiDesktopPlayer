import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class VideoFrame extends JInternalFrame {

	public String windowId;
	public Canvas videoCanvas;
	public JPanel transparentPanel;
	public ShinobiMonitor monitor;
	private VideoPlayerStream player;
	private VideoPlaybackController controller;
	
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
        videoCanvas = new Canvas();
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
				// TODO Auto-generated method stub
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				reSize(getWidth(),getHeight());
				
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
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
		if (player == null) {
			if (windowId != null && monitor != null && monitor.stream != null) {
				player = new VideoPlayerStream(windowId, monitor.stream);
				player.play();
			} else {
				System.out.println("Null found on WindowId="+windowId+", monitor.stream="+monitor.stream);
			}
		} else {
			
			player.stop();
		}
		
		
	}
	
	public void stopStream() {
		if (player != null) {
			player.stop();
		}
	}
	
	public void PlayerCleanup() {
		if (controller != null) {
			controller.cleanup();
		}
	}

	public void playVideoPlayback(Date time) {
		if (controller == null) {
			if (windowId != null && monitor != null) {
				controller = new VideoPlaybackController(monitor, time, windowId);
				controller.play();
			}
		} else {			
			controller.cleanup();
		}		
	}
	
}
