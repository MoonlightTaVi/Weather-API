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

/**
 * Is used to store meta information into JSON files
 */
public class CacheManager {
	
	private final Logger log = LoggerFactory.getLogger(CacheManager.class);
	
	/** File used to store the meta info */
	private Path path;
	/** JSON body, containing the meta info */
	private JSONObject body = new JSONObject();
	
	/**
	 * Creates an instance of the class that works with a specified file
	 * @param filepath - A path to a file (String), where the request 
	 * results and settings will be stored
	 */
	public CacheManager(final String filepath) {
		try {
			read(filepath);
			log.info("Loaded JSON: {}", filepath);
		} catch (IOException e) {
			log.warn("Could ot read settings from path: {}", filepath);
		}
	}
	
	/**
	 * Tries to read the already existing meta information
	 * @param filepath Reads from this file
	 * @throws IOException If could not read for some reason
	 */
	private void read(final String filepath) throws IOException {
		// Try to read the saved file, if exists
		path = Paths.get(filepath);
		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			try (
					InputStream inStream = Files.newInputStream(path);
					BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
					) {
				final String result = String.join("", reader.lines().toList());
				body = new JSONObject(result);
			} catch (JSONException e) {
				log.warn("Invalid JSON file: {}", filepath);
				Files.delete(path);
			}
		}
	}
	
	/**
	 * Gets a meta info piece from the JSON cache and casts it to the specified type
	 * @param <T> Type to cast the parameter to
	 * @param key Key of the parameter in the JSON body
	 * @param castTo Class to cast the parameter to
	 * @return Optional with the parameter, cast to the specified type
	 * ( {@code Optional.empty() } if could not cast or no such key)
	 */
	public <T> Optional<T> get(final String key, final Class<T> castTo) {
		Optional<T> result = Optional.empty();
		Object obj = null;
		try {
			if (body.has(key)) {
				obj = body.get(key);
				final T cast = castTo.cast(obj);
				result = Optional.of(cast);
			} else {
				log.warn("No such key exists: {}", key);
			}
		} catch (ClassCastException e) {
			if (log.isWarnEnabled()) {
				log.warn("Could not cast parameter with key {} to type {}. Parameter type is {}",
						key,
						castTo.getName(),
						obj.getClass().getName());
			}
		}
		return result;
	}
	
	/**
	 * Puts a specified Object to cache (as a string value)
	 * @param key Key of the relative parameter
	 * @param value Object to cache (must implement a valid {@code toString()} method)
	 */
	public void put(final String key, final Object value) {
		if (body == null) {
			return;
		}
		body.put(key, value.toString());
		log.debug("Put value to JSON: {} = {}", key, value);
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
	private void save(final JSONObject body) {
		try (BufferedWriter out = Files.newBufferedWriter(path)) {
			final String result = body.toString();
			out.write(result);
			if (log.isInfoEnabled()) {
				log.info("Saved to JSON file: {}", path.toFile().getPath());
			}
		} catch (IOException e) {
			if (log.isWarnEnabled()) {
				log.warn("JSON Service was not able to save its body to a file: {}", e.getLocalizedMessage());
			}
		}
	}
	
}
