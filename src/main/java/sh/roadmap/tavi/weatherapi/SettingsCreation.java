package sh.roadmap.tavi.weatherapi;

import sh.roadmap.tavi.weatherapi.tools.SimpleReader;
import sh.roadmap.tavi.weatherapi.view.CreateSettingsWindow;
import sh.roadmap.tavi.weatherapi.logging.WeatherLogger;
import java.io.InputStream;

/**
 * A thread with a JFrame, asking a user to create the .properties file
 */
public class SettingsCreation extends Thread {
	
	private WeatherLogger logger;
	private InputStream inStream;
	
	private Object lock = new Object();
	
	/**
	 * 
	 * @param logger - WeatherLogger to log exceptions
	 * @param inStream - the result of the Thread completion, an InputStream to read .properties of the WeatherAPI
	 */
	public SettingsCreation(WeatherLogger logger, InputStream inStream) {
		this.logger = logger;
		this.inStream = inStream;
	}
	
	@Override
	public void run() {
		synchronized (lock) {
			CreateSettingsWindow settings = new CreateSettingsWindow(lock);
			while (settings.isActive()) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					logger.severe("SettingsCreation thread interrupted for an unknown reason", e);
					System.exit(0);
				}
			}
			logger.info("Tried to create the .properties file.");
			if (settings.status != null) {
				logger.severe("Could not create the necessary properties file.", settings.status);
				logger.dispose();
				System.exit(0);
			}
			logger.info("(Success)");
			inStream = new SimpleReader().streamOf("data/weather-api.properties");
		}
	}
	
	/**
	 * Get the resulting InputStream, created after user submits the necessary information into the window
	 * @return - Resulting InputStream of the created .properties file
	 */
	public InputStream getResultingInputStream() {
		return inStream;
	}
}