package sh.roadmap.tavi.weatherapi.tools;


import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * A tool for reading the ".properties" files
 */
public class PropertiesReader {
	
	private Properties properties = new Properties();
	
	private Exception status;
	
	/**
	 * Reads the specified ".properties" file
	 * @param reader - a {@link FilepathReader} instance to read the file
	 * @param pathToFile - a local path to the file inside the "resources" directory
	 */
	public PropertiesReader(FilepathReader reader, String pathToFile) {
		try {
			properties.load(reader.streamOf(pathToFile));
		} catch (IOException e) {
			status = e;
		}
		
	}
	
	/**
	 * Creates an instance of a PropertiesReader directly from InputStream
	 * @param inStream - InputStream of a file with application properties
	 */
	public PropertiesReader(InputStream inStream) {
		try {
			properties.load(inStream);
		} catch (IOException e) {
			status = e;
		}
	}
	
	/**
	 * Returns the value of the specified key inside the ".properties" file, if present
	 * @param key - a property key
	 * @return - Optional, which may contain the value of the key
	 */
	public Optional<String> get(String key) {
		try {
			String result = properties.getProperty(key);
			if (result != null) {
				return Optional.of(result);
			}
		} catch (Exception e) {
			status = e;
		}
		return Optional.empty();
	}
	
	/**
	 * Get the current status
	 * @return - An exception caught during runtime
	 */
	public Exception getStatus() {
		return status;
	}
}
