package sh.roadmap.tavi.weatherapi.obsolete.tools;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sh.roadmap.tavi.weatherapi.obsolete.logging.WeatherLogger;

/**
 * Used for reading the files from the "resources" directory
 * @see sh.roadmap.tavi.weatherapi.obsolete.tools.SimpleReader
 */
public class ResourcesReader implements FilepathReader {

	private String[] result = new String[0];
	private Optional<WeatherLogger> log = Optional.empty();
	private Optional<FilepathReader> fallback = Optional.empty();
	
	/**
	 * Crates an empty ResourcesReader instance for later re-use
	 */
	public ResourcesReader() {
		
	}
	
	/**
	 * Creates a ResourcesReader instance and tries to {@link #read(String path)} the specified file
	 * @param path - path to the file in the "resources" directory
	 */
	public ResourcesReader(String path) {
		result = read(path);
	}
	
	@Override
	public String[] read() {
		if (result.length == 0 && fallback.isPresent()) {
			return fallback.get().read();
		}
		return result;
	}

	@Override
	public String[] read(String path) {
		try (InputStream is = streamOf(path)) {
			if (is == null) {
				log.ifPresent(log -> log.severe(path, new RuntimeException("File not found")));
				return null;
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				List<String> resultList = new ArrayList<>();
				String line = null;
				while ((line = br.readLine()) != null) {
					resultList.add(line);
				}
				return resultList.toArray(String[]::new);
			}
		} catch (IOException e) {
			log.ifPresent(log -> log.warning(path, e));
			FilepathReader reader = fallback.orElse(null);
			return reader == null ? new String[0] : reader.read(path);
		}
	}

	@Override
	public InputStream streamOf(String path) {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(path);
		if (inputStream == null) {
			inputStream = classLoader.getResourceAsStream("resources/" + path);
			if (inputStream == null) {
				FilepathReader reader = fallback.orElse(null);
				return reader == null ? null : reader.streamOf(path);
			}
		}
		return inputStream;
	}

	@Override
	public void setLogger(WeatherLogger logger) {
		log = Optional.of(logger);
	}

	@Override
	public FilepathReader setFallback(FilepathReader anotherReader) {
		fallback = Optional.of(anotherReader);
		return this;
	}

}
