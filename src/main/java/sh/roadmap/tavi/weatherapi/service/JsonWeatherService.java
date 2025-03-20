package sh.roadmap.tavi.weatherapi.service;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.json.*;

import sh.roadmap.tavi.weatherapi.controller.WeatherResponse;
import sh.roadmap.tavi.weatherapi.logging.WeatherLogger;

public class JsonWeatherService implements IWeatherService {
	
	private static long refreshRate = 12 * 60 * 60 * 1000;
	
	private Path path;
	
	private JSONObject body = new JSONObject();
	private JSONObject timestamps = new JSONObject();
	
	// Used for clearing the old keys at real time
	private RefreshService refreshService = new RefreshService(this);
	
	private Optional<WeatherLogger> log = Optional.empty();
	// Default status is reset if initialization (via constructor) successful
	private DBSTATUS status = DBSTATUS.FAILED_TO_CONNECT;
	
	/**
	 * As an alternative to the {@link ReadisWeatherService}, this service can be used to store
	 * the results of the requests to a JSON file
	 * @param filepath - A path to a file (String), where the request results and settings will be stored
	 * @throws IOException - If a file with the meta information already exists, but not readable / writable
	 */
	public JsonWeatherService(String filepath) throws IOException {
		
		// Try to read the saved file, if exists
		path = Paths.get(filepath);

		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			
			try (InputStream inStream = Files.newInputStream(path)) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
				String result = String.join("", reader.lines().toList());
				JSONObject temp = new JSONObject(result);
				
				if (temp.has("timestamps")) {
					timestamps = temp.getJSONObject("timestamps");
				}
				
				if (temp.has("body")) {
					body = temp.getJSONObject("body");
				}
				
				refresh(System.currentTimeMillis());
				
			} catch (JSONException e) {
				Files.delete(path);
			}
			
		}
		
		status = DBSTATUS.IDLE;
	}

	@Override
	public WeatherResponse update(WeatherResponse fromResponse) {
		try {
			String address = fromResponse.get("address", String.class).get();
			String response = fromResponse.stringify().get();
			body.put("last_location", address); // Last request location
			body.put(address, response); // Last request itself
		} catch (NoSuchElementException e) {
			log.ifPresent(log -> log.warning("Could not update the JSON service body from response: no address value", e));
			status = DBSTATUS.FAILED_TO_UPDATE;
		}
		return fromResponse;
	}

	@Override
	public Optional<String> getString(String key) {
		refreshService.update();
		
		if (body.has(key)) {
			return Optional.of(body.getString(key));
		}
		return Optional.empty();
	}

	@Override
	public Optional<JSONObject> getObject(String key) {
		refreshService.update();
		
		if (body.has(key)) {
			try {
				JSONObject result = body.getJSONObject(key);
				return Optional.of(result);
			} catch (JSONException e) {
				log.ifPresent(log -> log.warning("(JSONService) Key is not a JSONObject: " + key, e));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setLogger(WeatherLogger logger) {
		log = Optional.of(logger);
		logger.info("Json service is set up");
	}

	@Override
	public boolean putString(String key, String value) {
		refreshService.update();
		
		if (body == null) {
			return false;
		}
		
		body.put(key, value);
		timestamps.put(key, System.currentTimeMillis());
		return true;
	}

	@Override
	public boolean putObject(String key, JSONObject value) {
		refreshService.update();
		
		if (body == null) {
			return false;
		}
		
		body.put(key, value);
		timestamps.put(key, System.currentTimeMillis());
		return true;
	}

	@Override
	public void dispose() {
		save();
	}

	@Override
	public DBSTATUS getStatus() {
		return status;
	}
	
	
	/**
	 * Clears all the old keys from the DB
	 * @param time
	 */
	private void refresh(long time) {
		
		// This is sort of slow, but for now it's sufficient
		JSONObject obj = new JSONObject(body.toString());
		
		obj.keys().forEachRemaining(key -> {
			if (timestamps.has(key) && time - timestamps.getLong(key) > JsonWeatherService.refreshRate) {
				body.remove(key);
				timestamps.remove(key);
			}
		});
		
		final int count = obj.length() - body.length();
		
		if (count > 0) {
			log.ifPresent(log -> log.info("Json service performed timed clean-up of old keys; removed: " + count));
			save();
		} else {
			log.ifPresent(log -> log.info("Json service clean-up report: nothing to clean"));
		}
	}
	
	/**
	 * Save the {@link JsonWeatherService.body} to a file on the hard drive
	 */
	private void save() {
		try (BufferedWriter out = Files.newBufferedWriter(path)) {
			JSONObject temp = new JSONObject();
			temp.put("timestamps", timestamps);
			temp.put("body", body);
			
			String result = temp.toString();
			out.write(result);
		} catch (IOException e) {
			log.ifPresent(log -> log.warning("JSON Service was not able to save its body to a file", e));
		}
	}
	
	/**
	 * A delegate, which performs lazy checks of whether it is time to clear all data from file
	 * (mainly used if the Json Service is always running, without closing the application)
	 */
	private class RefreshService {
		private final JsonWeatherService service;
		
		private long lastUpdate;
		
		// Schedule refresh on every 30 minutes (does not mean any key must be cleared;
		// BUT every 30 minutes we check IF there are any keys to be cleared
		private long timedRefresh = 30 * 60 * 1000;
		
		/**
		 * A delegate, which performs lazy checks of whether it is time to clear all data from file
		 * (mainly used if the Json Service is always running, without closing the application)
		 * @param service - JsonWeatherService to clear keys in
		 */
		public RefreshService(JsonWeatherService service) {
			this.service = service;
			lastUpdate = System.currentTimeMillis();
		}
		
		/**
		 * Tests current time against the longest time, acceptable for storing keys.
		 * If this time has ran out, tells its service to clear old keys.
		 */
		public void update() {
			long time = System.currentTimeMillis();
			
			if (time - lastUpdate > timedRefresh) {
				service.refresh(time);
			}
			
			lastUpdate = time;
		}
	}

}
