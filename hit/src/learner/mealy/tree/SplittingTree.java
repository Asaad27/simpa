package learner.mealy.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import tools.SplittingTreeAntlrListener;
//import tools.antlr4.SplittingTree.*;
import tools.antlr4.SplittingTree.SplittingTreeLexer;
import tools.antlr4.SplittingTree.SplittingTreeParser;

/**
 * 
 */

/**
 * @author Lx Wang Construction of Splitting tree, used for algo
 *         AdaptiveLocaliser
 */
public class SplittingTree {

	/**
	 * get all elements from Splitting Tree
	 * 
	 * @param args
	 * @param root;
	 * @param splittingTree;
	 * @param inputSequence;
	 * @param outputSequence;
	 * @param branchs;
	 * @param Leaf
	 *            ;
	 */
	String root;
	String splittingTree;
	ArrayList<String> inputSequence;
	ArrayList<String> outputSequence;
	ArrayList<String> branchs;
	ArrayList<String> Leaf;
	ArrayList<String> subSplittingTree;
	ArrayList<String> io;
	Map<String, String> branch = new HashMap<String, String>();

	public SplittingTree() {
	}

	public SplittingTree(String sTree) throws FileNotFoundException, IOException {
		File file = new File(sTree);
		if (!file.exists()) {
			throw new IOException("'" + file.getAbsolutePath() + "' do not exists");
		}
		ANTLRInputStream stream = new ANTLRInputStream(new FileInputStream(file));
		SplittingTreeLexer lexer = new SplittingTreeLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SplittingTreeParser parser = new SplittingTreeParser(tokens);
		// tell ANTLR to build a parse tree
		parser.setBuildParseTree(true);
		ParseTree tree = parser.splitting_tree();
		// create a standard ANTLR parse tree walker
		ParseTreeWalker walker = new ParseTreeWalker();
		// create listener then feed to walker
		SplittingTreeAntlrListener antlrL = new SplittingTreeAntlrListener();
		walker.walk(antlrL, tree);
		root = antlrL.root;
		splittingTree = antlrL.splittingTree;
		inputSequence = antlrL.inputSequence;
		outputSequence = antlrL.outputSequence;
		branchs = antlrL.branchs;
		Leaf = antlrL.Leaf;
		subSplittingTree = antlrL.subSplittingTree;
		io = antlrL.props;
		branch = antlrL.branch;
	}

	public void addBranch(Branch b) {
		if (inputSequence.size() != b.outputSequence.size()) {
			throw new RuntimeException("Length of input sequence is diffrent from output sequence.");

		}

		// branchs.add(b);

	}

	public String subTree(String in) {
		// SplittingTree st = new SplittingTree(in);
		return in;

	}

	String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		// String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				// stringBuilder.append(ls);
			}
			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}

	void showGuiTreeView(String filename) throws IOException {
		File file = new File(filename);
		final org.antlr.v4.runtime.CharStream stream = new ANTLRInputStream(new FileInputStream(file));
		final SplittingTreeLexer lexer = new SplittingTreeLexer(stream);
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		final SplittingTreeParser parser = new SplittingTreeParser(tokens);
		final ParseTree tree = parser.splitting_tree();
		final List<String> ruleNames = Arrays.asList(SplittingTreeParser.ruleNames);
		final TreeViewer view = new TreeViewer(ruleNames, tree);
		view.open();
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File file = new File("/Users/wang/Desktop/splittingtree.txt");
		SplittingTree st = new SplittingTree("/Users/wang/Desktop/splittingtree.txt");
		System.out.println("splittingTree IO----- " + st.io);
		System.out.println("splittingTree branch----- " + st.branch);
		/*
		 * for (String i : st.inputSequence) { System.out.println(
		 * "inputSequence --->" + i); } for (String i : st.outputSequence) {
		 * System.out.println("outputSequence --->" + i); }
		 **/
		st.showGuiTreeView("/Users/wang/Desktop/splittingtree.txt");
	}

}
