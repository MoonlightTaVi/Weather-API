package sh.roadmap.tavi.weatherapi.view;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;

import sh.roadmap.tavi.weatherapi.tools.Applier;

/**
 * A decorator, which simplifies the work with the text inside a UI component
 * @param <T> - The type of the UI component
 */
public class UiComponent<T extends JComponent> {
	
	private final T component;
	
	private Applier<T, String> textSetter;
	private Map<String, Supplier<String>> replaceableText = new LinkedHashMap<>();
	private String defaultText = "";
	
	/**
	 * Creates a decorator, which simplifies the work with the text inside a UI component
	 * @param component - UI component to work with
	 */
	public UiComponent(T component) {
		this.component = component;
	}
	
	/**
	 * 
	 * @param textSetter - A functional interface, which sets the behavior of how the text inside the UI component should be updated
	 * @return - Itself, for chaining
	 */
	public UiComponent<T> setTextBehaviour(Applier<T, String> textSetter) {
		this.textSetter = textSetter;
		return this;
	}
	
	/**
	 * When an update occurs, replaces the "from" substring inside the text variable to the String, got from the "to" Supplier.
	 * If the String from the Supplier contains "{...}", then the "from" parameter means and does nothing.
	 * The "{...}" will be replaced with all the current text of the UI component, making it able to surround it with something
	 * (e.g. HTML tags)
	 * @param from - Substring to replace (if the String from the "to" parameter contains "{...}", then means and does nothing)
	 * @param to - Supplier, returning a String to replace the substring with (if contains "{...}", then the "from" parameter means and does nothing)
	 * @return - Itself, for chaining
	 */
	public UiComponent<T> addReplacement(String from, Supplier<String> to) {
		replaceableText.put(from, to);
		return this;
	}
	
	/**
	 * The default text of the UI component, which may (and usually does) have some place holders
	 * @param defaultText - Default text of the UI component
	 * @return - Itself, for chaining
	 */
	public UiComponent<T> setDefaultText(String defaultText) {
		this.defaultText = defaultText;
		return this;
	}
	
	/**
	 * Applies some changes to the component inside this decorator (like calling some methods on the component)
	 * @param componentChange - Consumer, which does something with the component inside this decorator
	 * @return - Itself, for chaining
	 */
	public UiComponent<T> apply(Consumer<T> componentChange) {
		componentChange.accept(component);
		return this;
	}
	
	/**
	 * Adds the component inside this decorator to some panel
	 * @param panel - JPanel to add to
	 * @return - Itself, for chaining
	 */
	public UiComponent<T> addToPanel(JPanel panel) {
		panel.add(component);
		return this;
	}
	
	/**
	 * Subscribes to some Collection, which will then be used to call the update() method on this current object
	 * @param observer - Collection, which stores all the UiComponent's to update
	 * @return - Itself, for chaining
	 */
	public UiComponent<T> subscribe(Collection<UiComponent<?>> observer) {
		observer.add(this);
		return this;
	}
	
	/**
	 * Updates the text of the UI Component inside this decorator. Usually called by some Observer.
	 */
	public void update() {
		if (textSetter == null) {
			//log.warning(defaultText, new NullPointerException("No text setter for an UiComponent determined"));
			return;
		}
		
		String result = defaultText;
		for (Map.Entry<String, Supplier<String>> entry : replaceableText.entrySet()) {
			String replacement = entry.getValue().get();
			if (replacement.contains("{...}")) {
				result = replacement.replace("{...}", result);
				continue;
			}
			result = result.replace(entry.getKey(), replacement);
		}
		
		textSetter.apply(component, result);
	}
	
}
