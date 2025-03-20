package sh.roadmap.tavi.weatherapi.view;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import sh.roadmap.tavi.weatherapi.controller.WeatherController;
import sh.roadmap.tavi.weatherapi.controller.WeatherResponse;
import sh.roadmap.tavi.weatherapi.service.IWeatherService;
import sh.roadmap.tavi.weatherapi.tools.CsvHandler;

/**
 * The all-in-one class for creating a window frame, serves as an example of the use of the WeatherAPI.
 * May seem a little bit complex (has a few responsibilities at the same time - setting up frame and panels,
 * listening to the input, etc.), but splitting it up is some unnecessary overhead, because it's just an example).
 */

public class WeatherApplication implements ActionListener, DocumentListener {
	
	private WeatherController api; // REST controller
	private WeatherResponse response = new WeatherResponse(); // Response body / (*) Tight coupling here!
	private CsvHandler locale; // Custom translations of the UI to different languages
	private IWeatherService service; // Database service (optional)

	private String location = ""; // Last location from user input
	private String lang = "en"; // Current UI language

	// The following Sets are used as Observers for updating the UI
	private Set<UiComponent<?>> uiListeners = new LinkedHashSet<>(); // Main UI (buttons, switches, etc.)
	private Set<UiComponent<?>> uiListenersStatus = new LinkedHashSet<>(); // Application status (on first start OR at exception)
	private Set<UiComponent<?>> uiListenersResponse = new LinkedHashSet<>(); // Received response
	
	// Panels in the middle part of screen
	private JPanel statusPanel = new JPanel();
	private JPanel responsePanel = new JPanel();
	// Corresponding (to language and unit system) drop-down menus
	private JComboBox<String> langSwitch;
	private JComboBox<String> unitsSwitch;
	
	// Status of the App (default to "waiting user's submit action")
	private STATUS status = STATUS.WAITING_SUBMIT;
	
	// A thread for fetching the data from the server
	private Thread connectionThread = null;
	
	/**
	 * All-in-one graphical representation of the use of the WeatherAPI (basic application)
	 * @param csvLocale - .csv (Comma-Separated-Value) file with localization
	 */
	public WeatherApplication(CsvHandler csvHandler) {
		locale = csvHandler;
	}
	
	
	/**
	 * It is necessary to call this method to actually create a window. It will do everything.
	 * Must be called AFTER everything else (like injecting the Database service and the REST controller)
	 */
	public void setupFrame() {
		// A window with a title
		JFrame frame = new JFrame("Weather API by TaVi - get a short weather forecast");

		// A panel with an input field and a button
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		topPanel.setBounds(2, 5, 488, 40);
		
		// Label with a tip for a user
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(locale.get("ui_enter_city", lang)))
		.apply(field -> field.setMaximumSize(new Dimension(80, 40)))
		.addToPanel(topPanel)
		.subscribe(uiListeners);
		
		// Input field for desired location
		new UiComponent<JTextField>(new JTextField())
		.setDefaultText(location)
		.setTextBehaviour((field, str) -> field.setText(location))
		.addToPanel(topPanel)
		.subscribe(uiListeners)
		.apply(field -> field.setPreferredSize(new Dimension(100, 18)))
		.apply(field -> field.getDocument().addDocumentListener(this));
		
		// Submit/update button
		new UiComponent<JButton>(new JButton())
		.setTextBehaviour((button, str) -> button.setText(locale.get("btn_submit", lang)))
		.apply(button -> button.setPreferredSize(new Dimension(100, 15)))
		.apply(button -> button.addActionListener(this))
		.addToPanel(topPanel)
		.subscribe(uiListeners);
		
		// A panel with language/unit system settings
		JPanel bottomPanel = new JPanel();
		GridLayout bottomLayout = new GridLayout(1,0);
		bottomLayout.setHgap(10);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		bottomPanel.setBounds(40, 320, 395, 30);
		
		// Languages are automatically collected from the .csv file
		String[] langs = locale.getColumns(); // Abbreviations
		// Map abbreviations to the actual (human-readable) names of the languages
		String[] langNames = Arrays.stream(langs).map(abbreviration -> locale.get("ui_lang", abbreviration)).toArray(String[]::new);
		
		// Tip of the setting
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(locale.get("ui_lang_label", lang)))
		.apply(label -> label.setPreferredSize(new Dimension(60, 20)))
		.apply(label -> label.setHorizontalAlignment(SwingConstants.RIGHT))
		.addToPanel(bottomPanel)
		.subscribe(uiListeners);
		// Menu
		new UiComponent<JComboBox<String>>(new JComboBox<>(langNames))
		.apply(box -> box.addActionListener(this))
		.apply(box -> langSwitch = box)
		.addToPanel(bottomPanel)
		.subscribe(uiListeners);
		// Tip of the setting
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(locale.get("ui_units_label", lang)))
		.apply(label -> label.setPreferredSize(new Dimension(100, 20)))
		.apply(label -> label.setHorizontalAlignment(SwingConstants.RIGHT))
		.addToPanel(bottomPanel)
		.subscribe(uiListeners);
		// Menu / (**) no locale changes here yet
		new UiComponent<JComboBox<String>>(new JComboBox<>(new String[] {"Metric", "US", "UK", "Base"}))
		.apply(box -> box.addActionListener(this))
		.apply(box -> unitsSwitch = box)
		.addToPanel(bottomPanel)
		.subscribe(uiListeners);
		
		// Subtasks for the middle panels
		setupStatus();
		setupResponse();
		
		frame.setSize(500, 400);
		frame.add(topPanel);
		frame.add(bottomPanel);
		
		frame.add(statusPanel);
		frame.add(responsePanel);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) { 	// We need to:
				api.dispose();							// Close DB and Logger (if opened)
				frame.dispose();						// And finally close the window afterwards
			}											// When user quits !!!
		});
		
		frame.setLayout(null);
		frame.setVisible(true);
		
		// Show last response from DB on start, if connected
		if (service != null && service.getString("last_location").isPresent()) {
			// Retrieve lastly used language (goes tight along with the "last_location")
			service.getString("lang").ifPresent(str -> {
				lang = str;
				for (int i = 0; i < langSwitch.getItemCount(); i++) {
					if (langSwitch.getItemAt(i).equals(locale.get("ui_lang", lang))) {
						langSwitch.setSelectedIndex(i);
						break;
					}
				}
				if (api != null) {
					api.setLang(str);
				}
			});
			// Retrieve lastly used unit system (goes tight along with the "last_location")
			service.getString("units").ifPresent(str -> {
				String units = "Metric";
				switch (str) {
				case "base":
					units = "Base";
					break;
				case "uk":
					units = "UK";
					break;
				case "us":
					units = "US";
				}
				for (int i = 0; i < unitsSwitch.getItemCount(); i++) {
					if (unitsSwitch.getItemAt(i).equals(units)) {
						unitsSwitch.setSelectedIndex(i);
						break;
					}
				}
				if (api != null) {
					switch (str) {
					case "base":
						api.setBaseUnits();
						break;
					case "uk":
						api.setUkUnits();
						break;
					case "us":
						api.setUsUnits();
						break;
					default:
						api.setMetric();
					}
				}
			});
						
			service.getString("last_location").ifPresent(loc -> {
				location = loc;
				service.getObject(loc).ifPresent(obj -> update(response.updated(obj)));
			});
		} else {
			update(); // Or basically show app status
		}
	}
	
	/**
	 * (Necessary) Set the REST controller
	 * @param api - REST controller of the WeatherAPI
	 */
	public void setApi(WeatherController api) {
		this.api = api;
	}


	/**
	 * (Optional) Set the DB service (for caching).
	 * The application can work without that with no problems
	 * (except for creating a log file every time)
	 * @param service - Jedis (Redis) or JSON service facade for the WeatherAPI
	 */
	public void setService(IWeatherService service) {
		this.service = service;
	}


	/**
	 * Updates the text on the screen (when changing language OR catching exceptions).
	 * Used at the first start (when there's no response received yet) or at failure.
	 */
	public void update() {
		// Basic UI
		for (UiComponent<?> component : uiListeners) {
			component.update();
		}
		// Status UI
		for (UiComponent<?> component : uiListenersStatus) {
			component.update();
		}

		// Switch between "tabs"
		responsePanel.setVisible(false);
		statusPanel.setVisible(true);
	}
	
	
	/**
	 * Updates the text on the screen (when successfully received the response from the server)
	 * @param response - The response from the server, containing the information on weather)
	 */
	public void update(WeatherResponse response) {
		this.response = response;
		// Fall back to the status tab if the response is corrupted
		if (response.getStatus() == WeatherResponse.STATUS.FAILURE) {
			status = STATUS.WAITING_SUBMIT;
			update();
			return;
		}
		// Basic UI
		for (UiComponent<?> component : uiListeners) {
			component.update();
		}
		// Response UI
		for (UiComponent<?> component : uiListenersResponse) {
			component.update();
		}

		// Switch between "tabs"
		statusPanel.setVisible(false);
		responsePanel.setVisible(true);
	}
	
	/**
	 * Application status <br>
	 * WAITING_SUBMIT - Print "Please enter location..." on the screen <br>
	 * WAITING_RESPONSE - Print "Please wait, connecting to server..." on the screen
	 */
	public enum STATUS {
		WAITING_SUBMIT,
		WAITING_RESPONSE
	}
	
	/*
	 * === INPUT CONTROLS ===
	 */
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) { // Menus interaction
			if (e.getSource().equals(unitsSwitch)) {
				// Change unit system (for the next request!)
				switch (unitsSwitch.getSelectedItem().toString()) {
					case "Metric":
						api.setMetric();
						break;
					case "US":
						api.setUsUnits();
						break;
					case "UK":
						api.setUkUnits();
						break;
					case "Base":
						api.setBaseUnits();
						break;
				}
			}
			if (e.getSource().equals(langSwitch)) {
				// Change UI language
				lang = locale.getColumnOf("ui_lang", langSwitch.getSelectedItem().toString());
				api.setLang(lang);
				update(); // Simply update and quit method
			}
			return;
		}
		
		// IF IT'S THE "SUBMIT/UPDATE" BUTTON INTERACTION
		
		// Stop user from spamming the button
		if (connectionThread != null) {
			if (connectionThread.isAlive()) {
				return;
			}
			connectionThread = null;
		}
		
		// Change the status and update it on the screen
		status = STATUS.WAITING_RESPONSE;
		update();
		
		// For the status label changes to actually take place (on the screen)
		// we need to wait for the response from the server on a different thread
		
		connectionThread = new Thread() {
			@Override
			public void run() {
				// Fetch the data for the set "location", for today and tomorrow
				update(api.sendRequest(location, "next1days"));
				status = STATUS.WAITING_SUBMIT;
			}
		};
		
		connectionThread.start();
	}

	/*
	 * === INPUT FIELD CHANGES ===
	 */
	
	@Override
	public void insertUpdate(DocumentEvent e) {
		updateInput(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateInput(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateInput(e);
	}
	
	/**
	 * Receives any changes to the input field, changes the "location" variable correspondingly
	 * @param e - The document, relative to the input field
	 */
	private void updateInput(DocumentEvent e) {
		try {
			location = e.getDocument().getText(0, e.getDocument().getLength());
		} catch (BadLocationException e1) {
			// This won't likely ever happen, because we hard-coded the boundaries of the input's text
			System.err.println("Well, this happened.. (SubmitButton.insertUpdate)");
			e1.printStackTrace();
		}
	}
	
	/*
	 * === SUB-TASKS FOR UI CREATION ===
	 */
	
	/**
	 * Sets up the status panel/tab, which shows the information on the database
	 * status, the REST API status and the "press update"/"wait for response" status
	 */
	private void setupStatus() {
		statusPanel.setBounds(20, 40, 480, 260);
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
		statusPanel.setVisible(true);
		
		Supplier<String> FONT_110 = () -> "<span style=\"font-size:110%\">{...}</span>";
		Supplier<String> BOLD = () -> "<b>{...}</b>";
		Supplier<String> HTML = () -> "<html>{...}</html>"; // Main tag for the other tags
		Consumer<JLabel> SET_UP_LABEL = label -> {
			label.setFont(label.getFont().deriveFont(0)); // Otherwise the font is always bold
			label.setMaximumSize(new Dimension(400, 40)); // Otherwise the label isn't broken on several lines
			};
		
		// The further code needs no explaining?..
		
		Supplier<String> API_STATUS = () -> {
			String result;
			switch (this.api.getStatus()) {
			case FAILED_TO_INITIALIZE:
				result = locale.get("ui_controller_fail", lang);
				break;
			case FAILED_TO_FETCH:
				result = locale.get("ui_controller_server_fail", lang);
				break;
			default:
				result = locale.get("ui_controller_ok", lang);
			}
			return result;
		};
		
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("{ui_controller_status}: {status}")
		.addReplacement("SIZE", FONT_110)
		.addReplacement("BOLD", BOLD)
		.addReplacement("HTML", HTML) // HTML last !!!
		.addReplacement("{ui_controller_status}", () -> locale.get("ui_controller_status", lang))
		.addReplacement("{status}", API_STATUS)
		.apply(SET_UP_LABEL)
		.addToPanel(statusPanel)
		.subscribe(uiListenersStatus);
		
		
		Supplier<String> SERVICE_STATUS = () -> {
			String result;
			switch (service.getStatus()) {
			case CONNECTED:
				result = locale.get("ui_db_status_connected", lang);
				break;
			case IDLE:
				result = locale.get("ui_db_status_idle", lang);
				break;
			default:
				result = locale.get("ui_db_status_failed", lang);
			}
			return result;
		};
		
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("{ui_db_status}: {status}")
		.addReplacement("SIZE", FONT_110)
		.addReplacement("BOLD", BOLD)
		.addReplacement("HTML", HTML) // HTML last !!!
		.addReplacement("{ui_db_status}", () -> locale.get("ui_db_status", lang))
		.addReplacement("{status}", SERVICE_STATUS)
		.apply(SET_UP_LABEL)
		.addToPanel(statusPanel)
		.subscribe(uiListenersStatus);
		
		
		Supplier<String> SELF_STATUS = () -> {
			String result;
			switch (status) {
			case WAITING_RESPONSE:
				result = locale.get("ui_app_connection", lang);
				break;
			default:
				result = locale.get("ui_app_idle", lang);
			}
			return result;
		};
		
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("{status}")
		.addReplacement("{status}", SELF_STATUS)
		.addReplacement("ITALIC", () -> "<i>{...}</i>")
		.addReplacement("HTML", HTML) // HTML last !!!
		.apply(SET_UP_LABEL)
		.addToPanel(statusPanel)
		.subscribe(uiListenersStatus);
	}
	
	/**
	 * Sets up the response tab/panel (with the information about the weather conditions)
	 */
	private void setupResponse() {
		responsePanel.setBounds(20, 40, 480, 260);
		responsePanel.setLayout(new BoxLayout(responsePanel, BoxLayout.Y_AXIS));
		responsePanel.setVisible(false);
		
		Supplier<String> FONT_110 = () -> "<span style=\"font-size:110%\">{...}</span>";
		Supplier<String> BOLD = () -> "<b>{...}</b>";
		Supplier<String> ITALIC = () -> "<i>{...}</i>";
		Supplier<String> HTML = () -> "<html>{...}</html>";
		Consumer<JLabel> SET_UP_LABEL = label -> {
			label.setFont(label.getFont().deriveFont(0));
			label.setMaximumSize(new Dimension(400, 40));
			};
			
		new UiComponent<JLabel>(new JLabel())
			.setDefaultText("{resolvedAddress}")
			.setTextBehaviour((label, str) -> label.setText(str))
			.addReplacement("{resolvedAddress}", () -> response.get("resolvedAddress", String.class).orElse("Unknown location"))
			.addReplacement("ITALIC", ITALIC)
			.addReplacement("HTML", HTML)
			.apply(SET_UP_LABEL)
			.addToPanel(responsePanel)
			.subscribe(uiListenersResponse);
		
		new UiComponent<JLabel>(new JLabel())
			.setTextBehaviour((label, str) -> label.setText(str))
			.setDefaultText("{ui_updated}: {last_updated}")
			.addReplacement("{ui_updated}", () -> locale.get("ui_updated", lang))
			.addReplacement("{last_updated}", () -> response.get("last_update", String.class).orElse("????-??-??"))
			.apply(SET_UP_LABEL)
			.addToPanel(responsePanel)
			.subscribe(uiListenersResponse);
		
		new UiComponent<JLabel>(new JLabel())
			.setDefaultText("{ui_today} ({date})")
			.setTextBehaviour((label, str) -> label.setText(str))
			.addReplacement("{ui_today}", () -> locale.get("ui_today", lang))
			.addReplacement("{date}", () -> response.get(0, "datetime").orElse("????-??-??"))
			.addReplacement("SIZE", FONT_110)
			.addReplacement("BOLD", BOLD)
			.addReplacement("HTML", HTML)
			.apply(SET_UP_LABEL)
			.addToPanel(responsePanel)
			.subscribe(uiListenersResponse);

		responseAddDay(0);
		new UiComponent<JLabel>(new JLabel())
			.setDefaultText("{ui_tommorow} ({date})")
			.setTextBehaviour((label, str) -> label.setText(str))
			.addReplacement("{ui_tommorow}", () -> locale.get("ui_tommorow", lang))
			.addReplacement("{date}", () -> response.get(1, "datetime").orElse("????-??-??"))
			.addReplacement("SIZE", FONT_110)
			.addReplacement("BOLD", BOLD)
			.addReplacement("HTML", HTML)
			.apply(SET_UP_LABEL)
			.addToPanel(responsePanel)
			.subscribe(uiListenersResponse);
		responseAddDay(1);
	}
	
	/**
	 * Sub-task of setupResponse(). Boiler-plate code for the weather information on the specified day
	 * @param day - The exact day, for which the weather info is required (starting from 0)
	 */
	private void responseAddDay(int day) {
		Supplier<String> HTML = () -> "<html>{...}</html>";
		Consumer<JLabel> SET_UP_LABEL = label -> {
			label.setFont(label.getFont().deriveFont(0));
			label.setMaximumSize(new Dimension(400, 40));
			};
		
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("<b>{ui_temp}</b>: {temp}{units}")
		.addReplacement("{ui_temp}", () -> locale.get("ui_temp", lang))
		.addReplacement("{temp}", () -> response.get(day, "temp").orElse("???"))
		.addReplacement("{units}", () -> api.getDegreeUnit())
		.addReplacement("HTML", HTML)
		.apply(SET_UP_LABEL)
		.addToPanel(responsePanel)
		.subscribe(uiListenersResponse);

		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("<b>{ui_temp_min}</b>: {temp}{units}")
		.addReplacement("{ui_temp_min}", () -> locale.get("ui_temp_min", lang))
		.addReplacement("{temp}", () -> response.get(day, "tempmin").orElse("???"))
		.addReplacement("{units}", () -> api.getDegreeUnit())
		.addReplacement("HTML", HTML)
		.apply(SET_UP_LABEL)
		.addToPanel(responsePanel)
		.subscribe(uiListenersResponse);

		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("<b>{ui_temp_max}</b>: {temp}{units}")
		.addReplacement("{ui_temp_max}", () -> locale.get("ui_temp_max", lang))
		.addReplacement("{temp}", () -> response.get(day, "tempmax").orElse("???"))
		.addReplacement("{units}", () -> api.getDegreeUnit())
		.addReplacement("HTML", HTML)
		.apply(SET_UP_LABEL)
		.addToPanel(responsePanel)
		.subscribe(uiListenersResponse);
		
		
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("<i>{conditions}</i>")
		.addReplacement("{conditions}", () -> response.get(day, "conditions").orElse("No info on conditions"))
		.addReplacement("HTML", HTML)
		.apply(SET_UP_LABEL)
		.addToPanel(responsePanel)
		.subscribe(uiListenersResponse);
		
		new UiComponent<JLabel>(new JLabel())
		.setTextBehaviour((label, str) -> label.setText(str))
		.setDefaultText("{description}")
		.addReplacement("{description}", () -> response.get(day, "description").orElse("No info on description"))
		.addReplacement("HTML", HTML)
		.apply(SET_UP_LABEL)
		.addToPanel(responsePanel)
		.subscribe(uiListenersResponse);
	}
}
