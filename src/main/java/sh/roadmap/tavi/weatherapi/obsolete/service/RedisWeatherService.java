package sh.roadmap.tavi.weatherapi.obsolete.service;


import java.util.NoSuchElementException;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.InvalidURIException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import sh.roadmap.tavi.weatherapi.obsolete.controller.WeatherResponse;
import sh.roadmap.tavi.weatherapi.obsolete.logging.WeatherLogger;
import sh.roadmap.tavi.weatherapi.obsolete.tools.PropertiesReader;

/**
 * Basic API for working with a Redis database
 */
public class RedisWeatherService implements IWeatherService {
	
	private Optional<WeatherLogger> log = Optional.empty();
	
	private Jedis redis;
	private String ip; // Starts with "http://..."
	private String port; // Redis port
	
	private DBSTATUS status = DBSTATUS.NOT_INITIALIZED;
	
	/**
	 * Creates the service working with Redis
	 * @param props - a {@link PropertiesReader} instance to read the configuration.
	 * ".properties" file must contain such info as "db-ip" and "db-port" for establishing connection
	 */
	public RedisWeatherService(PropertiesReader props) {
		props.get("db-ip").ifPresent(val -> ip = val);
		props.get("db-port").ifPresent(val -> port = val);
		if (ip != null && port != null) {
			try {
				redis = new Jedis(ip + ":" + port);
				status = DBSTATUS.CONNECTED;
			} catch (InvalidURIException e) {
				//lastException = e;
				status = DBSTATUS.FAILED_TO_CONNECT;
				log.ifPresent(log -> log.warning("Could not connect to DB: InvalidURI", e));
			} catch (JedisConnectionException e) {
				//lastException = e;
				status = DBSTATUS.FAILED_TO_CONNECT;
				log.ifPresent(log -> log.warning("Could not connect to DB: Connection exception", e));
			}
		} else {
			redis = null;
		}
	}
	
	@Override
	public WeatherResponse update(WeatherResponse fromResponse) {
		try {
			String address = fromResponse.get("address", String.class).get();
			String response = fromResponse.stringify().get();

			putString("last_location", address); // Last request location
			putString(address, response); // Last request itself
		} catch (NoSuchElementException e) {
			log.ifPresent(log -> log.warning("Could not update DB from response: no address value", e));
			//lastException = e;
			status = DBSTATUS.FAILED_TO_UPDATE;
		}
		
		return fromResponse;
	}
	
	@Override
	public Optional<String> getString(String key) {
		try {
			return Optional.of(redis.get(key));
		} catch (NullPointerException e) {
			//lastException = e;
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<JSONObject> getObject(String key) {
		try {
			return Optional.of(new JSONObject(redis.get(key)));
		} catch (NullPointerException e) {
			//lastException = e;
			return Optional.empty();
		} catch (JSONException e) {
			log.ifPresent(log -> log.warning("Could not get a JSON object from DB: key non-existent or not a JSON object", e));
			//lastException = e;
			return Optional.empty();
		}
	}
	
	@Override
	public void setLogger(WeatherLogger logger) {
		log = Optional.of(logger);
	}
	
	@Override
	public boolean putString(String key, String value) {
		try {
			redis.del(key);
			redis.append(key, value);
			redis.expire(key, 60 * 60 * 12);
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean putObject(String key, JSONObject value) {
		String result;
		try {
			if ((result = value.toString()) == null) {
				return false;
			}
			redis.del(key);
			redis.append(key, result);
			redis.expire(key, 60 * 60 * 12);
		} catch (NullPointerException e) {
			//lastException = e;
			return false;
		}
		return true;
	}
	
	@Override
	public void dispose() {
		if (redis != null) {
			redis.close();
		}
	}
	
	@Override
	public DBSTATUS getStatus() {
		return status;
	}
	
}
