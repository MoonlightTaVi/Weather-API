package sh.roadmap.tavi.weatherapi.gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import sh.roadmap.tavi.weatherapi.enums.UNIT;
import sh.roadmap.tavi.weatherapi.gui.locale.*;
import sh.roadmap.tavi.weatherapi.model.WeatherData;
import sh.roadmap.tavi.weatherapi.service.AppSettings;

@SuppressWarnings("serial")
public class ResponsePanel extends JPanel {
	private AppSettings appSettings;
	private UiObserver uiObserver;
	private UiFactory uiFactory;
	private Map<Label, Component> labels = new HashMap<>();
	private JTextPane address;
	private JLabel updated;
	
	public void init() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.setAlignmentX(CENTER_ALIGNMENT);


		this.add(
				getRow(
						uiFactory.getLabelBig("ui.response.updated"),
						getUpdated()
						)
				);
		this.add(getAddress());
		
		setupDay(0, "ui.response.today");
		setupDay(1, "ui.response.tomorrow");
		uiObserver.customUpdate();
	}
	
	public void setUiFactory(UiFactory uiFactory) {
		this.uiFactory = uiFactory;
	}
	
	public void setUiObserver(UiObserver uiObserver) {
		this.uiObserver = uiObserver;
	}

	public void setAppSettings(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

	public void updateFromData(WeatherData data) {
		uiObserver.customUpdate();
		address.setText(data.getResolvedAddress());
		updated.setText(data.getTimestamp());
		
		for (Label key : labels.keySet()) {
			switch (labels.get(key)) {
			case JLabel label:
				label.setText(data.getFromDay(key.day, key.type));
				break;
			case JTextArea area:
				area.setText(data.getFromDay(key.day, key.type));
				break;
			case JTextPane pane:
				pane.setText(data.getFromDay(key.day, key.type));
				break;
			default: break;
			}
		}
	}
	
	
	private void setupDay(int day, String daynameRef) {
		this.add(
				getRow(
						uiFactory.getLabelBig(daynameRef),
						getResponseLabel(day, "datetime")
						)
				);
		this.add(
				getRow(
						uiFactory.getLabelBold("ui.response.temp"),
						getResponseLabel(day, "temp"),
						getDegreesLabel()
						)
				);
		this.add(
				getRow(
						uiFactory.getLabelBold("ui.response.tempmin"),
						getResponseLabel(day, "tempmin"),
						getDegreesLabel()
						)
				);
		this.add(
				getRow(
						uiFactory.getLabelBold("ui.response.tempmax"),
						getResponseLabel(day, "tempmax"),
						getDegreesLabel()
						)
				);
		
		this.add(uiFactory.getLabelBold("ui.response.conditions"));
		JTextPane conditions = getResponseTextArea(day, "conditions");
		conditions.setFont(conditions.getFont().deriveFont(Font.ITALIC));
		conditions.setPreferredSize(new Dimension(300, 24));
		this.add(conditions);
		
		this.add(uiFactory.getLabelBold("ui.response.description"));
		JTextPane description = getResponseTextArea(day, "description");
		description.setPreferredSize(new Dimension(300, 37));
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
	
	private JLabel getUpdated() {
		updated = new JLabel("<...>", SwingConstants.CENTER);
		updated.setFont(updated.getFont().deriveFont(0));
		updated.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		updated.setHorizontalAlignment(JLabel.CENTER);
		return updated;
	}
	
	private JTextPane getAddress() {
		address = new JTextPane();
		address.setText("Waiting...");
		address.setPreferredSize(new Dimension(300, 37));
		address.setFont(address.getFont().deriveFont(Font.ITALIC));
		address.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        StyledDocument doc = address.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
		
        address.setEditable(false);
        address.setOpaque(false);
		
		return address;
	}

	private JLabel getResponseLabel(int day, String type) {
		JLabel label = new JLabel("<...>", SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(0));
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setHorizontalAlignment(JLabel.CENTER);
		labels.put(new Label(day, type), label);
		return label;
	}
	
	private JLabel getDegreesLabel() {
		JLabel label = new JLabel();
		uiObserver.registerCustom(() -> {
			switch (appSettings.getUnits()) {
			case UNIT.BASE:
				label.setText("°K");
				break;
			case UNIT.US:
				label.setText("°F");
				break;
			default:
				label.setText("°C");
			}
			});
		return label;
	}

	private JTextPane getResponseTextArea(int day, String type) {
		JTextPane pane = new JTextPane();
		pane.setText("<...>");
		
		pane.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
		SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        StyledDocument doc = pane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
		
		pane.setEditable(false);
		pane.setOpaque(false);
		
		labels.put(new Label(day, type), pane);
		return pane;
	}
	
	
	record Label(int day, String type) { }
	
}
