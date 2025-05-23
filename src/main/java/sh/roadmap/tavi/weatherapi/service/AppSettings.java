package sh.roadmap.tavi.weatherapi.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.roadmap.tavi.weatherapi.enums.UNIT;

public class AppSettings {
	
	private Logger log = LoggerFactory.getLogger(AppSettings.class);
	
	private CacheManager settingsCache;

	private String lang = "en";
	private UNIT units = UNIT.US;
	
	
	public void init() {
		settingsCache.get("lang", String.class).ifPresent(val -> lang = val);
		settingsCache.get("units", String.class).ifPresent(
				val -> units = UNIT.valueOf(val.toUpperCase())
				);
		Locale.setDefault(Locale.forLanguageTag(lang));
	}
	
	public void setSettingsCache(CacheManager settingsCache) {
		this.settingsCache = settingsCache;
	}

	public String getLang() {
		log.debug("Current language is: {}", lang);
		return lang;
	}
	
	public void setLang(String lang) {
		this.lang = lang;
		settingsCache.put("lang", lang);
	}
	
	public UNIT getUnits() {
		return units;
	}
	
	public void setUnits(UNIT units) {
		this.units = units;
		settingsCache.put("units", units);
	}
	
}
