package tools;

/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.antlr4.DotMealy.*;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.misc.TestRig;

import automata.mealy.Mealy;

public class DotParser {

	public DotParser() {
	}

	void showGuiTreeView(File filename) throws IOException {

		final org.antlr.v4.runtime.CharStream stream = new ANTLRInputStream(new FileInputStream(filename));
		final DotMealyLexer lexer = new DotMealyLexer(stream);
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		final DotMealyParser parser = new DotMealyParser(tokens);
		final ParseTree tree = parser.graph();
		final List<String> ruleNames = Arrays.asList(DotMealyParser.ruleNames);
		final TreeViewer view = new TreeViewer(ruleNames, tree);
		view.open();
	}

	public Mealy getAutomaton(File file) throws FileNotFoundException, IOException {
		if (!file.exists())
			throw new IOException("'" + file.getAbsolutePath() + "' do not exists");
		if (!file.getName().endsWith(".dot"))
			System.err.println("Are you sure that '" + file + "' is a dot file ?");

		final org.antlr.v4.runtime.CharStream stream = new ANTLRInputStream(new FileInputStream(file));
		final DotMealyLexer lexer = new DotMealyLexer(stream);
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		final DotMealyParser parser = new DotMealyParser(tokens);

		final ParseTree tree = parser.graph();
		ParseTreeWalker walker = new ParseTreeWalker();

		DotAntlrListener antlrL = new DotAntlrListener();

		walker.walk(antlrL, tree);
		return antlrL.automaton;
	}

	public static void main(String[] args) throws IOException {
		//
		/*
		 * DotParser dotParser = new DotParser(); File file = new
		 * File("/Users/wang/Documents/MyWorkspace/DotParser/test2.dot");
		 * dotParser.showGuiTreeView(file);
		 */

	}
}
