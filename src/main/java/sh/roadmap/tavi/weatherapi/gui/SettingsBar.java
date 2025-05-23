package sh.roadmap.tavi.weatherapi.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.*;

import sh.roadmap.tavi.weatherapi.enums.UNIT;
import sh.roadmap.tavi.weatherapi.gui.locale.*;
import sh.roadmap.tavi.weatherapi.service.AppSettings;

@SuppressWarnings("serial")
public class SettingsBar extends JPanel implements ActionListener {
	
	private AppSettings appSettings;
	private LocaleScanner localeScanner;
	private UiObserver uiObserver;
	
	private JComboBox<String> languageBox;
	private JComboBox<UNIT> unitsBox;
	
	public SettingsBar() {
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.setAlignmentX(CENTER_ALIGNMENT);
	}

	public void init() {
		this.setVisible(false);
		
		String[] langs = localeScanner.getLanguages();
		languageBox = new JComboBox<>(langs);
		languageBox.addActionListener(this);
		languageBox.setSelectedItem(
				localeScanner.languageFromLocale(
						appSettings.getLang()
						)
				);
		
		unitsBox = new JComboBox<>(UNIT.values());
		unitsBox.addActionListener(this);
		unitsBox.setSelectedItem(
				appSettings.getUnits()
				);
		
		this.add(languageBox);
		this.add(unitsBox);
		this.setVisible(true);
	}
	
	public void setAppSettings(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

	public void setLocaleScanner(LocaleScanner localeScanner) {
		this.localeScanner = localeScanner;
	}

	public void setUiObserver(UiObserver uiObserver) {
		this.uiObserver = uiObserver;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this.isVisible()) {
			appSettings.setUnits(
					unitsBox.getItemAt(
							unitsBox.getSelectedIndex()
							)
					);
			
			localeScanner.setLocale(
					languageBox.getItemAt(
							languageBox.getSelectedIndex()
							)
					);
			appSettings.setLang(Locale.getDefault().getLanguage());
			uiObserver.update();
		}
	}
	
}
