package drivers.mealy.real.mqtt;

public abstract class MQTTOperation {
	private String input = null;

	protected String usedTopic;
	protected String usedMessage;

	public String createInput(boolean showTopic, boolean showMessage) {
		assert input == null;
		input = createInput_intern(showTopic, showMessage);
		return input;
	}

	protected abstract String createInput_intern(boolean showTopic,
			boolean showMessage);

	public String getInput() {
		return input;
	}

	public abstract String execute();
}
