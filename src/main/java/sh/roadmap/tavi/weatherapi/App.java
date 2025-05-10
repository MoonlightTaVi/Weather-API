package sh.roadmap.tavi.weatherapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import sh.roadmap.tavi.weatherapi.service.RequestBuilder;

@Configuration
@ImportResource("classpath:beans.xml")
public class App {
	
	@Autowired
	Test test;
	
	public static void main(String... args) {
		SpringApplication.run(App.class, args);
	}
	
}
