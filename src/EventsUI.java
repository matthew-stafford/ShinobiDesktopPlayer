import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.JComboBox;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDayChooser;


public class EventsUI extends JFrame {

	private JPanel contentPane;
	private JTable tblData;
	private PlayerUI playerUI;
	private JComboBox<String> cboMonitors;
	private JCalendar calendar;
	private HashMap<String, Boolean> eventData = new HashMap<String,Boolean>();// key = YYYY-MM-DD, bool=has event data yes/no, for coloring calendar
	
	public EventsUI(PlayerUI playerUI, String defaultMonitorId) {
		this.playerUI = playerUI;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 860, 544);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		
		JPanel panel_1 = new JPanel();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 301, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
					.addGap(2))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGap(11))
		);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane);
		
		tblData = new JTable(){
	     	   private static final long serialVersionUID = 1L;
	
	           @Override
			public boolean isCellEditable(int row, int column) {                
	                   return false;               
	           };
	    };
	    tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblData.setFillsViewportHeight(true);
		scrollPane.setViewportView(tblData);
		panel.setLayout(null);
		
		cboMonitors = new JComboBox<String>();
		cboMonitors.setBounds(0, 5, 302, 24);
		/*
		for (ShinobiMonitor monitor : playerUI.api.getMonitors().values()) {
			cboMonitors.addItem(monitor.name);
			if (defaultMonitorId != null && defaultMonitorId.equals(monitor.mid)) {
				cboMonitors.setSelectedItem(monitor.name);
			}
		}
		*/
		
		
		panel.add(cboMonitors);
		
		calendar = new JCalendar();
		calendar.getDayChooser().setMaxDayCharacters(1);
		calendar.getDayChooser().setWeekOfYearVisible(false);
		calendar.setBounds(0, 41, 302, 224);
				
		
		disableButtonsAfterToday();
		
		calendar.addPropertyChangeListener(
		    new PropertyChangeListener() {
		        @Override
		        public void propertyChange(PropertyChangeEvent e) {
		            if (e.getNewValue() instanceof java.util.GregorianCalendar) {
		            	
		            	disableButtonsAfterToday();
		            	GregorianCalendar cal = (GregorianCalendar) e.getNewValue();
		            	getEventData(cal.getTime());
		            }
		        }

				
		    });
		
		panel.add(calendar);
		
		contentPane.setLayout(gl_contentPane);
	}

	private void disableButtonsAfterToday() {
		// disable buttons after todays date
		SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
		Date today = new Date();
		if (calendar.getYearChooser().getYear() >= today.getYear() && calendar.getMonthChooser().getMonth() >= today.getMonth()) {
			for (Component c : calendar.getDayChooser().getComponents()) {
				if (c instanceof JPanel) {
					JPanel p = (JPanel) c;
					for (Component b : p.getComponents()) {
						if (b.getClass().getName().equals("com.toedter.calendar.JDayChooser$1")) {
							JButton btn = (JButton) b;
							if (btn.getText().length() > 0) {
								try {
									Date d = sdf.parse(btn.getText() +" "+(calendar.getMonthChooser().getMonth()+1)+" "+calendar.getYearChooser().getYear());
									if (d.after(today)) {
										btn.setEnabled(false);
									}
									} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}
				}			
			}
		}
	}
	
	public void getEventData(Date date) {
	/*
		ShinobiMonitor monitor = (ShinobiMonitor) playerUI.api.getMonitors().values().toArray()[cboMonitors.getSelectedIndex()];
		

		SimpleDateFormat key = new SimpleDateFormat("yyMMdd");		
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		int limit = 2000;
		
		playerUI.api.getMotionEventData(monitor.mid, date, limit);	
		
		Object[][] cellData = new Object[monitor.motionEvents.get(key.format(date)).size()][2];
		int row = 0;
		for (MotionEvent me : monitor.motionEvents.get(key.format(date))) {
			cellData[row][0] = sdf.format(new Date(me.timeInMilliseconds));
			cellData[row++][1] = me.confidence;
		}
		Object[] columns = {"Time", "Confidence"};
		DefaultTableModel model = new DefaultTableModel(cellData,columns) {
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
		
		
		tblData.setModel(model);
		tblData.getColumn("Time").setPreferredWidth(400);
		tblData.getColumn("Confidence").setPreferredWidth(200);
		tblData.setAutoCreateRowSorter(true);
		tblData.addMouseListener(new MouseListener() {
			
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				JTable table =(JTable) e.getSource();
				if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
					System.out.println("Search to "+table.getValueAt(table.getSelectedRow(), 0));
					
					
					
					for (Component c : playerUI.videoLayout.getComponents()) {
	        			if (c instanceof VideoFrame) {
	        				try {
		        				SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
		        				
		        				SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
		        				
		        				Date selectedTime = dateAndTimeFormat.parse( dateFormat.format(calendar.getDate()) + " " + table.getValueAt(table.getSelectedRow(), 0) );
		        				
		        				System.out.println("Selected date/time="+dateAndTimeFormat.format(selectedTime));
		        				
		        				((VideoFrame) c).playVideoPlayback(selectedTime, false);
	        				} catch (Exception ex) {
	        					ex.printStackTrace();
	        				}
	        			}
	        		}
				}
				
			}
		});
		*/
	}
}
