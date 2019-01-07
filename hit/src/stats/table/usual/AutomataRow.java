package stats.table.usual;

import stats.attribute.Attribute;

public class AutomataRow extends AttributeTableRow<String> {

	public AutomataRow(String header, String attributeValue) {
		super(header, Attribute.AUTOMATA, attributeValue);
	}

}
