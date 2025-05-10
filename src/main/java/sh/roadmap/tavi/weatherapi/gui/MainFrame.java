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

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private MainBar mainBar;
	private ResponseLabel response;
	
	public MainFrame() {
		this.setTitle("MyApp");
		//JFrame temp = this;
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//temp.dispose();
				System.exit(EXIT_ON_CLOSE);
			}
		});
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
