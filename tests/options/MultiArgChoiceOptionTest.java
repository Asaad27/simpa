/********************************************************************************
 * Copyright (c) 2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Nicolas BREMOND
 ********************************************************************************/
package options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import tools.NullStream;

public class MultiArgChoiceOptionTest extends MultiArgChoiceOption {
	MultiArgChoiceOptionItem item1;
	MultiArgChoiceOptionItem item2;

	public MultiArgChoiceOptionTest() {
		super("Test");
		item1 = new MultiArgChoiceOptionItem("Item 1", "--item1", this);
		item2 = new MultiArgChoiceOptionItem("Item 2", "--item2", this);
		addChoice(item1);
		addChoice(item2);

	}

	@Test
	void testMissingArguments() {
		assertFalse(parseArguments("", new NullStream()));
	}

	@Test
	void testParseValue() {
		assertTrue(parseArguments(item1.argument.name, new NullStream()));
		assertEquals(item1, getSelectedItem());
		assertTrue(parseArguments(item2.argument.name, new NullStream()));
		assertEquals(item2, getSelectedItem());
	}

	@Test
	void testDefaultValue() {
		assertTrue(parseArguments(item1.argument.name, new NullStream()));
		setDefaultItem(item2);
		assertEquals(item1, getSelectedItem());
		assertTrue(parseArguments("", new NullStream()));
		assertEquals(item2, getSelectedItem());
		testParseValue();
	}
}
