package stats.table.usual;

import stats.StatsSet;
import stats.attribute.Attribute;
import stats.attribute.restriction.EqualsRestriction;
import stats.table.TableRow;

/**
 * a simple row filtering on one attribute
 * 
 * @author Nicolas BREMOND
 *
 * @param <T>
 *            the type of the filtering attribute
 */
public class AttributeTableRow<T extends Comparable<T>> extends TableRow {
	public AttributeTableRow(String header, Attribute<T> attribute,
			T attributeValue) {
		this.header = header;
		this.attribute = attribute;
		this.attributeValue = attributeValue;
	}

	final String header;
	final Attribute<T> attribute;
	final T attributeValue;

	@Override
	public String getRawHeader() {
		return header;
	}

	@Override
	public StatsSet getData(StatsSet s) {
		return new StatsSet(s,
				new EqualsRestriction<T>(attribute, attributeValue));
	}

}
