package sh.roadmap.tavi.weatherapi.obsolete.tools;


import java.io.InputStream;

import sh.roadmap.tavi.weatherapi.obsolete.logging.WeatherLogger;

/**
 * A simple facade for reading various files as String arrays
 * @see sh.roadmap.tavi.weatherapi.obsolete.tools.SimpleReader SimpleReader to read from the hard drive
 * @see sh.roadmap.tavi.weatherapi.obsolete.tools.ResourcesReader ResourcesReader to read from "resources" 
 */
public interface FilepathReader {
	
	/**
	 * Set an optional WeatherLogger for logging
	 * @param logger - a WeatherLogger instance
	 */
	public void setLogger(WeatherLogger logger);
	
	/**
	 * Get the content of the last read file
	 * @return - A String array with the file content (may be empty, i.e. length=0)
	 */
	public String[] read();
	
	/**
	 * Read the file and get it's content
	 * @param path - A path to the file
	 * @return - A String array with the file content (may be empty, i.e. length=0)
	 */
	public String[] read(String path);
	
	/**
	 * Try to get the InputStream with the file's content to read it later
	 * @param path - A path to the file
	 * @return - InputStream of the specified file
	 */
	public InputStream streamOf(String path);
	
	/**
	 * If an exception occurred when trying to read the file with the current implementation
	 * of the FilepathReader, anotherReader will be used to read the file again
	 * @param anotherReader - A fall-back FilepathReader to try to read the file in another way
	 * @return - Itself, to call at the initialization
	 */
	public FilepathReader setFallback(FilepathReader anotherReader);
}
