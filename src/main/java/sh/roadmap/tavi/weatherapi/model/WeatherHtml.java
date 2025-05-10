package sh.roadmap.tavi.weatherapi.model;

public class WeatherHtml {
	
	private WeatherData data;

	
	public WeatherHtml() { }
	public WeatherHtml(WeatherData data) {
		fromWeatherData(data);
	}
	
	public WeatherHtml fromWeatherData(WeatherData data) {
		this.data = data;
		return this;
	}
	
	
	private String tag(String tag, String text) {
		return String.format("<%s>%s</%s>", tag, text, tag);
	}
	
	private String ofDay(int day, String dayName) {
		String header = tag("b", dayName);;
		String date = tag("i", data.getFromDay(day, "datetime"));
		String t = data.getFromDay(day, "temp");
		String tMin = data.getFromDay(day, "tempmin");
		String tMax = data.getFromDay(day, "tempmax");
		String conditions = tag("i", data.getFromDay(day, "conditions"));
		String description = data.getFromDay(day, "description");
		return String.format("%s (%s) <br> %s <br> %s <br> %s <br> %s <br> %s",
				header,
				date,
				t,
				tMin,
				tMax,
				conditions,
				description
				);
	}
	
	private String today() {
		return ofDay(0, "Today");
	}
	
	private String tommorow() {
		return ofDay(1, "Tommorow");
	}
	
	
	@Override
	public String toString() {
		try {
		return String.format("<html>%s<br>%s<br>%s</html>",
				tag(
						"b",
						tag(
								"i",
								data.getResolvedAddress()
								)
						),
				today(),
				tommorow()
				);
		} catch (NullPointerException e) {
			return e.getLocalizedMessage();
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
	}
	
}
