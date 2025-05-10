package sh.roadmap.tavi.weatherapi;

import sh.roadmap.tavi.weatherapi.service.RequestBuilder;

public class Test {
	
	RequestBuilder request;

	public Test(RequestBuilder request) {
		request.setLocation("moscow").build();
	}
	
}
