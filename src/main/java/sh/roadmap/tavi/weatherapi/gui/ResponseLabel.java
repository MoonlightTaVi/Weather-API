package sh.roadmap.tavi.weatherapi.gui;

import javax.swing.JLabel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sh.roadmap.tavi.weatherapi.model.WeatherData;
import sh.roadmap.tavi.weatherapi.model.WeatherHtml;

@SuppressWarnings("serial")
public class ResponseLabel extends JLabel {
	
	private MainBar mainBar;

	private WeatherData data = new WeatherData();
	private WeatherHtml html = new WeatherHtml();

	public void setMainBar(MainBar mainBar) {
		this.mainBar = mainBar;
	}

	public void update() {
		this.setText(html.fromWeatherData(data.fromString(mainBar.getResponse())).toString());
	}
	
}
