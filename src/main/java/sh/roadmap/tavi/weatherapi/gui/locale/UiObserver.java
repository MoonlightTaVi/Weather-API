package sh.roadmap.tavi.weatherapi.gui.locale;

import java.util.*;

import javax.swing.*;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class UiObserver implements ApplicationListener<ContextRefreshedEvent> {

	private ResourceBundle rb;
	private Map<Object, String> components = new HashMap<>();
	
	
	public void register(Object... components) {
		for (Object component : components) {
			switch(component) {
			case JLabel label: this.components.put(component, label.getText()); break;
			case JTextArea area: this.components.put(component, area.getText()); break;
			case AbstractButton button: this.components.put(component, button.getText()); break;
			case JFrame frame: this.components.put(component, frame.getTitle()); break;
			default: break;
			}
		}
	}
	
	public void update() {
		rb = ResourceBundle.getBundle("ui", Locale.getDefault());
		for (Object component : components.keySet()) {
			String name = components.get(component);
			switch(component) {
			case JLabel label: label.setText(rb.getString(name)); break;
			case JTextArea area: area.setText(rb.getString(name)); break;
			case AbstractButton button: button.setText(rb.getString(name)); break;
			case JFrame frame: frame.setTitle(rb.getString(name)); break;
			default: break;
			}
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		update();
	}
	
	
	
}
