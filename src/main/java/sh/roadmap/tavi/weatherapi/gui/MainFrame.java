package sh.roadmap.tavi.weatherapi.gui;

import javax.swing.JFrame;

import org.springframework.stereotype.Component;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {


	private MainBar mainBar;
	private ResponseLabel response;
	
	public MainFrame() {
		this.setTitle("MyApp");
	}
	
	public void setMainBar(MainBar mainBar) {
		this.mainBar = mainBar;
	}


	public void setResponse(ResponseLabel response) {
		this.response = response;
	}
	
	public void init() {
		this.setSize(500, 400);
		this.add(mainBar);
		this.add(response);
		this.setVisible(true);
	}
	
}
