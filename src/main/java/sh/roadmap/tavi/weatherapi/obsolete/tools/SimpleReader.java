package sh.roadmap.tavi.weatherapi.obsolete.tools;


import java.io.*;
import java.util.*;

import sh.roadmap.tavi.weatherapi.obsolete.logging.WeatherLogger;

/**
 * Reads a file from a hard drive (not in the "resources" directory)
 * @see sh.roadmap.tavi.weatherapi.obsolete.tools.ResourcesReader
 */
public class SimpleReader implements FilepathReader {
	
	private String[] result = new String[0];
	private Optional<WeatherLogger> log = Optional.empty();
	private Optional<FilepathReader> fallback = Optional.empty();
	
	/**
	 * Crates an empty SimpleReader instance for later re-use
	 */
	public SimpleReader() {
		
	}

	/**
	 * Creates a SimpleReader instance and tries to {@link #read(String path)} the specified file
	 * @param path - path to the file on a hard drive
	 */
	public SimpleReader(String path) {
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
		try (FileReader fr = new FileReader(path)) {
			try (BufferedReader br = new BufferedReader(fr)) {
				List<String> resultList = new ArrayList<>();
				String line = null;
				while ((line = br.readLine()) != null) {
					resultList.add(line);
				}
				return resultList.toArray(String[]::new);
			}
		} catch (IOException e) {
			log.ifPresent(log -> log.warning("File name: " + path, e));
			FilepathReader reader = fallback.orElse(null);
			return reader == null ? new String[0] : reader.read(path);
		}
	}

	@Override
	public InputStream streamOf(String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			return fis;
		} catch (FileNotFoundException e) {
			FilepathReader reader = fallback.orElse(null);
			if (reader != null) {
				return reader.streamOf(path);
			}
			log.ifPresent(log -> log.severe("File name: " + path, e));
		}
		return null;
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
