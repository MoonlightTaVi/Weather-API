package sh.roadmap.tavi.weatherapi.obsolete.controller;


import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

import org.json.JSONObject;

import sh.roadmap.tavi.weatherapi.obsolete.logging.WeatherLogger;
import sh.roadmap.tavi.weatherapi.obsolete.service.IWeatherService;
import sh.roadmap.tavi.weatherapi.obsolete.tools.PropertiesReader;

/**
 * A REST controller, working with the Visual Crossing server
 */
public class WeatherController {
	
	private Optional<WeatherLogger> log = Optional.empty();
	private IWeatherService service;
	
	private String apiKey;

	private String lang = "en";
	private String units = "metric";
	
	// HTTP status, received from the server
	// -1 mean "hasn't yet tried to connect" or "could not send a request due to the application failure"
	private int lastStatusCode = -1;
	
	// Request parameters
	private Set<String> include = new HashSet<>(Arrays.asList(new String[] {"days"}));
	private Set<String> elements = new HashSet<>(Arrays.asList(new String[] {"tempmax", "temp", "tempmin", "description", "conditions", "datetime"}));

	// Receiver HTTP (JSON) response facade
	private WeatherResponse response = new WeatherResponse();

	// To prevent from DDoS
	private long lastRequestMillis;
	
	// Controller status
	private STATUS status = STATUS.READY;
	
	/**
	 * Creates a REST controller, working on fetching the weather data from the Visual Crossing service
	 * @param props - a {@link PropertiesReader} instance to read the configuration. 
	 * ".properties" file must contain the "api-key" (necessary) and (unnecessary) "include" and "elements" properties of the requests (visit Visual Crossing to get known of them, or use the default ones). The last ones are sequences of key words, divided by a comma 
	 */
	public WeatherController(PropertiesReader props) {
		props.get("api-key").ifPresent(val -> apiKey = val);
		props.get("include").ifPresent(val -> include = new HashSet<>(Arrays.asList(val.split(","))));
		props.get("elements").ifPresent(val -> elements = new HashSet<>(Arrays.asList(val.split(","))));
		if (props.getStatus() != null) {
			log.ifPresent(log -> log.severe("Could not read the properties file", props.getStatus()));
			status = STATUS.FAILED_TO_INITIALIZE;
		}
	}
	
	/**
	 * (Unnecessary) Sets the database service facade, which is then used for caching responses and other data.
	 * @param service - an instance of WeatherService, a Redis service facade
	 */
	public void setService(IWeatherService service) {
		this.service = service;
		response.setService(service);
		service.getString("last_location").ifPresent(loc -> service.getObject(loc).ifPresent(obj -> response.updated(obj)));
	}

	/**
	 * Get the currently used language of the response's weather descriptions
	 * @return a String, which may contain "en", "ru", "de", etc.
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * Sets the language of the next received response (does nothing with the current one)
	 * @param lang - "en", "ru", "de", etc.
	 */
	public void setLang(String lang) {
		this.lang = lang;
		if (service != null) {
			service.putString("lang", lang);
		}
	}

	/**
	 * Get the currently use unit system (US units, UK units, Metric units, Base scientific units)
	 * @return - "us", "uk", "metric", "base"
	 */
	public String getUnits() {
		return units;
	}
	
	/**
	 * Directly changes the "units" field
	 * @param unitSystem - a unit system to use
	 */
	private void setUnits(String unitSystem) {
		units = unitSystem;
		if (service != null) {
			service.putString("units", units);
		}
	}
	
	/**
	 * Sets the unit system of the next received response to the one used in the most of the countries (the metric one)
	 * @return - Itself, for chaining
	 */
	public WeatherController setMetric() {
		setUnits("metric");
		return this;
	}
	
	/**
	 * Sets the unit system of the next received response to the one used in the United States
	 * @return - Itself, for chaining
	 */
	public WeatherController setUsUnits() {
		setUnits("us");
		return this;
	}
	
	/**
	 * Sets the unit system of the next received response to the one used in the United Kingdom
	 * @return - Itself, for chaining
	 */
	public WeatherController setUkUnits() {
		setUnits("uk");
		return this;
	}
	
	/**
	 * Sets the unit system of the next received response to the one used in the scientific researches (for example, the degree units will be Kelvin's)
	 * @return - Itself, for chaining
	 */
	public WeatherController setBaseUnits() {
		setUnits("base");
		return this;
	}
	
	/**
	 * The main method to work with requests. Requests the weather data for the specified location and date.
	 * @param location - The location you want to get the weather data for
	 * @param forDate - The date you want to get the weather data for (may be two dates, separated with a slash, also the latter/the only one may be "next{number}days", where "{number}" is a string value of an integer)
	 * @return - The response facade to work with further
	 */
	public WeatherResponse sendRequest(String location, String forDate) {
		// Replace the white spaces
		location = location.replaceAll("\\s", "_");
		
		// If we request the info on the location, which is not the last location we got the data for, AND
		// we have received this info earlier (it's in the database), we fetch the request from the WeatherService
		// If we send a request again (the last location is the current location), we update
		if (service != null && !service.getString("last_location").orElse("???").equals(location)
				&& service.getObject(location).isPresent()) {
			service.getObject(location).ifPresent(lastResponse -> response.updated(lastResponse));
			service.putString("last_location", location);
			return response;
		}
		
		// 10 seconds should pass between request !
		if (lastRequestMillis != 0 && (System.currentTimeMillis() - lastRequestMillis) < 10000) {
			log.ifPresent(log -> log.info("You can't request the weather data more often than each 10 seconds."));
			lastStatusCode = -1;
			
			if (service != null) {
				service.getObject(location).ifPresent(lastResponse -> response.updated(lastResponse));
			}
			
			return response; // Return the last response
		}
		
		// Update
		lastRequestMillis = System.currentTimeMillis();
		
		// Wrong or absent API in ".properties"
		if (apiKey == null) {
			log.ifPresent(log -> log.info("An exception occurred when trying to get the weather data:"));
			log.ifPresent(log -> log.info("\tCould not retrieve the API key."));
			lastStatusCode = -1;
		}
		// Wrong location
		if (location == null || location.isBlank()) {
			log.ifPresent(log -> log.info("You have not set the desired location to get the weather data for!"));
			lastStatusCode = -1;
		}
		
		// Collect the request parameters
		String include = String.join(",", this.include);
		String elements = String.join(",", this.elements);
		
		// Finally, send the request
		URI uri = URI.create(String.format("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/%s/%s?key=%s&unitGroup=%s&lang=%s&include=%s&elements=%s&contentType=json", location, forDate, apiKey, units, lang, include, elements));
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout( Duration.ofSeconds(10) )
				.GET()
				.build();
		
		// The default status
		status = STATUS.FAILED_TO_FETCH;
		try {
			HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
			lastStatusCode = httpResponse.statusCode();
			response.update(new JSONObject(httpResponse.body())); // Update the response
			status = STATUS.READY; // Update to the OK status
		}
		// Some errors...
		catch (IOException e) {
			log.ifPresent(log -> log.warning("URL not found: " + uri, e));
			response.update(e);
		} catch (InterruptedException e) {
			log.ifPresent(log -> log.warning("Connection interrupted", e));
			response.update(e);
		} catch (org.json.JSONException e) {
			log.ifPresent(log -> log.warning("Either API key is not valid, or the location is not specified", e));
			response.update(e);
		} catch (Exception e) {
			log.ifPresent(log -> log.warning("Unknown exception", e));
			response.update(e);
			e.printStackTrace();
		} finally {
			log.ifPresent(log -> log.info("STATUS CODE: " + lastStatusCode));
		}
		
		return response;
	}
	
	/**
	 * Used for the UI description of the used degree units
	 * @return - "C" (Celsius), "F" (Fahrengeit) or "K" (Kelvin)
	 */
	public String getDegreeUnit() {
		return units.equals("metric") || units.equals("uk") ? "C" : units.equals("us") ? "F" : "K";
	}
	
	/**
	 * Returns the current status code of the request. <br>
	 * -1 means "hasn't yet tried to connect" or "could not send a request due to the application failure".
	 * @return - an int number, representing a status code 
	 */
	public int getStatusCode() {
		return lastStatusCode;
	}

	/**
	 * It is absolutely needed to close all the connections (of logger to a log file and of sevice to the database) on application quit.
	 */
	public void dispose() {
		log.ifPresent(log -> log.dispose());
		if (service != null) {
			service.dispose();
		}
	}
	
	/**
	 * Get the current REST controller status (see the return value type for info)
	 * @return - WeatherController.STATUS
	 */
	public STATUS getStatus() {
		return status;
	}
	
	/**
	 * Optionally set a logger
	 * @param logger - a {@link WeatherLogger} instance
	 */
	public void setLogger(WeatherLogger logger) {
		log = Optional.of(logger);
		if (response != null) {
			response.setLogger(logger);
		}
	}

	/**
	 * FAILED_TO_INITIALIZE - not able to send requests (because no API key or something else) <br>
	 * FAILED_TO_FETCH - a server error OR wrong request arguments <br>
	 * READY - waiting to send a request
	 */
	public enum STATUS {
		FAILED_TO_INITIALIZE,
		FAILED_TO_FETCH,
		READY
	}
	
}
