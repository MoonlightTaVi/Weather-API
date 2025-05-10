package sh.roadmap.tavi.weatherapi.obsolete.logging;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BasicWeatherLogger implements WeatherLogger {
	
	private static final Logger log = Logger.getLogger("Weather API");
	private volatile FileHandler fh;
	
	private Exception lastException;
	
	public BasicWeatherLogger() {
		addHandler();
	}
	
	@Override
	public void info(String info) {
		synchronized(fh) {
			//addHandler();
			log.info(info);
		}
	}
	
	@Override
	public void warning(String info, Exception e) {
		synchronized(fh) {
			//addHandler();
			lastException = e == null ? lastException : e;
			
			if (info != null && e != null) {
				log.info(info);
				log.warning(e.getLocalizedMessage());
				return;
			}
			
			if (info == null && e == null) {
				return;
			}
			
			String message = info != null ? info : e.getLocalizedMessage();
			log.warning(message);
		}
	}
	
	@Override
	public void severe(String info, Exception e) {
		synchronized(fh) {
			//addHandler();
			lastException = e == null ? lastException : e;
			
			if (info != null && e != null) {
				log.info(info);
				log.severe(e.getLocalizedMessage());
				return;
			}
			
			if (info == null && e == null) {
				return;
			}
			
			String message = info != null ? info : e.getLocalizedMessage();
			log.severe(message);
		}
	}

	@Override
	public Exception getLastException() {
		return lastException;
	}

	@Override
	public void dispose() {
		if (fh != null) {
			fh.close();
		}
	}
	
	private void addHandler() {
		try {
			//String datePattern = "yyyyMMddhhmmss";
			//SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
			
			//fh = new FileHandler("log-" + simpleDateFormat.format(new Date()) + ".log");
			fh = new FileHandler("weather-api.log");
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			log.addHandler(fh);
		} catch (SecurityException e) {
			log.info("SecurityException when creating a file handler for the logger.");
			log.warning(e.getLocalizedMessage());
			fh.close();
		} catch (IOException e) {
			log.info("IOException when creating a file handler for the logger.");
			log.warning(e.getLocalizedMessage());
			fh.close();
			//e.printStackTrace();
		}
	}
}
