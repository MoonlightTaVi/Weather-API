package sh.roadmap.tavi.weatherapi.gui.locale;

import javax.swing.*;

public class UiFactory {

	private UiObserver observer;

	public UiObserver getObserver() {
		return observer;
	}

	public void setObserver(UiObserver observer) {
		this.observer = observer;
	}
	
	
	public JLabel getLabel(String refText) {
		JLabel result = new JLabel(refText);
		result.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		result .setHorizontalAlignment(JLabel.CENTER);
		observer.register(result);
		result.setFont(result.getFont().deriveFont(0));
		return result;
	}
	
	public JLabel getLabelBold(String refText) {
		JLabel result = new JLabel(refText);
		result.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		result .setHorizontalAlignment(JLabel.CENTER);
		observer.register(result);
		return result;
	}
	
	public JButton getButton(String refText) {
		JButton result = new JButton(refText);
		observer.register(result);
		return result;
	}
	
}
