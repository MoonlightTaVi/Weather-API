package sh.roadmap.tavi.weatherapi.gui;

import java.awt.*;

import javax.swing.*;

import sh.roadmap.tavi.weatherapi.gui.locale.*;

@SuppressWarnings("serial")
public class AppFrame extends JFrame {

	private UiObserver uiObserver;
	private RequestBar requestBar;
	private SettingsBar settingsBar;
	private ResponsePanel response;
	
	public AppFrame() {
		this.setTitle("app.title");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void init() {
		uiObserver.register(this);
		
		this.setLayout(new BorderLayout(5, 5));
		
		this.getContentPane().add(requestBar, BorderLayout.NORTH);
		this.getContentPane().add(response, BorderLayout.CENTER);
		this.getContentPane().add(settingsBar, BorderLayout.SOUTH);
		
		this.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - this.getWidth()) / 2;
		int y = (screenSize.height - this.getHeight()) / 2;
		this.setLocation(x, y);
		
		this.setVisible(true);
	}
	
	public void setSettingsBar(SettingsBar settingsBar) {
		this.settingsBar = settingsBar;
	}
	
	public void setRequestBar(RequestBar requestBar) {
		this.requestBar = requestBar;
	}


	public void setResponse(ResponsePanel response) {
		this.response = response;
	}

	public void setUiObserver(UiObserver uiObserver) {
		this.uiObserver = uiObserver;
	}
	
}
