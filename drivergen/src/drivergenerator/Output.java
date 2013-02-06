package drivergenerator;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import drivergenerator.page.PageTreeNode;

public class Output {
	private Elements source = null;
	private List<String> params = null;
	private PageTreeNode pt = null;
	
	public Output(){
		params = new ArrayList<String>();
	}

	public Output(Document doc) {
		this.source = doc.getAllElements();
		if (!DriverGenerator.config.getLimitSelector().isEmpty()) this.source = doc.select(DriverGenerator.config.getLimitSelector());		
		this.params = new ArrayList<String>();
		pt = new PageTreeNode(doc);
	}
	
	public Output(String source) {
		Document doc = Jsoup.parse(source);
		this.source = doc.getAllElements();
		if (!DriverGenerator.config.getLimitSelector().isEmpty()) this.source = doc.select(DriverGenerator.config.getLimitSelector());
		this.params = new ArrayList<String>();
		pt = new PageTreeNode(doc);
	}

	public List<String> getParams() {
		return params;
	}

	public Elements getDoc() {
		return source;
	}
	
	public boolean isEquivalentTo(Output to) {
		return pt.equals(to.pt);
	}		
}
