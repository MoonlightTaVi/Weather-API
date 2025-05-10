package sh.roadmap.tavi.weatherapi.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import sh.roadmap.tavi.weatherapi.gui.locale.UiFactory;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private UiFactory uiFactory;
	private MainBar mainBar;
	private ResponseLabel response;
	
	public MainFrame() {
		this.setTitle("app.title");
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(EXIT_ON_CLOSE);
			}
		});
	}
	
	public void init() {
		uiFactory.getObserver().register(this);
		this.setSize(500, 400);
		this.add(mainBar);
		this.add(response);
		this.setVisible(true);
		uiFactory.getObserver().update();
	}
	
	public void setMainBar(MainBar mainBar) {
		this.mainBar = mainBar;
	}


	public void setResponse(ResponseLabel response) {
		this.response = response;
	}

	public void setUiFactory(UiFactory uiFactory) {
		this.uiFactory = uiFactory;
	}
	
}
