package drivergenerator.page;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testParcours();
	}

	private static void testParcours() {
		Document d1 = null;
		Document d2 = null;
		try {
			d1 = Jsoup.parse(new File("reg.html"), "utf-8");
			d2 = Jsoup.parse(new File("search.html"), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		PageTreeNode n1 = new PageTreeNode(d1);
		PageTreeNode n2 = new PageTreeNode(d2);
		System.out.println(n1.equals(n2));
	}
}
