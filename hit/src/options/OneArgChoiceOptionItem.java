package options;

public class OneArgChoiceOptionItem extends ChoiceOptionItem {
	public String argValue;

	public OneArgChoiceOptionItem(String name, String argValue,
			GenericChoiceOption<?> parent) {
		super(name, parent, null);
		this.argValue = argValue;
	}

}
