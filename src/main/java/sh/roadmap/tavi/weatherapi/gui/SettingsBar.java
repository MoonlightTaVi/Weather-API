package sh.roadmap.tavi.weatherapi.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import sh.roadmap.tavi.weatherapi.gui.locale.*;

@SuppressWarnings("serial")
public class SettingsBar extends JPanel implements ActionListener {
	
	private LocaleScanner localeScanner;
	private UiObserver uiObserver;
	
	private JComboBox<String> comboBox;
	
	public SettingsBar() {
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.setAlignmentX(CENTER_ALIGNMENT);
	}

	public void init() {
		String[] langs = localeScanner.getLanguages();
		comboBox = new JComboBox<>(langs);
		comboBox.addActionListener(this);
		this.add(comboBox);
		this.setVisible(true);
	}
	
	public void setLocaleScanner(LocaleScanner localeScanner) {
		this.localeScanner = localeScanner;
	}

	public void setUiObserver(UiObserver uiObserver) {
		this.uiObserver = uiObserver;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		localeScanner.setLocale(
				comboBox.getItemAt(
						comboBox.getSelectedIndex()
						)
				);
		uiObserver.update();
	}
	
}
