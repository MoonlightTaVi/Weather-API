package sh.roadmap.tavi.weatherapi.service;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestConfig {
	
	private Logger log = LoggerFactory.getLogger(RequestConfig.class);
	
	@SuppressWarnings("unused")
	private String propertiesFile;
	
	private String apiKey;
	private String uri;
	private String elements = "tempmax,temp,tempmin,description,conditions,datetime";
	private String include = "days";
	
	
	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
		loadProperties(propertiesFile);
	}
	
	private void loadProperties(String propertiesFile) {
		ResourceBundle rb = ResourceBundle.getBundle("weather-api");
		apiKey = getProperty(rb, "api-key", null);
		uri = getProperty(rb, "base-uri", null);
		elements = getProperty(rb, "elements", elements);
		include = getProperty(rb, "include", include);
		log.info("Loaded properties from: {}", propertiesFile);
	}
	
	
	private String getProperty(ResourceBundle rb, String key, String def) {
		try {
			String prop = rb.getString(key);
			return prop;
		} catch (MissingResourceException e) {
			log.warn("Could not load: {}; Fallback to: {}", key, def);
			return def;
		}
	}
	

	public String getApiKey() {
		return apiKey;
	}

	public String getUri() {
		return uri;
	}

	public String getElements() {
		return elements;
	}

	public String getInclude() {
		return include;
	}
	
}
