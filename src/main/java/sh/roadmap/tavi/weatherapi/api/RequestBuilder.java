package sh.roadmap.tavi.weatherapi.api;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import sh.roadmap.tavi.weatherapi.enums.UNIT;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;

public class RequestBuilder {
	private Logger log = LoggerFactory.getLogger(RequestBuilder.class);
	private WebClient client;
	private RequestConfig config;
	
	private String location;
	private String forDate = "next1days";
	private String units = "us";
	private String lang = "en";
	public RequestBuilder() {
		HttpClient httpClient = HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.responseTimeout(Duration.ofMillis(10000));
		client = WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}
	
	public void setConfig(RequestConfig config) {
		this.config = config;
	}
	
	
	public RequestBuilder setLocation(String location) {
		this.location = location;
		return this;
	}
	public RequestBuilder setUnits(UNIT units) {
		this.units = units.toString().toLowerCase();
		return this;
	}
	public RequestBuilder setLanguage(String language) {
		lang = language;
		return this;
	}
	
	public String build() {
		String uri = config.getUri();
		Map<String, String> params = new HashMap<>();
		params.put("key", config.getApiKey());
		params.put("location", location);
		params.put("forDate", forDate);
		params.put("units", units);
		params.put("lang", lang);
		params.put("include", config.getInclude());
		params.put("elements", config.getElements());
		params.put("contentType", "json");
		
		Mono<String> response = client.get().uri(uri, params)
				.exchangeToMono(r -> {
					if (r.statusCode().is2xxSuccessful()) {
						log.info("Successful request");
						return r.bodyToMono(String.class);
					} else {
						log.error("Failed request, status code: {}", r.statusCode().value());
						return Mono.just(String.format(" { status : \"%d\" } ", r.statusCode().value()));
					}
				});
		return response.block();
	}
	
}
