package tools;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testStringToList() {
		StringToListToString("abcd efgh");
		StringToListToString("abcd\\ efgh");
		StringToListToString("abcd\\\\ efgh");
		StringToListToString("ab\\\\cd efgh");
		StringToListToString("");
		StringToListToString(" ");
		StringToListToString("\\ ");
		ListToStringToList(Arrays.asList("abcd", "efgh"));
		ListToStringToList(Arrays.asList("abcd\\", "efgh"));
		ListToStringToList(Arrays.asList("abcd\\\\", "efgh"));
		ListToStringToList(Arrays.asList("ab\\cd", "efgh"));
		ListToStringToList(Arrays.asList("ab\\cd", "efgh"));
		ListToStringToList(Arrays.asList("", "", " ", ""));
	}

	public void StringToListToString(String in) {
		String out = Utils.listToString(Utils.stringToList(in));
		assertEquals(in, out);
	}

	public void ListToStringToList(List<String> in) {
		List<String> out = Utils.stringToList(Utils.listToString(in));
		assertEquals(in, out);
	}

}
