package sh.roadmap.tavi.weatherapi.gui.locale;

import java.util.*;

import javax.swing.*;

public class UiObserver {

	private ResourceBundle rb;
	private Map<String, Object> components = new HashMap<>();
	
	
	public void register(Object... components) {
		for (Object component : components) {
			switch(component) {
			case JLabel label: this.components.put(label.getText(), component); break;
			case AbstractButton button: this.components.put(button.getText(), component); break;
			case JFrame frame: this.components.put(frame.getTitle(), component); break;
			default: break;
			}
		}
	}
	
	public void update() {
		rb = ResourceBundle.getBundle("ui", Locale.getDefault());
		for (String name : components.keySet()) {
			switch(components.get(name)) {
			case JLabel label: label.setText(rb.getString(name)); break;
			case AbstractButton button: button.setText(rb.getString(name)); break;
			case JFrame frame: frame.setTitle(rb.getString(name)); break;
			default: break;
			}
		}
	}
	
	
	
}
