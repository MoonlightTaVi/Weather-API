package sh.roadmap.tavi.weatherapi.service;

import org.springframework.stereotype.Component;

@Component
public class ServiceFactory {

	public RequestBuilder createRequest() {
		return new RequestBuilder();
	}
	
}
