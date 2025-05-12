package sh.roadmap.tavi.weatherapi.gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import sh.roadmap.tavi.weatherapi.gui.locale.*;
import sh.roadmap.tavi.weatherapi.model.WeatherData;

@SuppressWarnings("serial")
public class ResponsePanel extends JPanel {
	
	private UiFactory uiFactory;
	private Map<Label, Component> labels = new HashMap<>();
	
	public void init() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.setAlignmentX(CENTER_ALIGNMENT);
		
		setupDay(0, "ui.response.today");
		setupDay(1, "ui.response.tomorrow");
	}
	
	public void setUiFactory(UiFactory uiFactory) {
		this.uiFactory = uiFactory;
	}
	
	public void updateFromData(WeatherData data) {
		for (Label key : labels.keySet()) {
			switch (labels.get(key)) {
			case JLabel label: label.setText(data.getFromDay(key.day, key.type)); break;
			case JTextArea area: area.setText(data.getFromDay(key.day, key.type)); break;
			case JTextPane pane: pane.setText(data.getFromDay(key.day, key.type)); break;
			default: break;
			}
		}
	}
	
	private void setupDay(int day, String daynameRef) {
		this.add(
				getRow(
						uiFactory.getLabelBold(daynameRef),
						getResponseLabel(day, "datetime")
						)
				);
		this.add(
				getRow(
						uiFactory.getLabelBold("ui.response.temp"),
						getResponseLabel(day, "temp"),
						uiFactory.getLabelBold("ui.degrees.us")
						)
				);
		this.add(
				getRow(
						uiFactory.getLabelBold("ui.response.tempmin"),
						getResponseLabel(day, "tempmin"),
						uiFactory.getLabelBold("ui.degrees.us")
						)
				);
		this.add(
				getRow(
						uiFactory.getLabelBold("ui.response.tempmax"),
						getResponseLabel(day, "tempmax"),
						uiFactory.getLabelBold("ui.degrees.us")
						)
				);
		
		this.add(uiFactory.getLabelBold("ui.response.conditions"));
		JTextPane conditions = getResponseTextArea(day, "conditions");
		conditions.setPreferredSize(new Dimension(150, 40));
		this.add(conditions);
		
		this.add(uiFactory.getLabelBold("ui.response.description"));
		JTextPane description = getResponseTextArea(day, "description");
		description.setPreferredSize(new Dimension(150, 40));
		this.add(description);
	}
	
	private JPanel getRow(JLabel... children) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panel.setAlignmentX(CENTER_ALIGNMENT);
		
		for (JLabel child : children) {
			panel.add(child);
		}
		
		panel.setVisible(true);
		return panel;
	}

	private JLabel getResponseLabel(int day, String type) {
		JLabel label = new JLabel("<...>", SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(0));
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setHorizontalAlignment(JLabel.CENTER);
		labels.put(new Label(day, type), label);
		return label;
	}

	private JTextPane getResponseTextArea(int day, String type) {
		JTextPane area = new JTextPane();
		area.setText("<...>");
		
		area.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
		SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        StyledDocument doc = area.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
		
		area.setEditable(false);
		area.setOpaque(false);
		
		labels.put(new Label(day, type), area);
		return area;
	}
	
	record Label(int day, String type) { }
	
}
