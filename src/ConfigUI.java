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

public class ConfigUI extends JDialog {

	private JPanel contentPane;
	private JTextPane txtHost, txtApiKey, txtGroupKey;
	private JLabel lblHost, lblApiKey, lblGroupKey;
	private JButton btnCancel, btnSave;
	
	String host, apikey,groupkey;

	public ConfigUI(String host, String apikey, String groupkey) {
		this.host = host;
		this.apikey = apikey;
		this.groupkey = groupkey;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 800, 200);
		setTitle("Please enter Shinobi Configuration");
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
	
		lblHost = new JLabel();
		lblHost.setBounds(15, 15, 80, 20);
		lblHost.setText("Host:");
		
		txtHost = new JTextPane();
		txtHost.setText(host);
		txtHost.setBounds(110, 15, getWidth()-120, 20);
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
		lblApiKey.setBounds(15, 45, 80, 20);
		lblApiKey.setText("API Key:");
		
		txtApiKey = new JTextPane();
		txtApiKey.setText(apikey);
		txtApiKey.setBounds(110, 45, getWidth()-120, 20);
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
		lblGroupKey.setBounds(15, 75, 80, 20);
		lblGroupKey.setText("Group Key:");
		
		txtGroupKey = new JTextPane();
		txtGroupKey.setText(groupkey);
		txtGroupKey.setBounds(110, 75, getWidth()-120, 20);
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
		btnSave.setBounds(getWidth()-120, 115, 110, 30);
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
		btnCancel.setBounds(getWidth()-240, 115, 110, 30);
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
