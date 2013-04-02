package crawler;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import crawler.driver.GenericDriver;
import crawler.page.PageTreeNode;


public class Output {
	private int state = 0;
	private Elements source = null;
	private List<String> params = null;
	private PageTreeNode pt = null;
	private boolean mark = false;
	private List<Input> from = null;
	
	public boolean isMark() {
		return mark;
	}

	public void setMark() {
		this.mark = true;
	}
	
	public void cleanMark() {
		this.mark = false;
	}

	public Output(){
		params = new ArrayList<String>();
		from = new ArrayList<Input>();
	}
	
	public void addFrom(Input i){
		if (!from.contains(i)) from.add(i);
	}
	
	public boolean isNewFrom(Input i){
		boolean n = from.contains(i);
		if (!n) from.add(i);
		return !n;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Output(Document doc, Input from) {
		this();
		this.from.add(from);
		this.source = doc.getAllElements();
		if (GenericDriver.config!=null && !DriverGenerator.config.getLimitSelector().isEmpty()) this.source = doc.select(DriverGenerator.config.getLimitSelector());
		if (GenericDriver.config!=null && !GenericDriver.config.getLimitSelector().isEmpty()) this.source = doc.select(GenericDriver.config.getLimitSelector());
		pt = new PageTreeNode(Jsoup.parse(this.source.html()));
	}
	
	public Output(String source, boolean raw) {
		this();
		Document doc = Jsoup.parse(source);
		this.source = doc.getAllElements();
		if (!raw){
			if (DriverGenerator.config!=null && !DriverGenerator.config.getLimitSelector().isEmpty()) this.source = doc.select(DriverGenerator.config.getLimitSelector());
			if (GenericDriver.config!=null && !GenericDriver.config.getLimitSelector().isEmpty()) this.source = doc.select(GenericDriver.config.getLimitSelector());
			pt = new PageTreeNode(Jsoup.parse(getDoc().html()));
		}else{
			pt = new PageTreeNode(doc);
		}		
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
