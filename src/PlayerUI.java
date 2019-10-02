import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DateFormatter;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTree;



/**
 * @author Matthew Stafford
 * 
 */
public class PlayerUI extends javax.swing.JFrame {

	public ShinobiAPI api;
	
	public static HashMap<String, VideoFrame> videoFrames = new HashMap<String, VideoFrame>();
	public static Dimension full_screen_size = new Dimension();
	public static PlayMode PLAY_MODE = PlayMode.Live;
	public static String host;
	public static String apiKey;
	public static String windowTitle = "Shinobi Desktop Player";
	public static String groupKey;


	public ArrayList<ShinobiSite> sites = new ArrayList<ShinobiSite>();
	
	public static int GLOBAL_VOLUME = 100;
	
	public boolean audio = false;
	public boolean speed = false;
	public boolean paused = false;

	private JTree tree;
	
    public PlayerUI() {
    	
    	// load sites
    	this.windowTitle = generateWindowTitle();
    	
    	addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				for (Component c : videoLayout.getComponents()) {
					if (c instanceof VideoFrame) {
						VideoFrame f = (VideoFrame) c;
						f.stopStream();
					}
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				
			}
		});
    	
        jPanel1 = new javax.swing.JPanel();
        btnLive = new javax.swing.JButton();
        btnLive.setToolTipText("Live");
        btnLive.setEnabled(false);
        btnLive.setIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/icons8-private-wall-mount-camera-32.png")));
        btnLive.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent arg0) {
        		updatePlayMode(PlayMode.Live);
        	}
        });
        btnPlayback = new javax.swing.JButton();
        btnPlayback.setToolTipText("Playback");
        btnPlayback.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-video-playlist-32.png")));
        btnPlayback.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent e) {        		
        		updatePlayMode(PlayMode.Playback);
        	}
        });
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        
        
        videoLayout = new VideoLayout(this);
        videoLayout.setOpaque(false);
        videoLayout.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
        videoLayout.setBackground(new Color(0,0,0,0));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        setTitle(windowTitle);

        jPanel1.setBackground(UIManager.getColor("Button.background"));

        jLabel1.setText("Playback Mode");

        jLabel3.setText("Sites / Monitors");
        
        btnSettings = new JButton("");
        btnSettings.setEnabled(true);
        btnSettings.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-settings-32.png")));
        
        spinnerPlaybackTime = new JSpinner(new SpinnerDateModel() );
        spinnerPlaybackTime.setEnabled(false);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinnerPlaybackTime, "HH:mm:ss");
        DateFormatter formatter = (DateFormatter)timeEditor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false);
        formatter.setOverwriteMode(true);
        spinnerPlaybackTime.setEditor(timeEditor);
        spinnerPlaybackTime.setValue(new Date());
        
        cboDate = new JComboBox<String>();
        cboDate.setEnabled(false);
        
        btnSeek = new JButton("");
        btnSeek.setEnabled(false);
        btnSeek.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-play-32.png")));
        btnSeek.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		System.out.println(getDateFromSpinnerAndComboBox());
        		for (Component c : videoLayout.getComponents()) {
        			if (c instanceof VideoFrame) {
        				((VideoFrame) c).playVideoPlayback(getDateFromSpinnerAndComboBox(), false);
        			}
        		}
        	}
        });
        
        JScrollPane scrollPane = new JScrollPane();
        
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        				.addComponent(jLabel1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addGap(3)
        					.addComponent(btnLive, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnPlayback, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addGap(72)
        					.addComponent(btnSettings, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap())
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(spinnerPlaybackTime, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnSeek)
        					.addContainerGap())
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(cboDate, 0, 249, Short.MAX_VALUE)
        					.addContainerGap())
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
        					.addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING, false)
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addGap(22)
        					.addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING, false)
        						.addComponent(btnPlayback, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
        						.addComponent(btnLive, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addComponent(jLabel1)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnSettings, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 766, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(cboDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING, false)
        				.addComponent(spinnerPlaybackTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(btnSeek, GroupLayout.PREFERRED_SIZE, 40, Short.MAX_VALUE))
        			.addContainerGap())
        );
        
        tree = new JTree();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(false);
        
        tree.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
		        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		        if (selPath != null && selPath.getPathCount() == 3) {
			        if(selRow != -1) {
			            if(e.getClickCount() == 1) {
			                //mySingleClick(selRow, selPath);
			            }
			            else if(e.getClickCount() == 2) {
			            	// get monitor 
			 			    ShinobiMonitor monitor = getSelectedMonitorFromTree();			 			    
			 			    // and add to layout
			 			    addSelectedVideoToLayout(monitor);
			            }
			        }
		        }
				
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}
		});
        tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
			    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

			    if (node == null)
			    //Nothing is selected.  
			    return;

			    Object nodeInfo = node.getUserObject();

			    System.out.println("Hello "+nodeInfo);
			}
		});
        scrollPane.setViewportView(tree);
        
        
        jPanel1.setLayout(jPanel1Layout);
    
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
        Calendar dates = Calendar.getInstance();
        for (int i = 0 ; i < 100; i++) {
        	cboDate.addItem(sdf.format(dates.getTime()));
        	dates.add(Calendar.DATE, -1);
        }
        
        
        btnPause = new JToggleButton("");
        btnPause.setEnabled(false);
        btnPause.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-pause-32.png")));
        btnPause.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		videoLayout.pauseAll(paused);
        		if (paused) {
        			btnPause.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-pause-32.png")));			
        		} else {
        			btnPause.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-play-32.png")));
        		}
        		paused = !paused;
        	}
        });
        
        btnVolume = new JButton("");
        btnVolume.setEnabled(false);
        btnVolume.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		GLOBAL_VOLUME = (Integer) spinnerVolume.getValue();
        		if (audio == false) {
        			audio = true;
        			
        			// update button
        			updateVolumeButton();
        			videoLayout.setVolume((Integer) spinnerVolume.getValue());
        			
        		} else if (audio == true) {
        			audio = false;     
        			
        			// update button
        			updateVolumeButton();        			
        			videoLayout.setVolume(0);
        		}
        	}
        });
        btnVolume.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-audio-32.png")));
        
        btnSpeed = new JToggleButton("");
        btnSpeed.setEnabled(false);
        btnSpeed.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-speed-32.png")));
        btnSpeed.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (speed == true) {
					speed = false;
					// reset speed to 1x
					videoLayout.setPlaybackSpeed(1.01);
				} else {
					speed = true;
					// change speed to user value
					videoLayout.setPlaybackSpeed((Double) spinnerPlaybackSpeed.getValue());
				}
				
			}
		});
        
        spinnerPlaybackSpeed = new JSpinner();
        spinnerPlaybackSpeed.setEnabled(false);
        spinnerPlaybackSpeed.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent arg0) {
        		if (speed == true) {
            		videoLayout.setPlaybackSpeed((Double) spinnerPlaybackSpeed.getValue());
				}
        	}
        });
        spinnerPlaybackSpeed.setModel(new SpinnerNumberModel(1.00,0.01,100.00,1.00));
        JSpinner.NumberEditor ne1 = new JSpinner.NumberEditor(spinnerPlaybackSpeed);
		NumberFormatter nf1 = (NumberFormatter) ne1.getTextField().getFormatter();
		final JTextField jtf1 = ((JSpinner.DefaultEditor) spinnerPlaybackSpeed.getEditor()).getTextField();
		jtf1.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyReleased(KeyEvent e) {
		        String text = jtf1.getText().replace(",", "");
		        int oldCaretPos = jtf1.getCaretPosition();
		        try {
		            Double newValue = Double.valueOf(text);
		            
		            if (newValue <= 100.00 && newValue >= 0.01) {
		            	if (speed == true) {
		            		videoLayout.setPlaybackSpeed((Double)newValue);
						}
		            }
		            jtf1.setCaretPosition(oldCaretPos);
		        } catch(NumberFormatException ex) {
		            //Not a number in text field -> do nothing
		        }
		    }
		});
		
		nf1.setAllowsInvalid(false);
		nf1.setOverwriteMode(true);
		nf1.setCommitsOnValidEdit(true);
		
        btnReplay30 = new JButton("");
        btnReplay30.setEnabled(false);
        btnReplay30.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		videoLayout.seekAll(-30);
        	}
        });
        btnReplay30.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-replay-30-32.png")));
        
        btnReplay10 = new JButton("");
        btnReplay10.setEnabled(false);
        btnReplay10.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		videoLayout.seekAll(-10);
        	}
        });
        btnReplay10.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-replay-10-32.png")));
        
        btnReplay5 = new JButton("");
        btnReplay5.setEnabled(false);
        btnReplay5.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		videoLayout.seekAll(-5);
        	}
        });
        btnReplay5.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-replay-5-32.png")));
        
        btnForward5 = new JButton("");
        btnForward5.setEnabled(false);
        btnForward5.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		videoLayout.seekAll(5);
        	}
        });
        btnForward5.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-forward-5-32.png")));
        
        btnForward10 = new JButton("");
        btnForward10.setEnabled(false);
        btnForward10.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		videoLayout.seekAll(10);
        	}
        });
        btnForward10.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-forward-10-32.png")));
        
        btnForward30 = new JButton("");
        btnForward30.setEnabled(false);
        btnForward30.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		videoLayout.seekAll(30);
        	}
        });
        btnForward30.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-forward-30-32.png")));
        
        spinnerVolume = new JSpinner();
        spinnerVolume.setEnabled(false);
		spinnerVolume.setModel(new SpinnerNumberModel(100,0,1000,1));

		JSpinner.NumberEditor ne = new JSpinner.NumberEditor(spinnerVolume);
		NumberFormatter nf = (NumberFormatter) ne.getTextField().getFormatter();
		final JTextField jtf = ((JSpinner.DefaultEditor) spinnerVolume.getEditor()).getTextField();
		jtf.addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyReleased(KeyEvent e) {
		        String text = jtf.getText().replace(",", "");
		        int oldCaretPos = jtf.getCaretPosition();
		        try {
		            Integer newValue = Integer.valueOf(text);
		            
		            if (newValue <= 1000 && newValue >= 0) {
		            	audio = true;
						updateVolumeButton();
		        		GLOBAL_VOLUME =  newValue;
						videoLayout.setVolume(GLOBAL_VOLUME);
		            }
		            jtf.setCaretPosition(oldCaretPos);
		        } catch(NumberFormatException ex) {
		            //Not a number in text field -> do nothing
		        }
		    }
		});
		
		nf.setAllowsInvalid(false);
		nf.setOverwriteMode(true);
		nf.setCommitsOnValidEdit(true);
		
		spinnerVolume.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				audio = true;
				updateVolumeButton();
				videoLayout.setVolume((Integer)spinnerVolume.getValue());
			}
		});
        
        btnDownload = new JButton("");
        btnDownload.setEnabled(false);
        btnDownload.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-download-from-cloud-32.png")));
		
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addComponent(btnReplay30, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnReplay10, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnReplay5, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnPause, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnSpeed, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addComponent(spinnerPlaybackSpeed, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnForward5, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnForward10, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnForward30, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addGap(271)
        			.addComponent(btnDownload, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnVolume, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(spinnerVolume, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
        			.addGap(6))
        );
        jPanel2Layout.setVerticalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(Alignment.TRAILING, jPanel2Layout.createParallelGroup(Alignment.LEADING)
        					.addComponent(btnReplay30, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(btnReplay10, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(btnReplay5, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(btnPause, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(btnSpeed, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(btnForward5, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(btnForward10, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(btnForward30, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(spinnerPlaybackSpeed, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        					.addComponent(spinnerVolume, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        					.addComponent(btnVolume, GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE))
        				.addComponent(btnDownload, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE))
        			.addContainerGap())
        );
        jPanel2.setLayout(jPanel2Layout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, 1007, Short.MAX_VALUE)
        				.addComponent(videoLayout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        				.addComponent(jPanel1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 956, Short.MAX_VALUE)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(videoLayout, GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)))
        			.addGap(6))
        );
        
        videoLayout.setLayout(null);
        
        btnSettings.setVisible(true);

		setDefaultLookAndFeelDecorated(true);
        
        getContentPane().setLayout(layout);
        pack();
        
        ToolTipManager.sharedInstance().setInitialDelay(0);
    }
    
    private ShinobiMonitor getSelectedMonitorFromTree() {
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    	if (node == null) {
	    	return null;
	    } else {
	    	String monitor = (String) node.getUserObject();
	    	
	    	ShinobiSite site = getSelectedSiteFromTree();
	    	if (site != null) {
	    		for (ShinobiMonitor m : site.getMonitors().values()) {
	    			if (m.name.equalsIgnoreCase(monitor)) {
	    				return m;
	    			}
	    		}
	    			
	    	}
	    }
    	return null;
    }
    
    private ShinobiSite getSelectedSiteFromTree() {
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    	
    	//Nothing is selected.  
	    if (node == null) {
	    	return null;
	    } else if (node.getParent() == null) {
	    	return null;
	    } else {
	    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
	    	for (ShinobiSite site : sites) {
	    		if (parent.getUserObject().toString().toLowerCase().equals(site.name.toLowerCase())) {
	    			return site;
	    		}
	    	}
	    }
	    return null;
    }

    private DefaultMutableTreeNode createSiteTree() {
    	
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sites");
        
    	for (ShinobiSite s : sites) {
    		DefaultMutableTreeNode site = new DefaultMutableTreeNode(s.name);
    		// add monitors for site 
    		ArrayList<String> orderedNames = new ArrayList<String>(s.getMonitors().size());
    		for (ShinobiMonitor m : s.getMonitors().values()) {
    			// alphabet sort
    			if (PLAY_MODE == PlayMode.Live) {
    				if (m.stream != null && m.stream.length() > 0) {
    					orderedNames.add(m.name);
    				}
    			} else {
    				if (m.recording) {
    					orderedNames.add(m.name);
    				}
    			}
    		}
    		Collections.sort(orderedNames);
    		
    		for (String name : orderedNames) {

    			site.add(new DefaultMutableTreeNode(name));
    		}
    		
    		root.add(site);
    	}

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(root);
    	tree.setModel(model);
    	for (int i = 0; i < tree.getRowCount(); i++) {
    	    tree.expandRow(i);
    	}
    	
    	return root;
	}

	/**
     * Generate a unique window title if default is already taken
     * @return name of the window title
     */
    private String generateWindowTitle() {
    	// count instances of Shinobi Desktop Player already created and add one 
    	
    	int instanceId = 1;
    	String windowTitle = "Shinobi Desktop Player <1>";
    	while (windowTitleExists(windowTitle)) {
    		
    		instanceId++;
    		windowTitle = "Shinobi Desktop Player <"+instanceId+">";
    	}
		return windowTitle;
	}
    
    /**
     * Check if an existing window has this name already
     * @param windowTitle - title of the window to look for
     * @return 
     */
    private boolean windowTitleExists(String windowTitle) {
    	String cmd = "xwininfo -root -tree | grep -c \""+windowTitle+"\"";
    	System.out.println("Executing shell command: "+cmd);
    	try {
			Process process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmd})
                    .redirectErrorStream(true)
                    .start();

            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            
				while ( (line = br.readLine()) != null ) {
					System.out.println("Shell returned: "+line);
				    if (line.equalsIgnoreCase("0")) {
				    	return false;
				    } else {
				    	return true;
				    }
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
    	return false;
    }

	protected void updateVolumeButton() {
		if (audio == true) {
			
			if ((Integer) spinnerVolume.getValue() > 0) {
				btnVolume.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-mute-32.png")));
			} else {
				btnVolume.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-audio-32.png")));
			}
			
		} else {
			btnVolume.setIcon(new ImageIcon(PlayerUI.class.getResource("/assets/icons8-audio-32.png")));
		}
	}

	protected void toggleFullScreenForSelectedMonitor() {
    	/*
		int index = 0;
    	if (PLAY_MODE  == PlayMode.Live) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.stream != null && monitor.stream.length() > 0) {
					if (index == table.getSelectedRow()) {
						videoLayout.fullScreen(monitor);
						break;
					}
					index++;
				}
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.recording == true) {
					if (index == table.getSelectedRow()) {
						videoLayout.fullScreen(monitor);
						break;
					}
					index++;
				}
			}	
		}
		*/
	}

	public void addSelectedVideoToLayout(ShinobiMonitor monitor) {
    
    	if (PLAY_MODE  == PlayMode.Live) {
			if (monitor.stream != null && monitor.stream.length() > 0) {
				videoLayout.addVideoStream(monitor);
			}
			
		} else if (PLAY_MODE == PlayMode.Playback) {
			if (monitor.recording == true) {
				videoLayout.addVideoPlayback(monitor, getDateFromSpinnerAndComboBox());
			}
		}
    	
    	enableButtons();
    }
    
    public void removeSelectedVideoFromLayout(ShinobiMonitor monitor) {

    	if (PLAY_MODE  == PlayMode.Live) {
			if (monitor.stream != null && monitor.stream.length() > 0) {
				videoLayout.removeVideoStream(monitor);				
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			if (monitor.recording == true) {
				videoLayout.removeVideoPlayback(monitor);
			}
			
		}    	
			
    	enableButtons();
    }
    
    private void enableButtons() {
    	boolean enabled = false;
    	if (videoLayout.getComponentCount() > 0) {
    		enabled = true;
    	}
		btnReplay10.setEnabled(enabled);
		btnReplay5.setEnabled(enabled);
		btnReplay30.setEnabled(enabled);
		btnForward5.setEnabled(enabled);
		btnForward10.setEnabled(enabled);
		btnForward30.setEnabled(enabled);
		btnPause.setEnabled(enabled);
		btnSeek.setEnabled(enabled);
		btnSpeed.setEnabled(enabled);
		btnVolume.setEnabled(enabled);
		if (PLAY_MODE == PlayMode.Playback) {
			btnDownload.setEnabled(enabled);
		} else {
			btnDownload.setEnabled(false);
		}
		spinnerPlaybackSpeed.setEnabled(enabled);
		spinnerVolume.setEnabled(enabled);		
    }
    
    
    private Date getDateFromSpinnerAndComboBox() {
    	if (cboDate == null || cboDate.getSelectedItem() == null) {
    		return null;
    	}
    	
    	Date date = (Date) spinnerPlaybackTime.getValue();
    	
    	int hours = date.getHours();
    	int minutes = date.getMinutes();
    	int seconds = date.getSeconds();
    	
    	String time = (hours < 10 ? "0"+hours : hours) + ":" + (minutes < 10 ? "0"+minutes : minutes) + ":" + (seconds < 10 ? "0"+seconds : seconds);
	
		SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getDefault());
			cal.setTime(sdf.parse(cboDate.getSelectedItem()+" "+time));
			Date date2 = cal.getTime();

			return date2;
		} catch (ParseException e) {
			e.printStackTrace();
		}    		
    	
    	return new Date();
    }
        
    public static void loadProperties() {
    	
    }
    
    public static void main(String args[]) {
        
    	try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
            	PlayerUI ui = new PlayerUI();

                ui.setVisible(true);
            	try {
            		// check if sites.ini exists
            		try {
            			// load existing sites.ini if exists
            			Wini ini = new Wini(new File("sites.ini"));
            			for (String s : ini.keySet()) {
            				ShinobiSite site = new ShinobiSite();
            				site.apiKey = ini.get(s, "apikey");
            				site.groupKey = ini.get(s, "groupkey");
            				site.host = ini.get(s, "host");
            				site.name = ini.get(s, "name");
            				site.port = Integer.parseInt(ini.get(s, "port"));
            				site.https = Boolean.parseBoolean(ini.get(s, "https"));
            				
            				ui.sites.add(site);
            			}
            		} catch (Exception e) {
            			if (e.getMessage() != null && e.getMessage().toLowerCase().contains("no such file")) {
                			JOptionPane.showMessageDialog(null, e.getMessage());
            			}
            			
            			// load popup for user to create first sites.ini file
            			SettingsUI settings = new SettingsUI(ui);
            			settings.setVisible(true);
            		}
                	
                	System.out.println("Sites loaded: "+ui.sites.size());

            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	
            	// load all sites monitors
            	for (ShinobiSite site: ui.sites) {
            		site.loadMonitors();
            	}
            	// create monitor tree
            	ui.createSiteTree();
            	
            }
        });
    }

	protected void updatePlayMode(PlayMode mode) {
    	PLAY_MODE = mode;
    	if (mode == PlayMode.Live) {
    		btnLive.setEnabled(false);
    		btnPlayback.setEnabled(true);
    		btnSeek.setEnabled(false);
    		spinnerPlaybackTime.setEnabled(false);
            cboDate.setEnabled(false);
            videoLayout.removeVideoPlayback();
    	} else if (mode == PlayMode.Playback) {
    		btnPlayback.setEnabled(false);
    		btnLive.setEnabled(true);
    		cboDate.setEnabled(true);
    		btnSeek.setEnabled(true);
    		spinnerPlaybackTime.setEnabled(true);
    		videoLayout.removeVideoStreams();
    		
    	}
    	createSiteTree();
    }

    private javax.swing.JButton btnLive;
    private javax.swing.JButton btnPlayback;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private JSpinner spinnerPlaybackTime,spinnerPlaybackSpeed,spinnerVolume;
    private JComboBox<String> cboDate;
    public VideoLayout videoLayout;
    private JToggleButton btnPause;
    private JButton btnVolume;
    private JButton btnSeek;
    private JToggleButton btnSpeed;
    private JButton btnReplay10;
    private JButton btnReplay5;
    private JButton btnReplay30;
    private JButton btnForward5;
    private JButton btnForward10;
    private JButton btnForward30;
    private JButton btnDownload;
    private JButton btnSettings;

	
	public enum TimePeriod {
		AM,
		PM
	}
	
	public enum PlayMode {
		Live,
		Playback
	}

	public double getSpeed() {
		return (Double) spinnerPlaybackSpeed.getValue();
	}
}