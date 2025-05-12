package sh.roadmap.tavi.weatherapi.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.roadmap.tavi.weatherapi.gui.locale.UiFactory;
import sh.roadmap.tavi.weatherapi.service.RequestBuilder;
import sh.roadmap.tavi.weatherapi.model.WeatherData;

@SuppressWarnings("serial")
public class RequestBar extends JPanel implements ActionListener, DocumentListener {
	
	private UiFactory uiFactory;
	private ResponsePanel response;
	private RequestBuilder requestBuilder;

	private Logger log = LoggerFactory.getLogger(RequestBar.class);
	
	private String location = "";
	
	private JTextField input = new JTextField();
	private JButton btnSubmit;
	
	public RequestBar() {
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.setAlignmentX(CENTER_ALIGNMENT);
	}
	
	public void init() {
		JLabel label = uiFactory.getLabelBold("ui.input-field");
		this.add(label);
		
		input.setPreferredSize(new Dimension(100, 18));
		input.getDocument().addDocumentListener(this);
		this.add(input);
		
		btnSubmit = uiFactory.getButton("ui.submit");
		
		btnSubmit.addActionListener(this);
		this.add(btnSubmit);
		
		this.setVisible(true);
	}
	
	public void setResponse(ResponsePanel response) {
		this.response = response;
	}
	
	public void setUiFactory(UiFactory uiFactory) {
		this.uiFactory = uiFactory;
	}

	public void setRequestBuilder(RequestBuilder requestBuilder) {
		this.requestBuilder = requestBuilder;
	}
	

	@Override
	public void insertUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		update(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		update(e);
	}
	
	private void update(DocumentEvent e) {
		try {
			location = e.getDocument().getText(0, e.getDocument().getLength());
		} catch (BadLocationException exc) {
			// This won't likely ever happen, because we hard-coded the boundaries of the input's text
			log.error("Some unpredicted behaviour: {}", exc.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		log.info("Button clicked");
		log.info("Fetching weather for location: '{}'", location);
		String responseJson = requestBuilder
				.setLocation(location)
				.build();
		response.updateFromData(new WeatherData().fromString(responseJson));
	}
	
}
