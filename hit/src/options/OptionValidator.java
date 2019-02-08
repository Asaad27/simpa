package options;

import javax.swing.JLabel;
import javax.swing.UIManager;

public abstract class OptionValidator {
	public enum CriticalityLevel {
		NOTHING, WARNING, ERROR,
	}

	private CriticalityLevel criticality = CriticalityLevel.NOTHING;
	private String message = "";

	public OptionValidator() {
	}

	public CriticalityLevel getCriticality() {
		return criticality;
	}

	protected void setCriticality(CriticalityLevel criticality) {
		this.criticality = criticality;
	}

	protected void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public abstract void check();

	public JLabel createComponent() {
		if (criticality.compareTo(CriticalityLevel.ERROR) >= 0)
			return new JLabel(getMessage(),
					UIManager.getIcon("OptionPane.errorIcon"), JLabel.LEFT);
		if (criticality.compareTo(CriticalityLevel.WARNING) >= 0)
			return new JLabel(getMessage(),
					UIManager.getIcon("OptionPane.warningIcon"), JLabel.LEFT);
		return new JLabel(getMessage());
	}

}
