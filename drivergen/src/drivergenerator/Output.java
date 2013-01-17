package drivergenerator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tools.Utils;

public class Output {
	private Elements source = null;	
	private String filteredSource = null;
	
	public Output(Document doc){
		this.source = doc.select(DriverGenerator.config.getLimitSelector());
		this.filteredSource = filter(doc.select(DriverGenerator.config.getLimitSelector()));
	}

	public Elements getDoc() {
		return source;
	}

	public String getFilteredSource() {
		return filteredSource;
	}
	
	private String filter(Elements selected) {
		String s = "";
		for(Element e : selected){
			s += e.tagName();
			if (e.tagName().equals("form")){
				s += e.attr("action");
			}
			if (e.tagName().equals("input")){
				s += e.attr("name");
			}
			s += filter(e.children());
		}
		return s;
	}

	private int computeLevenshteinDistance(CharSequence str1,
			CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = Utils
						.minimum(
								distance[i - 1][j] + 1,
								distance[i][j - 1] + 1,
								distance[i - 1][j - 1]
										+ ((str1.charAt(i - 1) == str2
												.charAt(j - 1)) ? 0 : 1));

		return distance[str1.length()][str2.length()];
	}

	public boolean isEquivalentTo(Output to) {
		double l = (double)computeLevenshteinDistance(to.getFilteredSource(), getFilteredSource());
		double c = l / ((double)(to.getFilteredSource().length()+getFilteredSource().length()) /2.0);
		if (c < 0.10){
			System.out.println(getDoc().html());
			System.out.println(to.getDoc().html());
			return true;
		}
		return false;
	}
}
