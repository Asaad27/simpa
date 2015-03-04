package crawler;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import crawler.page.PageTreeNode;

public class WebOutput {

	private Elements doc;
	private String source = null;
	private List<String> params = null;
	private PageTreeNode pt = null;
	private List<WebInput> from = null;
	private int state;

	public String getSource() {
		return source;
	}

	public Elements getDoc() {
		return doc;
	}

	public PageTreeNode getPageTree() {
		return pt;
	}

	public WebOutput() {
		params = new ArrayList<>();
		from = new ArrayList<>();
	}

	public void addFrom(WebInput i) {
		if (!from.contains(i)) {
			from.add(i);
		}
	}

	/**
	 * Checks if the given WebInput is already known to result in this
	 * WebOutput.
	 * <p>
	 * If the WebInput is effectively new, it is memorized.
	 *
	 * @param i
	 * @return
	 */
	public boolean isNewFrom(WebInput i) {
		boolean n = from.contains(i);
		if (!n) {
			from.add(i);
		}
		return !n;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public WebOutput(Document document, WebInput from, String limitSelector) {
		this();
		this.from.add(from);
		this.source = document.html();
		this.doc = document.getAllElements();
		if (limitSelector != null && !limitSelector.isEmpty()) {
			this.doc = document.select(DriverGenerator.config.getLimitSelector());
			this.source = this.doc.html();
		}
		pt = new PageTreeNode(Jsoup.parse(this.source));
	}

	public WebOutput(String source, boolean raw, String limitSelector) {
		this();
		Document document = Jsoup.parse(source);
		this.source = document.html();
		this.doc = document.getAllElements();
		if (!raw) {
			if (limitSelector != null && !limitSelector.isEmpty()) {
				this.doc = document.select(limitSelector);
				this.source = this.doc.html();
			}
		}
		pt = new PageTreeNode(Jsoup.parse(this.source));
	}

	public List<String> getParams() {
		return params;
	}

	public boolean isEquivalentTo(WebOutput to) {
		return pt.equals(to.pt);
	}
}
