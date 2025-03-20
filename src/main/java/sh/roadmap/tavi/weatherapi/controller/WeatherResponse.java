package sh.roadmap.tavi.weatherapi.controller;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sh.roadmap.tavi.weatherapi.logging.WeatherLogger;
import sh.roadmap.tavi.weatherapi.service.IWeatherService;

/**
 * A facade for the JSONObject, received as a response from the VisualCrossing web service.
 * Meant to be re-utilized for several times during the application run-time.
 */
public class WeatherResponse {
	
	private JSONObject body;
	private long updatedDate; // Last time the response has been received
	private IWeatherService service; // DB connection (optional, for caching)
	private Optional<WeatherLogger> log = Optional.empty();
	
	private STATUS status;
	
	/**
	 * Creates an empty WeatherResponse object, with no information
	 */
	public WeatherResponse() {
	}

	/**
	 * Creates a WeatherResponse object, with the information on the weather
	 * @param responseBody - JSONObject, received from the server
	 */
	public WeatherResponse(JSONObject responseBody) {
		update(responseBody);
	}
	
	/**
	 * Creates an empty WeatherResponse object, then changes its status (depending on the exception)
	 * @param e - Exception, caught when trying to receive the response
	 */
	public WeatherResponse(Exception e) {
		update(e);
	}
	
	/**
	 * (Optional) Sets the database service, used for caching the results
	 * @param service - The database API service used
	 */
	public void setService(IWeatherService service) {
		this.service = service;
	}
	
	/**
	 * Used to retrieve some information on the weather at a particular day
	 * @param day - Some day, containing some information on the weather (starting from 0)
	 * @param key - Some information on the weather at this day (temperature, conditions, etc.)
	 * @return Optional String, possibly containing the desired value (Optional.empty() if no such day or no such key)
	 */
	public Optional<String> get(int day, String key) {
		try {
			JSONArray days = body.optJSONArray("days");
			JSONObject dayObject = days.getJSONObject(day);
			Object result = dayObject.opt(key);
			
			if (result != null) {
				return Optional.of(String.valueOf(result));
			}
		} catch (JSONException e1) {
			//status = e1;
			log.ifPresent(log -> log.warning("Array index out of bounds: " + String.valueOf(day), e1));
		} catch (NullPointerException e2) {
			//status = e2;
			log.ifPresent(log -> log.warning("Non-existent key \"days\" in response body OR response body absent", e2));
		}
		
		return Optional.empty();
	}
	
	/**
	 * 
	 * @param day - Some particular day you want to get the weather information for (starting from 0)
	 * @return - Optional JSONObject with all the weather information on this day (if the day is existent in the response, otherwise empty)
	 */
	public Optional<JSONObject> get(int day) {
		try {
			JSONArray days = body.optJSONArray("days");
			JSONObject dayObject = days.getJSONObject(day);

			return Optional.of(dayObject);
		} catch (JSONException e1) {
			//status = e1;
			log.ifPresent(log -> log.warning("Array index out of bounds: " + String.valueOf(day), e1));
		} catch (NullPointerException e2) {
			//status = e2;
			log.ifPresent(log -> log.warning("Non-existent key \"days\" in response body OR response body absent", e2));
		}
		
		return Optional.empty();
	}
	
	/**
	 * Retrieve some information from the JSON response itself (i.e. not nested somewhere inside)
	 * @param <T> - Type to cast the result to
	 * @param key - Key of the response, containing the result
	 * @param castTo - Class to cast the result to (for safe casting)
	 * @return - Optional value from the key, casted to <T> (if the response isn't corrupted, there is such key, and it can be casted)
	 */
	public <T> Optional<T> get(String key, Class<T> castTo) {
		try {
			Object resultTemp = body.get(key);
			T result = castTo.cast(resultTemp);

			return Optional.of(result);
		} catch (JSONException e1) {
			//status = e1;
			log.ifPresent(log -> log.warning("Key not found: " + key, e1));
		} catch (NullPointerException e2) {
			//status = e2;
			log.ifPresent(log -> log.warning("Non-existent key \"days\" in response body OR response body absent", e2));
		} catch (ClassCastException e3) {
			//status = e3;
			log.ifPresent(log -> log.warning("Class cast failed: " + key, e3));
		}
		
		return Optional.empty();
	}
	
	/**
	 * The current status of the response (see the return value type description)
	 * @return Status of the response
	 */
	public STATUS getStatus() {
		return status;
	}

	/**
	 * Get the time of the last update, to protect the server from DDoS
	 * @return - Time in milliseconds, when the response was last updated
	 */
	public long getCreatedDate() {
		return updatedDate;
	}
	
	/**
	 * Updates the object's state from the JSON response
	 * @param responseBody - JSONObject receiver from the VisualCrossing web service
	 */
	public void update(JSONObject responseBody) {
		status = STATUS.OK;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss a", Locale.US);
		responseBody.put("last_update", dateFormat.format(new Date()));
		
		body = responseBody;
		updatedDate = System.currentTimeMillis();
		
		if (service != null) {
			service.putObject(responseBody.getString("address"), responseBody);
			service.putString("last_location", responseBody.getString("address"));
		}
	}
	
	/**
	 * Same as update(), except does not stop from abusing requests (DDoS attacks) and immediately returns itself.
	 * Used for updating the object from the Database
	 * @param responseBody - JSONObject, which was cached in the database
	 * @return - Itself
	 */
	public WeatherResponse updated(JSONObject responseBody) {
		status = STATUS.OK;
		body = responseBody;
		return this;
	}
	
	/**
	 * At the moment, it simply changes the status to STATUS.FAILURE
	 * @param e - Exception caught during REST API request
	 */
	public void update(Exception e) {
		status = STATUS.FAILURE;
	}
	
	/**
	 * Get a string representation of the JSON response body. Use for caching the response to the Database as a String
	 * @return - Optional, which contains a string representation of the JSON response body (if it is not corrupted/absent)
	 */
	public Optional<String> stringify() {
		try {
			String result = body.toString();
			if (result != null) {
				return Optional.of(result);
			}
		} catch (NullPointerException e) {
			//status = e;
			log.ifPresent(log -> log.warning("Could not cast response body to string", e));
		}
		
		return Optional.empty();
	}
	
	/**
	 * Optionally set a logger
	 * @param logger - a {@link WeatherLogger} instance
	 */
	public void setLogger(WeatherLogger logger) {
		log = Optional.of(logger);
	}
	
	/**
	 * OK - JSON body was not received yet, or is valid <br>
	 * FAILURE - Could not update the JSON response body
	 */
	public enum STATUS {
		OK, FAILURE
	}
}
