package drivergenerator.page;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class Test {
	
	static Set<String> excludedNode = new HashSet<String>(Arrays.asList(new String[] {
			"#document", "#text", "span", "font", "a", "center", "bold", "italic", "style", "base", "param", "script", "noscript",
			"b", "i", "tt", "sub", "sup", "big", "small", "img", "br"
			}));

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testParcours();
	}

	private static void testParcours() {
		Document d = null;
		try {
			d = Jsoup.parse(new File("example.html"), "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PageTreeNode n = null;
		d.body().traverse(new NodeVisitor() {
		    public void head(Node node, int depth) {
		    	if (!excludedNode.contains(node.nodeName())){
		    		System.out.println(space(depth) + node.nodeName());
		    		if (n==null) n = new PageTreeNode(node.nodeName());
		    		else
		    	}
		    }

			public void tail(Node node, int depth) {
		        
		    }
		});
	}
	
    private static String space(int depth) {
    	String s = "";
		for (int i=0; i<depth*3; i++) s += " ";
		return s;
	}

}
