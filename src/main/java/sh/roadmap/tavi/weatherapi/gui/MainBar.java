package sh.roadmap.tavi.weatherapi.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sh.roadmap.tavi.weatherapi.service.RequestBuilder;

@SuppressWarnings("serial")
public class MainBar extends JPanel implements ActionListener, DocumentListener {
	
	@Autowired
	private ApplicationContext context;
	
	private Logger log = LoggerFactory.getLogger(MainBar.class);
	
	private String location;
	
	private JTextField input = new JTextField();
	private JButton btnSubmit = new JButton("Submit");
	
	private String response = "null";
	
	public MainBar(ApplicationContext context) {
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.setBounds(2, 5, 488, 40);
		this.add(new JLabel("Enter location: "));
		input.setPreferredSize(new Dimension(100, 18));
		this.add(input);
		btnSubmit.setPreferredSize(new Dimension(100, 15));
		this.add(btnSubmit);
		this.setVisible(true);
	}
	
	public String getResponse() {
		return response;
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
		response = context.getBean(RequestBuilder.class)
				.setLocation(location)
				.build();
	}
	
}
