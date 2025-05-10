package sh.roadmap.tavi.weatherapi.obsolete.service;

import java.util.Optional;

import org.json.JSONObject;

import sh.roadmap.tavi.weatherapi.obsolete.controller.WeatherResponse;
import sh.roadmap.tavi.weatherapi.obsolete.logging.WeatherLogger;

public interface IWeatherService {
	/**
	 * Automatically stores the response in the DB
	 * @param fromResponse - WeatherResponse with all the data to store (last update, response body, etc.)
	 * @return - The "fromResponse" itself again
	 */
	public WeatherResponse update(WeatherResponse fromResponse);
	
	/**
	 * Get a raw string from the DB
	 * @param key - Key, corresponding to the desired value
	 * @return - Optional, containing the value (if could retrieve)
	 */
	public Optional<String> getString(String key);
	
	/**
	 * Get a string from the DB, casted to JSONObject
	 * @param key - Key, corresponding to the desired value
	 * @return - Optional JSONObject, containing the value (if could retrieve and cast the string to JSONObject)
	 */
	public Optional<JSONObject> getObject(String key);
	
	/**
	 * Optionally set a logger
	 * @param logger - a {@link WeatherLogger} instance
	 */
	public void setLogger(WeatherLogger logger);
	
	/**
	 * Store a String value in the DB (if connected)
	 * @param key - Key to store the value to
	 * @param value - Value to store to the DB
	 * @return - true if connected to the DB, false otherwise
	 */
	public boolean putString(String key, String value);
	
	/**
	 * Store a String value in the DB (if connected), which is extracted from the JSONObject (by stringifying)
	 * @param key - Key to store the value to
	 * @param value - JSON value to store to the DB
	 * @return - true if connected to the DB and the value is valid, false otherwise
	 */
	public boolean putObject(String key, JSONObject value);
	
	/**
	 * Closes the connection to the Database (if it was open)
	 */
	public void dispose();
	
	/**
	 * Get the status of the connection
	 * @return - Status enum (look at the type itself for the info).
	 * @see DBSTATUS
	 */
	public DBSTATUS getStatus();
}
