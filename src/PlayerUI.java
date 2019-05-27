import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DateFormatter;



/**
 * @author Matthew Stafford
 * 
 */
public class PlayerUI extends javax.swing.JFrame {

	private ShinobiAPI api;
	
	public static HashMap<String, VideoFrame> videoFrames = new HashMap<String, VideoFrame>();
	public static Dimension full_screen_size = new Dimension();
	public static PlayMode PLAY_MODE = PlayMode.Live;
	public static String host;
	public static String apiKey;
	public static String groupKey;
	public static String WINDOW_TITLE;

    public PlayerUI() {
    	
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	
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
				new MPVManager("").KillAll();			
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
        btnLive.setIcon(new ImageIcon(getClass().getClassLoader().getResource("assets/live.png")));
        btnLive.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent arg0) {
        		updatePlayMode(PlayMode.Live);
        	}
        });
        btnPlayback = new javax.swing.JButton();
        btnPlayback.setToolTipText("Playback");
        btnPlayback.setIcon(new ImageIcon(classLoader.getResource("assets/playback.png")));
        btnPlayback.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent e) {        		
        		updatePlayMode(PlayMode.Playback);
        	}
        });
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel2.setVisible(false);
        
        
        jPanel3 = new VideoLayout();
        jPanel3.setOpaque(false);
        jPanel3.setBackground(new Color(0,0,0,0));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
       
        // generate random title so xwininfo can identify which window to get
        // allows for multiple clients 
        Random r = new Random();        
        PlayerUI.WINDOW_TITLE = "Shinobi Desktop Player "+r.nextInt(1000000);        
        setTitle(WINDOW_TITLE);

        jPanel1.setBackground(UIManager.getColor("Button.background"));

        jLabel1.setText("Playback Mode");

        jLabel3.setText("Sources");
        
        JButton btnSettings = new JButton("");
        btnSettings.setEnabled(true);
        btnSettings.setIcon(new ImageIcon(classLoader.getResource("assets/settings.png")));
        
        spinner = new JSpinner(new SpinnerDateModel() );
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinner, "HH:mm:ss");
        DateFormatter formatter = (DateFormatter)timeEditor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false);
        formatter.setOverwriteMode(true);
        spinner.setEditor(timeEditor);
        spinner.setValue(new Date());
        
        cboDate = new JComboBox<String>();
        cboDate.setEnabled(false);
        
        JButton btnSeek = new JButton("Play");
        btnSeek.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		System.out.println(getDateFromSpinnerAndComboBox());
        		for (Component c : jPanel3.getComponents()) {
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
        					.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
        					.addContainerGap())
        				.addComponent(jLabel1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(btnLive, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnPlayback, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addGap(72)
        					.addComponent(btnSettings, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap())
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(spinner, GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnSeek)
        					.addContainerGap())
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(cboDate, 0, 246, Short.MAX_VALUE)
        					.addContainerGap())
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
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
        					.addComponent(btnSettings)))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(cboDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(btnSeek)
        				.addComponent(spinner, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
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
        
        popupMenu = new JPopupMenu();
        addPopup(table, popupMenu);
        
        mntmAdd = new JMenuItem("Add");
        mntmAdd.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (table.getSelectedRow() != -1) {
        			addSelectedVideoToLayout();
        		}
        	}
        });
        popupMenu.add(mntmAdd);
        
        mntmRemove = new JMenuItem("Remove");
        popupMenu.add(mntmRemove);
        
        menuItem = new JMenuItem("");
        menuItem.setEnabled(false);
        popupMenu.add(menuItem);
        
        mntmFullscreen = new JMenuItem("Fullscreen");
        mntmFullscreen.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (table.getSelectedRow() != -1) {
        			toggleFullScreenForSelectedMonitor();
        		}
        	}
        });
        popupMenu.add(mntmFullscreen);
        jPanel1.setLayout(jPanel1Layout);
    
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
        Calendar dates = Calendar.getInstance();
        for (int i = 0 ; i < 100; i++) {
        	cboDate.addItem(sdf.format(dates.getTime()));
        	dates.add(Calendar.DATE, -1);
        }
        
        
        btnPause = new JButton("Pause");
        btnPause.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
        	}
        });
        
        btnEvents = new JButton("Events");
        
        btnNow = new JButton("Now");
        
        btnMute = new JButton("Mute");
        
        btnSpeed = new JButton("Speed");
		
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addComponent(btnPause)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnNow, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnMute, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnSpeed, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnEvents, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(566, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        					.addComponent(btnPause, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
        					.addComponent(btnNow, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
        					.addComponent(btnMute, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
        					.addComponent(btnSpeed, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE))
        				.addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
        					.addComponent(btnEvents, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap())))
        );
        jPanel2.setLayout(jPanel2Layout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, 989, Short.MAX_VALUE)
        					.addGap(12))
        				.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, 989, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, 875, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(35, Short.MAX_VALUE))
        		.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, 962, Short.MAX_VALUE)
        );
        
        jPanel3.setLayout(null);
        
        btnSettings.setVisible(false);

		setDefaultLookAndFeelDecorated(true);
        
        getContentPane().setLayout(layout);
        pack();
        
        ToolTipManager.sharedInstance().setInitialDelay(0);
    }

    protected void toggleFullScreenForSelectedMonitor() {
    	int index = 0;
    	if (PLAY_MODE  == PlayMode.Live) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.stream != null && monitor.stream.length() > 0) {
					if (index == table.getSelectedRow()) {
						jPanel3.fullScreen(monitor);
						break;
					}
					index++;
				}
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.recording == true) {
					if (index == table.getSelectedRow()) {
						jPanel3.fullScreen(monitor);
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
						jPanel3.addVideoStream(monitor);
						break;
					}
					index++;
				}
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.recording == true) {
					if (index == table.getSelectedRow()) {
						jPanel3.addVideoPlayback(monitor, getDateFromSpinnerAndComboBox());
						break;
					}
					index++;
				}
			}	
		}
    }
    
    public void removeSelectedVideoFromLayout() {
    	int index = 0;
    	if (PLAY_MODE  == PlayMode.Live) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.stream != null && monitor.stream.length() > 0) {
					if (index == table.getSelectedRow()) {
						jPanel3.removeVideoStream(monitor);
						break;
					}
					index++;
				}
			}			
		} else if (PLAY_MODE == PlayMode.Playback) {
			for (ShinobiMonitor monitor : api.getMonitors().values()) {
				if (monitor.recording == true) {
					if (index == table.getSelectedRow()) {
						jPanel3.removeVideoPlayback(monitor);
						break;
					}
					index++;
				}
			}	
		}
    }
    
    
    private Date getDateFromSpinnerAndComboBox() {
    	if (cboDate == null || cboDate.getSelectedItem() == null) {
    		return null;
    	}
    	
    	Date date = (Date) spinner.getValue();
    	
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
            cboDate.setEnabled(false);
            jPanel3.removeVideoPlayback();
    	} else if (mode == PlayMode.Playback) {
    		btnPlayback.setEnabled(false);
    		btnLive.setEnabled(true);
    		cboDate.setEnabled(true);
    		api.getVideoData(Calendar.getInstance().getTime());
    		jPanel3.removeVideoStreams();
    		
    	}
    	addMonitorsToList(api.getMonitors());
    }

    private javax.swing.JButton btnLive;
    private javax.swing.JButton btnPlayback;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private JSpinner spinner;
    private JComboBox<String> cboDate;
    private VideoLayout jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private JTable table;
    private JButton btnPause;
    private JButton btnEvents;
    private JButton btnNow;
    private JButton btnMute;
    private JButton btnSpeed;
    private JPopupMenu popupMenu;
    private JMenuItem mntmAdd;
    private JMenuItem mntmRemove;
    private JMenuItem menuItem;
    private JMenuItem mntmFullscreen;

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
	}

	
	public enum TimePeriod {
		AM,
		PM
	}
	
	public enum PlayMode {
		Live,
		Playback
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}