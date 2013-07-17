package crawler;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import crawler.page.PageTreeNode;


public class Output {
	private Elements doc;
	private String source = null;
	private List<String> params = null;
	private PageTreeNode pt = null;
	private List<Input> from = null;
	private int state;
	
	public String getSource(){
		return source;
	}

	public Elements getDoc(){
		return doc;
	}
	
	public PageTreeNode getPageTree(){
		return pt;
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

	public Output(Document doc, Input from, String limitSelector) {
		this();		
		this.from.add(from);
		this.source = doc.html();
		this.doc = doc.getAllElements();
		if (limitSelector!=null && !limitSelector.isEmpty()){
			this.doc = doc.select(DriverGenerator.config.getLimitSelector());
			this.source = this.doc.html();
		}
		pt = new PageTreeNode(Jsoup.parse(this.source));
	}
	
	public Output(String source, boolean raw, String limitSelector) {
		this();		
		Document doc = Jsoup.parse(source);
		this.source = doc.html();
		this.doc = doc.getAllElements();
		if (!raw){
			if (limitSelector!=null && !limitSelector.isEmpty()){
				this.doc = doc.select(limitSelector);
				this.source = this.doc.html();
			}
		}	
		pt = new PageTreeNode(Jsoup.parse(this.source));
	}

	public List<String> getParams() {
		return params;
	}
	
	public boolean isEquivalentTo(Output to) {
		return pt.equals(to.pt);
	}
}
