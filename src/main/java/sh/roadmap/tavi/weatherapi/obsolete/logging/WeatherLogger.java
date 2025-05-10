package sh.roadmap.tavi.weatherapi.obsolete.logging;


/**
 * A work-around to connect the project to any logger possible (by utilizing Composition principle).
 * All WeatherAPI modules will operate with this logger.
 */
public interface WeatherLogger {
	
	// Set the desired implementation of your logger here
	public static WeatherLogger impl = new BasicWeatherLogger();
	
	/**
	 * Display any message (informational, no exceptions caught)
	 * @param info - Message to display
	 */
	public void info(String info);
	/**
	 * Displays information on a non-critical exception
	 * @param info - Additional message
	 * @param e - Exception caught
	 */
	public void warning(String info, Exception e);
	/**
	 * Displays information on a critical exception
	 * @param info - Additional message
	 * @param e - Exception caught
	 */
	public void severe(String info, Exception e);
	/**
	 * Get the last Exception for the further examination (if ever needed)
	 * @return The last exception, which has been caught during current session
	 */
	public Exception getLastException();
	/**
	 * Closes the FileHandler (if has one), does any other clean-up necessary
	 */
	public void dispose();
	
}
