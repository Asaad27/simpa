package drivergenerator.page;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

public class PageTreeNode extends GenericTreeNode<String> {
	
	public Set<String> excludedNode = new HashSet<String>(Arrays.asList(new String[] {
			"#document", "#text", "span", "font", "a", "center", "bold", "italic", "style", "base", "param", "script", "noscript",
			"b", "i", "tt", "sub", "sup", "big", "small", "img", "br", "tr", "td", "option"
			}));

	public PageTreeNode(String nodeName){
		super(nodeName);
	}
	
	public PageTreeNode(Document doc){
		super(doc.body().nodeName());
		extractPageTree(doc.body().childNodes(), this);
	}
	
    private void extractPageTree(List<Node> childNodes, PageTreeNode pt) {
		for(Node n : childNodes){
			if (!excludedNode.contains(n.nodeName())){
				PageTreeNode tmp = new PageTreeNode(n.nodeName());
				if (n.nodeName().equals("form")) tmp.setData(n.nodeName()+"["+n.attr("action")+"]");
				extractPageTree(n.childNodes(), tmp);
				pt.addChild(tmp);
			}else{
				extractPageTree(n.childNodes(), pt);
			}
		}		
	}
	
	public boolean equals(PageTreeNode to) {
		if (!data.equals(to.data)) return false;
		else{
			if (getNumberOfChildren() != to.getNumberOfChildren()) return false;
			else{
				for(int i=0; i<getNumberOfChildren(); i++){
					if (!((PageTreeNode)getChildAt(i)).equals((PageTreeNode)to.getChildAt(i))) return false;
				}
			}
		}
		return true;
	}
}
