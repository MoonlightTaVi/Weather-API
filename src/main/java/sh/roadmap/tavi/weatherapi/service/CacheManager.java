package sh.roadmap.tavi.weatherapi.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheManager {
	
	private Logger log = LoggerFactory.getLogger(CacheManager.class);
	
	private Path path;
	private JSONObject body = new JSONObject();
	
	/**
	 * As an alternative to the {@link ReadisWeatherService}, this service can be used to store
	 * the results of the requests to a JSON file
	 * @param filepath - A path to a file (String), where the request results and settings will be stored
	 */
	public CacheManager(String filepath) {
		try {
			read(filepath);
			log.info("Loaded JSON: {}", filepath);
		} catch (IOException e) {
			log.warn("Could ot read settings from path: {}", filepath);
		}
	}
	
	private void read(String filepath) throws IOException {

		// Try to read the saved file, if exists
		path = Paths.get(filepath);

		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			
			try (InputStream inStream = Files.newInputStream(path)) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
				String result = String.join("", reader.lines().toList());
				body = new JSONObject(result);
			} catch (JSONException e) {
				log.warn("Invalid JSON file: {}", filepath);
				Files.delete(path);
			}
			
		}
	}

	public Optional<String> getString(String key) {
		if (body.has(key)) {
			return Optional.of(body.getString(key));
		}
		return Optional.empty();
	}

	public Optional<JSONObject> getObject(String key) {
		if (body.has(key)) {
			try {
				JSONObject result = body.getJSONObject(key);
				return Optional.of(result);
			} catch (JSONException e) {
				log.warn("Key is not a JSONObject: {}", key);
			}
		}
		return Optional.empty();
	}

	public boolean putString(String key, String value) {
		if (body == null) {
			return false;
		}
		body.put(key, value);
		log.debug("Put value to JSON: {} = {}", key, value);
		return true;
	}

	public boolean putObject(String key, JSONObject value) {
		if (body == null) {
			return false;
		}
		body.put(key, value);
		log.debug("Put object to JSON, key: {}", key);
		return true;
	}

	/**
	 * Called on application close
	 */
	public void dispose() {
		save(body);
	}
	
	/**
	 * Save the meta data to a file on the hard drive
	 */
	private void save(JSONObject body) {
		try (BufferedWriter out = Files.newBufferedWriter(path)) {
			String result = body.toString();
			out.write(result);
			log.info("Saved to JSON file: {}", path.toFile().getPath());
		} catch (IOException e) {
			log.warn("JSON Service was not able to save its body to a file: {}", e.getLocalizedMessage());
		}
	}
	
}
