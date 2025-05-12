package sh.roadmap.tavi.weatherapi.gui.locale;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class LocaleScanner {
	
	private Logger log = LoggerFactory.getLogger(LocaleScanner.class);
	private List<String> filenames = new ArrayList<>();
	private List<String> languages = new ArrayList<>();
	private List<String> locales = new ArrayList<>();

	public LocaleScanner() {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			Resource[] resources = resolver.getResources("/**");
			for (Resource resource : resources) {
				String name = resource.getFilename();
				if (name.matches(".*?_[a-zA-Z]{2}.properties")) {
					filenames.add(name);
					ResourceBundle rb = ResourceBundle.getBundle(name.replace(".properties", ""));
					String language = rb.getString("app.lang");
					languages.add(language);
					String locale = rb.getString("app.locale");
					locales.add(locale);
					log.info("Loaded locale: {}, language name: {}, locale: {} {}", name, language, locale, locale.toUpperCase());
				}
			}
		} catch (IOException e) {
			log.error("IOException while trying to retrieve the localization files list ({})", e.getLocalizedMessage());
		}
	}
	
	
	public String[] getLanguages() {
		return languages.toArray(String[]::new);
	}
	
	public void setLocale(String fromLanguageName) {
		String locale = null;
		log.debug("Trying to change locale to {}...", fromLanguageName);
		try {
			locale = locales.get(languages.indexOf(fromLanguageName));
			Locale.setDefault(Locale.of(locale, locale.toUpperCase()));
		} catch (IndexOutOfBoundsException e) {
			log.error("Could not switch app language to {}: language was not specified", fromLanguageName);
		} catch (NullPointerException e) {
			log.error("Could not switch app language to {}: locale {} {}, specified in the .properties, is not valid", fromLanguageName, locale, locale.toUpperCase());
		}
	}
	
}
