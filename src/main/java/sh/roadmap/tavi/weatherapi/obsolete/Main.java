package sh.roadmap.tavi.weatherapi.obsolete;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import sh.roadmap.tavi.weatherapi.obsolete.controller.WeatherController;
import sh.roadmap.tavi.weatherapi.obsolete.logging.*;
import sh.roadmap.tavi.weatherapi.obsolete.service.*;
import sh.roadmap.tavi.weatherapi.obsolete.tools.*;
import sh.roadmap.tavi.weatherapi.obsolete.view.WeatherApplication;

/**
 * Application entry point
 */
public class Main {
	
	
	public static void main(String[] args) {
		
		// For logging failure at start-up
		WeatherLogger logger = new BasicWeatherLogger();
		
		// We need this folder, if .properties are absent in the resources directory, OR
		// if someone wants to create a localization, OR
		// if the user needs to cache the data in a JSON file, not Redis
		try {
			Files.createDirectories(Paths.get("data/"));
		} catch (IOException e) {
			logger.warning("Could not create the \"/data\" folder for caching and settings (not critical)", e);
		}
		
		// Trying to read properties...
		FilepathReader reader = new SimpleReader();
		InputStream inStream = reader.streamOf("data/weather-api.properties");
		
		if (inStream == null) { // Not inside the "resources"...
			
			reader = new ResourcesReader();
			inStream = reader.streamOf("weather-api.properties");
			
			if (inStream == null) { // Neither in /data folder...
				SettingsCreation settingsThread = new SettingsCreation(logger, inStream);
				settingsThread.start(); // Could not read - ask user to create .properties by filling-in the necessary info
				try {
					settingsThread.join();
					inStream = settingsThread.getResultingInputStream();
					if (inStream == null) {
						System.exit(0);
					}
				} catch (InterruptedException e) {
					logger.severe("Main thread was interrupted for an unknown reason", e);
					System.exit(0);
				}
			}
			
		}
		
		PropertiesReader props = new PropertiesReader(inStream);
		
		WeatherController api = new WeatherController(props);
		IWeatherService service = new RedisWeatherService(props);
		
		// If not connected to the Redis, fallback to JSON caching
		if (service.getStatus() != DBSTATUS.CONNECTED) {
			try {
				service = new JsonWeatherService("data/meta.json");
			} catch (IOException e) {
				logger.warning("Could not create JSON service", e);
			}
		}
		
		WeatherApplication app = new WeatherApplication(
				new CsvHandler(
						new SimpleReader("data/weather-ui.csv").setFallback( // Preferably read the locale from /data
								new ResourcesReader("weather-ui.csv")))); // If /data has no locale
		
		api.setLogger(logger);
		service.setLogger(logger);
		
		api.setService(service);
		
		app.setApi(api);
		app.setService(service);
		app.setupFrame(); // Open window
	}
}
