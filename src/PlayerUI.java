import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import javax.swing.GroupLayout.Alignment;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ComponentSampleModel;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Canvas;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;

/**
 * @author Matthew Stafford
 * 
 */
public class PlayerUI extends javax.swing.JFrame {

	private ShinobiAPI api;
	
	public static HashMap<String, VideoFrame> videoFrames = new HashMap<String, VideoFrame>();
	public static Dimension full_screen_size = new Dimension();
	public static PlayMode PLAY_MODE = PlayMode.Live;
	public static String WINDOW_TITLE;
	private TimePeriod playbackTimePeriod = TimePeriod.AM;

    public PlayerUI() {
    	addWindowListener(new WindowAdapter() {
    		@Override
    		public void windowClosing(WindowEvent e) {
    			// streams
    			String cmd1 = "pkill -f \"mpv --no-cache --volume 0 --keep-open --profile=low-latency -wid\"";
    			System.out.println(cmd1);
    			try {
    	            Process process = 
    	                new ProcessBuilder(new String[] {"bash","-c", cmd1})
    	                    .redirectErrorStream(true)
    	                    .start();
    	            BufferedReader br = new BufferedReader(
    	                    new InputStreamReader(process.getInputStream()));
    	                String line = null;
    	                while ( (line = br.readLine()) != null ) {
    	                	System.out.println(line);
    	                }
    	        } catch (Exception ex) {
    	        	ex.printStackTrace();
    	        }
    			// playback
    			String cmd2 = "pkill -f \"mpv --profile=low-latency --start=\"";
    			System.out.println(cmd1);
    			try {
    	            Process process = 
    	                new ProcessBuilder(new String[] {"bash","-c", cmd2})
    	                    .redirectErrorStream(true)
    	                    .start();
    	            BufferedReader br = new BufferedReader(
    	                    new InputStreamReader(process.getInputStream()));
    	                String line = null;
    	                while ( (line = br.readLine()) != null ) {
    	                	System.out.println(line);
    	                }
    	        } catch (Exception ex) {
    	        	ex.printStackTrace();
    	        }
    		}
    	});
		
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        btnLive = new javax.swing.JButton();
        btnLive.setToolTipText("Live");
        btnLive.setEnabled(false);
        btnLive.setIcon(new ImageIcon("assets/live.png"));
        btnLive.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		updatePlayMode(PlayMode.Live);
        	}
        });
        btnPlayback = new javax.swing.JButton();
        btnPlayback.setToolTipText("Playback");
        btnPlayback.setIcon(new ImageIcon("assets/playback.png"));
        btnPlayback.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {        		
        		updatePlayMode(PlayMode.Playback);
        	}
        });
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel2.setVisible(false);
        jSlider1 = new javax.swing.JSlider();
        jSlider1.setToolTipText("");
        jSlider1.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		if (lblTime != null) {
	        		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd MMM yyyy");
	        		lblTime.setText(sdf.format(getTimeFromSlider()));
        		}
        	}
        });
        jSlider1.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseReleased(MouseEvent e) {
        		System.out.println(getTimeFromSlider());
        	}
        });
        jSlider1.setEnabled(false);
        jSlider1.setPaintTicks(true);
        jSlider1.setPaintLabels(true);
        jPanel3 = new VideoLayout();
        jPanel3.setOpaque(false);
        jPanel3.setBackground(new Color(0,0,0,0));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
       
        Random r = new Random();
        
        this.WINDOW_TITLE = "Shinobi Desktop Player "+r.nextInt(1000000);
        
        setTitle(WINDOW_TITLE);

        jPanel1.setBackground(UIManager.getColor("Button.background"));

        jLabel1.setText("Playback Mode");

        jLabel3.setText("Sources");
        
        JLabel lblMonitorGroups = new JLabel("Monitor Groups");
        
        JComboBox comboBox = new JComboBox();
        
        JButton btnSettings = new JButton("");
        btnSettings.setEnabled(false);
        btnSettings.setIcon(new ImageIcon("assets/settings.png"));
        
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
        				.addComponent(jLabel1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        						.addGroup(jPanel1Layout.createSequentialGroup()
        							.addComponent(btnLive, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(btnPlayback, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
        							.addGap(72)
        							.addComponent(btnSettings, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
        						.addComponent(lblMonitorGroups, GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
        						.addComponent(comboBox, 0, 246, Short.MAX_VALUE))
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
        			.addComponent(lblMonitorGroups)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 754, Short.MAX_VALUE)
        			.addContainerGap())
        );
        
        table = new JTable() {
        	   private static final long serialVersionUID = 1L;

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
				int index = 0;
				if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
					
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
									jPanel3.addVideoPlayback(monitor, getTimeFromSlider());
									break;
								}
								index++;
							}
						}	
					}
		        }				
			}
		});
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);        
        table.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(table);
        jPanel1.setLayout(jPanel1Layout);

        jSlider1.setMaximum(43200);
        jSlider1.setMinimum(10);
       
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
        Calendar dates = Calendar.getInstance();
        cboDate = new JComboBox<String>();
        cboDate.setEnabled(false);
        for (int i = 0 ; i < 100; i++) {
        	cboDate.addItem(sdf.format(dates.getTime()));
        	dates.add(Calendar.DATE, -1);
        }
        
        DateFormat dateFormat = new SimpleDateFormat("a");
        Calendar cal = Calendar.getInstance();
        System.out.println(dateFormat.format(cal.getTime()));
        
        JButton btnAm = new JButton("PM");
        btnAm.setEnabled(false);
        if (dateFormat.format(cal.getTime()).equalsIgnoreCase("pm")) {
			playbackTimePeriod = TimePeriod.PM;
			btnAm.setText("AM");
        } else {
        	playbackTimePeriod = TimePeriod.AM;
			btnAm.setText("PM");
        }
        
        int hours = Integer.parseInt(new SimpleDateFormat("hh").format(cal.getTime())) * 60 * 60;
        int minutes = Integer.parseInt(new SimpleDateFormat("mm").format(cal.getTime())) * 60;
        jSlider1.setValue(hours+minutes);
        
        btnAm.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (playbackTimePeriod == TimePeriod.PM) {
        			btnAm.setText("PM");
        			playbackTimePeriod = TimePeriod.AM;
        		} else if (playbackTimePeriod == TimePeriod.AM) {
        			btnAm.setText("AM");
        			playbackTimePeriod = TimePeriod.PM;
        		}
        	}
        });
        
        lblTime = new JLabel("21:46:01 19 May 2019");
        lblTime.setHorizontalAlignment(SwingConstants.CENTER);
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss dd MMM yyyy");
		lblTime.setText(sdf2.format(getTimeFromSlider()));
		
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addComponent(cboDate, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnAm)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(jSlider1, GroupLayout.DEFAULT_SIZE, 972, Short.MAX_VALUE)
        					.addGap(10))
        				.addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
        					.addComponent(lblTime, GroupLayout.PREFERRED_SIZE, 871, GroupLayout.PREFERRED_SIZE)
        					.addGap(99))))
        );
        jPanel2Layout.setVerticalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addComponent(lblTime)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.TRAILING)
        				.addComponent(jSlider1, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addGap(8)
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(btnAm)
        						.addComponent(cboDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
        			.addContainerGap())
        );
        jPanel2.setLayout(jPanel2Layout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, 989, Short.MAX_VALUE))
        		.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, 1262, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, 919, Short.MAX_VALUE)
        				.addComponent(jPanel1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 919, Short.MAX_VALUE))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        
        jPanel3.setLayout(null);
        
        btnSettings.setVisible(false);
        
        
        getContentPane().setLayout(layout);
        pack();
        
        
        
        ToolTipManager.sharedInstance().setInitialDelay(0);
    }

    
    private Date getTimeFromSlider() {
    	if (cboDate == null || cboDate.getSelectedItem() == null) {
    		return null;
    	}
    	int value = jSlider1.getValue();
    	
    	int hours = (value/3600);
    	int minutes = (value%3600)/60;
    	int seconds = ((value%3600)%60);
    	
    	String time = (hours < 10 ? "0"+hours : hours) + ":" + (minutes < 10 ? "0"+minutes : minutes) + ":" + (seconds < 10 ? "0"+seconds : seconds);
    	
    	if (playbackTimePeriod == TimePeriod.AM) {
    		SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
    		try {
    			Calendar cal = Calendar.getInstance();
    			cal.setTimeZone(TimeZone.getDefault());
    			cal.setTime(sdf.parse(cboDate.getSelectedItem()+" "+time));
				Date date = cal.getTime();

				return date;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	} else if (playbackTimePeriod == TimePeriod.PM) {
    		try {
    			SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
    			Calendar cal = Calendar.getInstance();    		
    			cal.setTimeZone(Calendar.getInstance().getTimeZone());
				cal.setTime(sdf.parse(cboDate.getSelectedItem()+" "+time));
	    		cal.add(Calendar.HOUR, 12);
	    		
	    		return cal.getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}    	
    	return new Date();
    }
    
    public void initShinobi() {
        addMonitorsToList(api.getMonitors());
    }
    

    public static void main(String args[]) {
        try {
        	// set nimbus look & feel, ubuntu look & feel jslider is invisible?
        	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        	
        	
        	
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	try {
                    
            		// load host & apikey
                	Wini ini = new Wini(new File("config.ini"));
                	String host = ini.get("Config", "host");
                	String api = ini.get("Config", "api_key");
                	String groupKey = ini.get("Config", "group_key");
                	
                	boolean valid = true;
                	if (host == null || host.trim().length() < 6) {
                        JOptionPane.showMessageDialog(null, "Please set host in config.ini\n\nFor example: https://shinobi.com:8080/", "Error", JOptionPane.ERROR_MESSAGE);
                        valid = false;
                	} 
                	
                	if (api == null || api.trim().length() < 10) {
                	    JOptionPane.showMessageDialog(null, "Please set api key in config.ini\n\nGenerate one by opening Shinobi, clicking your username in the top left and selected API from the menu. Set Allowed IPs to 0.0.0.0 and then press Add to generate a new API key.", "Error", JOptionPane.ERROR_MESSAGE);	
                	    valid = false;
                	} 
                	
                	if (groupKey == null || groupKey.trim().length() < 1) {
                	    JOptionPane.showMessageDialog(null, "Please set group key in config.ini. This can be found by opening Shinobi, selecting your email address in the top left and opening Settings from the menu.", "Error", JOptionPane.ERROR_MESSAGE);	
                	    valid = false;
                	} 
                	
                	// start creating UI
                	// check API is valid
                	PlayerUI ui = new PlayerUI();
                    ui.api = new ShinobiAPI(api, host, groupKey);
                   
                    if (!ui.api.checkAPIValidAndGetMonitors()) {
                    	JOptionPane.showMessageDialog(null, "API Key is invalid.", "Error", JOptionPane.ERROR_MESSAGE);	
                	    valid = false;
                    }
                	
                	if (!valid) {
                		System.exit(0);
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
            jSlider1.setEnabled(false);
            jPanel2.setVisible(false);
            jPanel3.removeVideoPlayback();
    	} else if (mode == PlayMode.Playback) {
    		btnPlayback.setEnabled(false);
    		btnLive.setEnabled(true);
    		cboDate.setEnabled(true);
    		jSlider1.setEnabled(true);
            jPanel2.setVisible(true);
    		api.getVideoData(Calendar.getInstance().getTime());
    		jPanel3.removeVideoStreams();
    		
    	}
    	addMonitorsToList(api.getMonitors());
    }

    private javax.swing.JButton btnLive;
    private javax.swing.JButton btnPlayback;
    private javax.swing.JLabel jLabel1,lblTime;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private JComboBox<String> cboDate;
    private VideoLayout jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private JTable table;

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
	        		"Name", "mid"
	        	}
	        ));
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