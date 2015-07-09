package crawler;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import crawler.page.PageTreeNode;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jsoup.nodes.Element;

public class WebOutput {

	private Elements doc;
	private String source = null;
	/**
	 * The list of parameters' names
	 */
	private Set<String> params = null;
	/**
	 * An example of value for each parameter
	 */
	private Map<String, String> paramsValues = null;
	private PageTreeNode pt = null;
	private List<WebInput> from = null;
	private int state = 0;

	public String getSource() {
		return source;
	}

	public Elements getDoc() {
		return doc;
	}

	/**
	 * Return the Elements of the html document without the "excluded" nodes.
	 * It removes the excluded nodes from the DOM, but keep their children nodes.
	 * @see PageTreeNode#excludedNode
	 */
	public Elements getDocWithoutExcludedNodes() {
		Elements document = this.doc.clone();
		for (String excludedNode : PageTreeNode.excludedNode) {
			Elements excludedNodes = document.select(excludedNode);
			excludedNodes.unwrap();
		}
		return document;
	}

	public PageTreeNode getPageTree() {
		return pt;
	}

	private WebOutput() {
		params = new HashSet<>();
		paramsValues = new HashMap<>();
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
		if (from != null) {
			document.setBaseUri(from.getAddress());
			this.from.add(from);
		}
		if (limitSelector != null && !limitSelector.isEmpty()) {
			this.doc = document.select(limitSelector);
			this.source = this.doc.html();
		} else {
			this.doc = document.getAllElements();
			this.source = document.html();
		}
		pt = new PageTreeNode(Jsoup.parse(this.source));
	}

	public WebOutput(String source, WebInput from, String limitSelector) {
		this(Jsoup.parse(source), from, limitSelector);
	}
	
	public WebOutput(String source, String limitSelector) {
		this(Jsoup.parse(source), null, limitSelector);
	}

	public WebOutput(String source) {
		this(Jsoup.parse(source), null, null);
	}
	
	
	public boolean isEquivalentTo(WebOutput to) {
		return pt.equals(to.pt);
	}
	
	
	/*
	 * Methods related to parameters access
	 */
	
	/**
	 * Retrieve the value of an output parameter.
	 * /!\ Warning : the given path is valid in the FILTERED version of the document, 
	 * i.e without the excluded nodes
	 * 
	 * @param paramPath The path in the DOM where the parameter is located
	 * @return The parameter's value
	 */
	public String extractParam(String paramPath) {
		String path[] = paramPath.split("/");
		Element e = this.getDocWithoutExcludedNodes().get(Integer.parseInt(path[0]));
		for (int i = 1; i < path.length; i++) {
			try {
				e = e.child(Integer.parseInt(path[i]));
			} catch (Exception ex) {
				//System.err.println("The path does not work on that page");
				return "";
			}
		}
		return e.ownText();
	}
	
	public void removeParam(String paramPath, Iterator<String> iter){
		iter.remove();
		paramsValues.remove(paramPath);
	}
	
	public void removeParam(String paramPath){
		params.remove(paramPath);
		paramsValues.remove(paramPath);
	}
	
	public String getParamValue(String paramPath){
		return paramsValues.get(paramPath);
	}
		
	public Iterator<String> getParamsIterator(){
		return params.iterator();
	}
	
	public void addParam(String paramPath){
		params.add(paramPath);
	}
	
	public void addParam(String paramPath, String paramValue){
		addParam(paramPath);
		paramsValues.put(paramPath, paramValue);
	}
	
	public void addAllParams(Collection<String> paramsPaths){
		params.addAll(paramsPaths);
	}
	
	public void addAllParams(Map<String,String> paramsPathsValues){
		params.addAll(paramsPathsValues.keySet());
		paramsValues.putAll(paramsPathsValues);
	}
	
	public int getParamsNumber(){
		return params.size();
	}
}
