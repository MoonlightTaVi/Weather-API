package sh.roadmap.tavi.weatherapi.model;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherData {
	
	private static Logger log = LoggerFactory.getLogger(WeatherData.class);
	
	private JSONObject body;
	
	public WeatherData fromString(String jsonString) {
		try {
			body = new JSONObject(jsonString);
		} catch (JSONException e) {
			log.error("Could not parse response, invalid JSON string: {}", jsonString);
		}
		return this;
	}
	
	public String getResolvedAddress() {
		return getParam("resolvedAddress").orElse(null);
	}
	
	public String getAddress() {
		return getParam("address").orElse(null);
	}
	
	public JSONArray getDays() {
		return getParam("days", JSONArray.class).orElse(null);
	}
	
	public JSONObject getDay(int day) {
		JSONArray days;
		if ((days = getDays()) != null) {
			if (day < days.length()) {
				return days.getJSONObject(day);
			}
		}
		return null;
	}
	
	public String getFromDay(int day, String key) {
		JSONObject data;
		if ((data = getDay(day)) != null) {
			return getParam(data, key).orElse(null);
		}
		return null;
	}
	
	
	private <T> Optional<T> getParam(String key, Class<T> castTo) {
		return getParam(body, key, castTo);
	}
	
	private Optional<String> getParam(String key) {
		return getParam(body, key, String.class);
	}
	
	private Optional<String> getParam(JSONObject fromObject, String key) {
		return getParam(fromObject, key, String.class);
	}
	
	private <T> Optional<T> getParam(JSONObject fromObject, String key, Class<T> castTo) {
		Object param = null;
		try {
			param = fromObject.get(key);
			T cast = castTo.cast(param);
			return Optional.of(cast);
		} catch(NullPointerException e) {
			log.error("JSON body is null");
		} catch (JSONException e) {
			log.error("Key does not exist: {}", key);
		} catch (ClassCastException e) {
			if (castTo == String.class) {
				return Optional.of(castTo.cast(String.valueOf(param)));
			}
			log.error("Could not cast param {} of type {} to type {}, param key: {}",
					param.toString(),
					param.getClass().getSimpleName(),
					castTo.getSimpleName(),
					key
					);
		}
		return Optional.empty();
	}
	
}
