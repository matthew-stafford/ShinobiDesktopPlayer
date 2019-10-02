import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class SettingsUI extends JFrame {
	
	private PlayerUI ui;
	private SettingsUI settingsUI;
	
	
	public SettingsUI(PlayerUI ui) {
		this.ui = ui;
		settingsUI = this;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 613, 418);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 605, 385);
		
		tabbedPane.addTab("Sites", null, createSitesTab(), "Add/Modify Shinobi sites");
		
		contentPane.add(tabbedPane);
		
		

		setContentPane(contentPane);
		
		validateSitesTab();
	}
	

	private JComboBox<Boolean> cboHttps;
	private JComboBox<String> cboOption;
	private JTextField txtHost, txtPort, txtApiKey,txtGroupKey,txtName;
	private JButton btnSave, btnRemove;
	
	public JComponent createSitesTab() {
		JPanel contentPane = new JPanel(null);
			
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
	
		JLabel lblOption = new JLabel();
		lblOption.setText("Option");
		lblOption.setBounds(15,0,80,20);
		contentPane.add(lblOption);
		
		cboOption = new JComboBox<String>();
		cboOption.setBounds(113, 0, 322, 20);
		cboOption.addItem("Add New");
		// add edit options for all sites
		for (ShinobiSite site : ui.sites) {
			cboOption.addItem("Edit - "+site.name+" ["+site.host+"]");
		}
		cboOption.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String selectedItem = cboOption.getSelectedItem().toString();
				if (selectedItem.equalsIgnoreCase("add new")) {
					// add new, do nothing
					
				} else if (selectedItem.startsWith("Edit - ")) {
					// edit, get site name and host to identify site
					String siteName = selectedItem.substring(6, selectedItem.indexOf("[")).trim();
					
					// find the ShinobiSite and populate text boxes with values
					for (ShinobiSite site:ui.sites) {
						if (site.name.equalsIgnoreCase(siteName)) {
							txtName.setText(site.name);
							txtHost.setText(site.host);
							txtApiKey.setText(site.apiKey);
							txtGroupKey.setText(site.groupKey);
							txtPort.setText(""+site.port);
							cboHttps.setSelectedItem((site.https?"True":"False"));
							break;	
						}
					}
				}
				validateSitesTab();
				
			}
			
		});
		contentPane.add(cboOption);

		JLabel lblName = new JLabel();
		lblName.setText("Name:");
		lblName.setBounds(15, 30, 80, 20);
		contentPane.add(lblName);
		
		txtName = new JTextField();
		txtName.setText("");
		txtName.setBounds(113, 30, 322, 20);
		txtName.addKeyListener(validateKeyListener());
		contentPane.add(txtName);
		
		JLabel lblHost = new JLabel();
		lblHost.setBounds(15, 60, 80, 20);
		lblHost.setText("Host:");
		
		txtHost = new JTextField();
		txtHost.setText("");
		txtHost.setBounds(113, 60, 322, 20);
		txtHost.addKeyListener(validateKeyListener());
		
		JLabel lblApiKey = new JLabel();
		lblApiKey.setBounds(15, 157, 80, 20);
		lblApiKey.setText("API Key:");
		
		txtApiKey = new JTextField();
		txtApiKey.setText("");
		txtApiKey.setBounds(113, 157, 322, 20);
		txtApiKey.addKeyListener(validateKeyListener());
		
		JLabel lblGroupKey = new JLabel();
		lblGroupKey.setBounds(15, 189, 80, 20);
		lblGroupKey.setText("Group Key:");
		
		txtGroupKey = new JTextField();
		txtGroupKey.setText("");
		txtGroupKey.setBounds(113, 189, 325, 20);
		txtGroupKey.addKeyListener(validateKeyListener());
		
		btnRemove = new JButton("Remove Site");
		btnRemove.setBounds(15, 221, 110, 30);
		btnRemove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		
		btnSave = new JButton("Save Site");
		btnSave.setBounds(325, 221, 110, 30);
		btnSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
								
				try {
					File sites = new File("sites.ini");
					if (!sites.exists()) {
						sites.createNewFile();
					}
					
					// trim all values for any whitespace
					String item = (String) cboOption.getSelectedItem();
					String name = txtName.getText().trim();
					String host = txtHost.getText().trim();
					Integer port = Integer.parseInt(txtPort.getText().trim());
					Boolean https = cboHttps.getSelectedIndex() == 0 ? true : false;
					String apikey = txtApiKey.getText().trim();
					String groupkey = txtGroupKey.getText().trim();
					
					if (item.equalsIgnoreCase("add new")) {
						// check if existing site with same name exists, if it does present an error
						Wini ini = new Wini(new File("sites.ini"));
						if (ini.get(name) != null) {
							// error
							JOptionPane.showMessageDialog(null, "An existing site with this name already exists, please choose another name.");
							return;
						}
						ini.put(name, "name", name);
						ini.put(name, "host", host);
						ini.put(name, "port", port);
						ini.put(name, "https", https);
						ini.put(name, "apikey", apikey);
						ini.put(name, "groupkey", groupkey);
						ini.store();
						
						updateSitesTab();
						
						for (ShinobiSite site : ui.sites) {
							site.loadMonitors();
						}
						
					} else if (item.startsWith("Edit - ")) {
						String siteName = item.substring(6, item.indexOf("[")).trim();

						// store values in sites.ini file
						Wini ini = new Wini(new File("sites.ini"));
						
						
						if (!siteName.equalsIgnoreCase(name)) {
							// site name has been changed
							System.out.println("Site name has been changed, removing old section.");
							ini.remove(siteName);
						}
						
						ini.put(name, "name", name);
						ini.put(name, "host", host);
						ini.put(name, "port", port);
						ini.put(name, "https", https);
						ini.put(name, "apikey", apikey);
						ini.put(name, "groupkey", groupkey);
						ini.store();
						
						// updating site in memory
						
						for (ShinobiSite site:ui.sites) {
							if (site.name.equalsIgnoreCase(siteName)) {
								site.name = name;
								site.host = host;
								site.apiKey = apikey;
								site.groupKey = groupkey;
								site.port = port;
								site.https = https;
								
								// refresh monitors for this site
								site.loadMonitors();
							}
						}
						
						updateSitesTab();
						
						// update main UI tree with new data
						ui.createSiteTree();

					}

					settingsUI.setVisible(false);
					settingsUI.dispose();
					settingsUI = null;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		contentPane.add(lblHost);
		contentPane.add(lblApiKey);
		contentPane.add(lblGroupKey);
		contentPane.add(txtHost);
		contentPane.add(txtApiKey);
		contentPane.add(txtGroupKey);
		contentPane.add(btnSave);
		contentPane.add(btnRemove);
		
		updateSitesTab();
		
		setContentPane(contentPane);
		
		txtPort = new JTextField();
		txtPort.setText("80");
		txtPort.setBounds(113, 91, 80, 20);
		txtPort.addKeyListener(validateKeyListener());
		contentPane.add(txtPort);
		
		JLabel lblPort = new JLabel();
		lblPort.setText("Port:");
		lblPort.setBounds(15, 91, 80, 20);
		contentPane.add(lblPort);
		
		JLabel lblHttps = new JLabel();
		lblHttps.setText("HTTPS?");
		lblHttps.setBounds(15, 125, 80, 20);
		contentPane.add(lblHttps);
		
		cboHttps = new JComboBox<Boolean>();
		cboHttps.setModel(new DefaultComboBoxModel(new String[] {"True", "False"}));
		cboHttps.setSelectedIndex(1);
		cboHttps.setBounds(113, 125, 80, 24);
		contentPane.add(cboHttps);
		
		
		
		return contentPane;
	}
	
	private void validateSitesTab() {
		// validate hosts name
		boolean saveValid = true;
		boolean removeValid = true;
		
		String host = txtHost.getText();
		boolean hostValid = true;
		
		if (host == null || host.length() == 0) {
			saveValid = hostValid = false;
		} else {
			if (host.toLowerCase().startsWith("http://") || host.toLowerCase().startsWith("https://") || host.contains("/")) {
				saveValid = hostValid = false;
			}	
		}
		
		Integer port = Integer.parseInt(txtPort.getText());
		boolean portValid = true;
		if (port == null || port <= 0) {
			saveValid = portValid = false;
		}
		
		String name = txtName.getText();
		boolean nameValid = true;
		for (ShinobiSite s : ui.sites) {
			if (name == s.name) {
				saveValid = nameValid = false;
			}
		}
		
		if (name.contains("/") || name.contains("[") || name.length() == 0) {
			saveValid = nameValid = false;
		}
		
		String apiKey = txtApiKey.getText();
		boolean apiKeyValid = true;
		if (apiKey == null || apiKey.length() == 0) {
			saveValid = apiKeyValid = false;
		}
		
		String groupKey = txtGroupKey.getText();
		boolean groupKeyValid = true;
		if (groupKey == null || groupKey.length() == 0) {
			saveValid = groupKeyValid = false;
		}
		
		String selectedItem = (String) cboOption.getSelectedItem();
		if (selectedItem.equalsIgnoreCase("add new")) {
			removeValid = false;
		}
		
		txtHost.setForeground(hostValid ? Color.black : Color.red);
		txtPort.setForeground(portValid ? Color.black : Color.red);
		txtName.setForeground(nameValid ? Color.black : Color.red);
		txtApiKey.setForeground(apiKeyValid ? Color.black : Color.red);
		txtGroupKey.setForeground(groupKeyValid ? Color.black : Color.red);
		
		
		
		btnSave.setEnabled(saveValid);
		btnRemove.setEnabled(removeValid);
		
	} 
	
	private ActionListener validateActionListener() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				validateSitesTab();
			}
		};
	}
	
	private KeyListener validateKeyListener() {
		return new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				validateSitesTab();				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
			}
		};
	}
	
	private void updateSitesTab() {
		
	}
 }
