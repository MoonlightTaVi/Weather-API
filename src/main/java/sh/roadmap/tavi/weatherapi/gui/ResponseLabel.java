package sh.roadmap.tavi.weatherapi.gui;

import javax.swing.JLabel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sh.roadmap.tavi.weatherapi.model.WeatherData;
import sh.roadmap.tavi.weatherapi.model.WeatherHtml;

@SuppressWarnings("serial")
public class ResponseLabel extends JLabel {

	private WeatherData data = new WeatherData();
	private WeatherHtml html = new WeatherHtml();
	
	public ResponseLabel() {
		this.setVisible(true);
	}

	public void update(String fromString) {
		this.setText(html.fromWeatherData(data.fromString(fromString)).toString());
	}
	
}
