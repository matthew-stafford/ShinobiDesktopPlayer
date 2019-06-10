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
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;



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
	public static String groupKey;

	public static int GLOBAL_VOLUME = 100;
	
	private boolean audio = false;
	private boolean speed = false;
	private boolean paused = false;

    public PlayerUI() {
    	    	    	
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    	
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
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

        setTitle("Shinobi Desktop Player");

        jPanel1.setBackground(UIManager.getColor("Button.background"));

        jLabel1.setText("Playback Mode");

        jLabel3.setText("Sources");
        
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
        
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
        					.addContainerGap())
        				.addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addGap(3)
        					.addComponent(btnLive, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnPlayback, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addGap(72)
        					.addComponent(btnSettings, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap())
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(spinnerPlaybackTime, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnSeek)
        					.addContainerGap())
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addComponent(cboDate, 0, 249, Short.MAX_VALUE)
        					.addContainerGap())
        				.addGroup(jPanel1Layout.createSequentialGroup()
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
        			.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(cboDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING, false)
        				.addComponent(spinnerPlaybackTime)
        				.addComponent(btnSeek, GroupLayout.PREFERRED_SIZE, 40, Short.MAX_VALUE))
        			.addContainerGap())
        );
        
        table = new JTable() {
        	   private static final long serialVersionUID = 1L;

               @Override
			public boolean isCellEditable(int row, int column) {                
                       return false;               
               };
        };
        table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				JTable table =(JTable) e.getSource();
				if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
					addSelectedVideoToLayout();
		        }				
			}
		});
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);        
        table.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(table);
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
	}

	public void addSelectedVideoToLayout() {
    	int index = 0;
    	if (PLAY_MODE  == PlayMode.Live) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.stream != null && monitor.stream.length() > 0) {
					if (index == table.getSelectedRow()) {
						videoLayout.addVideoStream(monitor);
						break;
					}
					index++;
				}
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.recording == true) {
					if (index == table.getSelectedRow()) {
						videoLayout.addVideoPlayback(monitor, getDateFromSpinnerAndComboBox());
						break;
					}
					index++;
				}
			}	
		}
    	
    	enableButtons();
    }
    
    public void removeSelectedVideoFromLayout() {
    	int index = 0;
    	if (PLAY_MODE  == PlayMode.Live) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.stream != null && monitor.stream.length() > 0) {
					if (index == table.getSelectedRow()) {
						videoLayout.removeVideoStream(monitor);
						break;
					}
					index++;
				}
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.recording == true) {
					if (index == table.getSelectedRow()) {
						videoLayout.removeVideoPlayback(monitor);
						break;
					}
					index++;
				}
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
    
    public void initShinobi() {
        addMonitorsToList(api.getMonitors());
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
            	try {
            		// load host & apikey
            		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            		String appConfigPath = rootPath + "shinobidesktopplayer.properties";
            		
            		File file = new File(appConfigPath);
            		if (!file.exists()) 
            			file.createNewFile();
            		
            		Properties appProps = new Properties();
            		appProps.load(new FileInputStream(appConfigPath));
            		
                	host = appProps.getProperty("host");
                	apiKey = appProps.getProperty("api_key");
                	groupKey = appProps.getProperty("group_key");
                	
                	System.out.println("Host: "+host);
                	System.out.println("API: "+apiKey);
                	System.out.println("GroupKey: "+groupKey);
                	
                	boolean valid = true;
                	if (host == null || host.trim().length() < 6) {
                        valid = false;
                	} 
                	
                	if (apiKey == null || apiKey.trim().length() < 10) {
                	    valid = false;
                	} 
                	
                	if (groupKey == null || groupKey.trim().length() < 1) {
                	    valid = false;
                	} 
                	
                	
                	
                	if (!valid) {
                		System.out.println("Ini not configured, exiting");
                		ConfigUI cfg = new ConfigUI(host,apiKey,groupKey);
                		cfg.setVisible(true);
                		                		
                	} else {
                		System.out.println("Everything looks ok, continuing");
                	}
                	
                	// tidy trailing / from host if there
                	if (host.endsWith("/")) {
            			host = host.substring(0, host.length()-1);
            		}
                	
                	// start creating UI
                	// check API is valid
                	PlayerUI ui = null;
                    boolean apiValid =false;
                    while (!apiValid) {
                    	ui = new PlayerUI();
                        ui.api = new ShinobiAPI(apiKey, host, groupKey);
	                    if (!ui.api.checkAPIValidAndGetMonitors()) {
	                    	JOptionPane.showMessageDialog(null, "API Key is invalid.", "Error", JOptionPane.ERROR_MESSAGE);	
	                    	ConfigUI cfg = new ConfigUI(host,apiKey,groupKey);
	                		cfg.setVisible(true);
	                    } else {
	                    	apiValid = true;
	                    }
                    }
                	ui.initShinobi();
                    ui.setVisible(true);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            	

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
    		api.getVideoData(Calendar.getInstance().getTime());
    		videoLayout.removeVideoStreams();
    		
    	}
    	addMonitorsToList(api.getMonitors());
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
    private javax.swing.JScrollPane jScrollPane1;
    private JTable table;
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

	private void addMonitorsToList(TreeMap<String, ShinobiMonitor> monitors) {
		
		Object[][] cellData = new Object[monitors.size()][2];
		int row = 0;
		int size = 0;
		if (PLAY_MODE  == PlayMode.Live) {
			for (ShinobiMonitor monitor : monitors.values()) {
				if (monitor.stream != null && monitor.stream.length() > 0) {
					size++;
				}
			}
			cellData = new Object[size][2];
			for (ShinobiMonitor monitor : monitors.values()) {
				if (monitor.stream != null && monitor.stream.length() > 0) {
					cellData[row][0] = monitor.name;
					cellData[row++][1] = monitor.mid;
				}
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			for (ShinobiMonitor monitor : monitors.values()) {
				if (monitor.recording == true) {
					size++;
				}
			}
			cellData = new Object[size][2];
			for (ShinobiMonitor monitor : monitors.values()) {
				if (monitor.recording == true) {
					cellData[row][0] = monitor.name;
					cellData[row++][1] = monitor.mid;
				}
			}	
		}
		
		
		for (Object[] cell:cellData) {
			if (cell[0] == null) {
				
			}
		}
		
		table.setModel(new DefaultTableModel(
	        	cellData,
	        	new String[] {
	        		"Name", "Monitor ID"
	        	}
	        ));
		table.getColumn("Name").setPreferredWidth(400);
		table.getColumn("Monitor ID").setPreferredWidth(1);
		
		// hide monitor ID column :)
		TableColumnModel tcm = table.getColumnModel();
		tcm.removeColumn( tcm.getColumn(1) );
	}

	
	public enum TimePeriod {
		AM,
		PM
	}
	
	public enum PlayMode {
		Live,
		Playback
	}

}