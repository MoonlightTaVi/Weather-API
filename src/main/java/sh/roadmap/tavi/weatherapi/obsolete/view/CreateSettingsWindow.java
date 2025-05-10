package sh.roadmap.tavi.weatherapi.obsolete.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.*;

/**
 * JFrame, handling the creation of .properties file
 */
public class CreateSettingsWindow implements ActionListener {
	
	/** If an exception occurs when writing to the newly created .properties,
	 *  we can know about it from this field */
	public IOException status;
	
	// Whether we're finished with this JFrame
	private volatile boolean active = true;
	
	private JFrame frame;
	private String address = "";
	private String port = "";
	private String api = "";
	
	private JTextField apiField = new JTextField();
	private JTextField addressField = new JTextField();
	private JTextField portField = new JTextField();
	
	private Object lock;
	
	/**
	 * Instantiates a JFrame, asking the user to fill in the information, required for the WeatherAPI
	 * @param lock - Used by SettingsCreation thread 
	 * for waiting until the JFrame is closed, or the information is submitted
	 */
	public CreateSettingsWindow(Object lock) {
		this.lock = lock;
		
		frame = new JFrame("Settings files are missing");
		frame.setSize(400, 250);
		frame.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(5, 5, 390, 240);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		apiField.setMaximumSize(new Dimension(380, 20));
		addressField.setMaximumSize(new Dimension(380, 20));
		portField.setMaximumSize(new Dimension(380, 20));
		
		JLabel mainLabel = new JLabel("<html>The settings file for the application is missing. Fill-in the following info to continue ((*) indicates required information):</html>");
		mainLabel.setMaximumSize(new Dimension(350, 50));
		panel.add(mainLabel);
		panel.add(new JLabel("* Visual Crossing API key"));
		panel.add(apiField);
		panel.add(new JLabel("Redis address"));
		addressField.setText("http://localhost");
		panel.add(addressField);
		panel.add(new JLabel("Redis port"));
		portField.setText("8080");
		panel.add(portField);
		
		JButton okBtn = new JButton("Ok");
		okBtn.addActionListener(this);
		panel.add(okBtn);
		
		frame.add(panel);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				synchronized (lock) {
					frame.dispose();
					active = false;
					lock.notify();
					status = new IOException("Window closed");
				}
			}
		});
		
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized (lock) {
			api = apiField.getText();
			address = addressField.getText();
			port = portField.getText();
			
			try {
				PrintWriter writer = new PrintWriter(new FileWriter("data/weather-api.properties"));
				writer.printf("api-key=%s%n", api);
				writer.printf("db-ip=%s%n", address);
				writer.printf("db-port=%s%n", port);
				writer.close();
			} catch (IOException e1) {
				// This code won't be executed: applications exits in-before
				JFrame error = new JFrame("Application failure");
				error.setSize(150, 50);
				error.add(new JLabel("Application failure"));
				error.setVisible(true);
				status = e1;
			} finally {
				frame.dispose();
				active = false;
				lock.notify();
			}
		}
	}
	
	/**
	 * Check if the user has submitted the information, required by the program
	 * @return - True if JFrame closed / information submitted
	 */
	public boolean isActive() {
		return active;
	}
}
