/********************************************************************************
 * Copyright (c) 2018,2019 Institut Polytechnique de Grenoble 
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
package tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class UtilsTest {

	@Test
	public void testStringToList() {
		StringToListToString("abcd efgh ");
		StringToListToString("abcd\\ efgh ");
		StringToListToString("abcd\\\\ efgh ");
		StringToListToString("ab\\\\cd efgh ");
		StringToListToString(" ");
		StringToListToString("  ");
		StringToListToString("\\  ");
		ListToStringToList(Arrays.asList("abcd", "efgh"));
		ListToStringToList(Arrays.asList("abcd\\", "efgh"));
		ListToStringToList(Arrays.asList("abcd\\\\", "efgh"));
		ListToStringToList(Arrays.asList("ab\\cd", "efgh"));
		ListToStringToList(Arrays.asList("ab\\cd", "efgh"));
		ListToStringToList(Arrays.asList("", "", " ", ""));
	}

	public void StringToListToString(String in) {
		StringBuilder warnings = new StringBuilder();
		String out = Utils.listToString(Utils.stringToList(in, warnings));
		assertTrue((out.equals(in) && warnings.length() == 0)
				|| out.contentEquals(in + " "));
	}

	public void ListToStringToList(List<String> in) {
		StringBuilder warnings = new StringBuilder();
		List<String> out = Utils.stringToList(Utils.listToString(in), warnings);
		assertEquals(in, out);
		assertEquals(warnings.length(), 0);// warnings should only appear in
											// user-written strings.
	}

	@Test
	public void testFileContent() {
		stringToFileToString("test");
		stringToFileToString("test\n");
		stringToFileToString("test\r");
		stringToFileToString("test\r\n");
		stringToFileToString("test1\ntest2");
		stringToFileToString("test1\rtest2");
		stringToFileToString("test1\r\ntest2");
	}

	public void stringToFileToString(String in) {
		File f;
		try {
			f = File.createTempFile("tmptest", null);
		} catch (IOException e) {
			return;
		}
		Utils.setFileContent(f, in);
		String out = Utils.fileContentOf(f);
		assertEquals(in, out);
		f.delete();
	}
}
