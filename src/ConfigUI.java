import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class ConfigUI extends JFrame {

	private JPanel contentPane;
	private JTextPane txtHost, txtApiKey, txtGroupKey, txtName, txtPort;
	private JLabel lblHost, lblApiKey, lblGroupKey;
	private JButton btnCancel, btnSave;
	private JComboBox<Boolean> cboHttps;
	
	
	String host, apikey,groupkey;

	public ConfigUI(String host, String apikey, String groupkey) {
		setResizable(false);
		this.host = host;
		this.apikey = apikey;
		this.groupkey = groupkey;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 460, 270);
		setTitle("Please enter Shinobi Configuration");
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
	
		lblHost = new JLabel();
		lblHost.setBounds(15, 29, 80, 20);
		lblHost.setText("Host:");
		
		txtHost = new JTextPane();
		txtHost.setText(host);
		txtHost.setBounds(113, 29, 322, 20);
		txtHost.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				updateUI();
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		lblApiKey = new JLabel();
		lblApiKey.setBounds(15, 127, 80, 20);
		lblApiKey.setText("API Key:");
		
		txtApiKey = new JTextPane();
		txtApiKey.setText(apikey);
		txtApiKey.setBounds(113, 127, 322, 20);
		txtApiKey.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				updateUI();
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		lblGroupKey = new JLabel();
		lblGroupKey.setBounds(15, 159, 80, 20);
		lblGroupKey.setText("Group Key:");
		
		txtGroupKey = new JTextPane();
		txtGroupKey.setText(groupkey);
		txtGroupKey.setBounds(113, 159, 325, 20);
		txtGroupKey.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				updateUI();
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		btnSave = new JButton("Save");
		btnSave.setBounds(325, 191, 110, 30);
		btnSave.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        		String appConfigPath = rootPath + "shinobidesktopplayer.properties";
        		
        		Properties appProps = new Properties();
        		try {
					appProps.load(new FileInputStream(appConfigPath));
					
					appProps.setProperty("host", txtHost.getText().trim());
					appProps.setProperty("api_key", txtApiKey.getText().trim());
					appProps.setProperty("group_key", txtGroupKey.getText().trim());
					
					String newAppConfigPropertiesFile = rootPath + "shinobidesktopplayer.properties";
					appProps.store(new FileWriter(newAppConfigPropertiesFile), "store to properties file");
					
					PlayerUI.apiKey = txtApiKey.getText().trim();
					PlayerUI.host = txtHost.getText().trim();
					PlayerUI.groupKey = txtGroupKey.getText().trim();
					
					dispose();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(15, 191, 110, 30);
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		contentPane.add(lblHost);
		contentPane.add(lblApiKey);
		contentPane.add(lblGroupKey);
		contentPane.add(txtHost);
		contentPane.add(txtApiKey);
		contentPane.add(txtGroupKey);
		contentPane.add(btnSave);
		contentPane.add(btnCancel);
		
		updateUI();
		
		setContentPane(contentPane);
		
		txtPort = new JTextPane();
		txtPort.setText("<dynamic>");
		txtPort.setBounds(113, 61, 80, 20);
		contentPane.add(txtPort);
		
		JLabel lblPort = new JLabel();
		lblPort.setText("Port:");
		lblPort.setBounds(15, 61, 80, 20);
		contentPane.add(lblPort);
		
		JLabel lblHttps = new JLabel();
		lblHttps.setText("HTTPS?");
		lblHttps.setBounds(15, 95, 80, 20);
		contentPane.add(lblHttps);
		
		cboHttps = new JComboBox<Boolean>();
		cboHttps.setModel(new DefaultComboBoxModel(new String[] {"Yes", "No"}));
		cboHttps.setSelectedIndex(1);
		cboHttps.setBounds(113, 93, 80, 24);
		contentPane.add(cboHttps);
		
		JLabel lblName = new JLabel();
		lblName.setText("Name:");
		lblName.setBounds(15, 0, 80, 20);
		contentPane.add(lblName);
		
		txtName = new JTextPane();
		txtName.setText("<dynamic>");
		txtName.setBounds(113, 0, 322, 20);
		contentPane.add(txtName);
	}

	public ConfigUI() {
		// TODO Auto-generated constructor stub
	}

	private void updateUI() {
		if (isFormValid() ) {
			btnSave.setEnabled(true);
		} else {
			btnSave.setEnabled(false);
		}
	}
	
	private boolean isFormValid() {
		if (txtHost.getText() != null && txtHost.getText().trim().length() > 5) {
			if (txtApiKey.getText() != null && txtApiKey.getText().trim().length() > 5) {
				if (txtGroupKey.getText() != null && txtGroupKey.getText().trim().length() > 0) {
					return true;
				}
			}
			return false;
		}
		return false;
	}
}
