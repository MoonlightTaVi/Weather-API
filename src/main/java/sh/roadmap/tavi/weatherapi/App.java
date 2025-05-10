package sh.roadmap.tavi.weatherapi;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import sh.roadmap.tavi.weatherapi.gui.MainFrame;
import sh.roadmap.tavi.weatherapi.service.RequestBuilder;

@Configuration
@ImportResource("classpath:beans.xml")
public class App {
	
	@Autowired
	private MainFrame appFrame;
	
	public static void main(String... args) {
		System.setProperty("java.awt.headless", "false");
		Locale.setDefault(Locale.of("en", "EN"));
		SpringApplication.run(App.class, args);
	}
	
}
