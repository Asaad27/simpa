package learner.mealy.tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import learner.mealy.Node;
import main.Options;
import tools.GraphViz;
import tools.loggers.LogManager;

public class ObservationNode extends Node{
	public int state = -1;
	public int label = -1;
		
	public ObservationNode(){
		super();
	}
	
	public ObservationNode(String input, String output){
		super(input, output);
	}
	
	public void makeInitial(){
		state = 0;
		input = null;
		output = null;
		clearChildren();
	}
	
	public boolean isState(){
		return state > -1;
	}
	
	public boolean isLabelled(){
		return label > -1;
	}
	
	public boolean isInitial(){
		return state == 0 && input == null;
	}
	
	private void toDotCreateNodes(Writer w) throws IOException{
		w.write("    node" + id + " [style=\"rounded,filled\", fillcolor=\"#"+ (state==-1?"FFFFFF":"E0FEEE") + "\", color=\"#666666" + "\", shape=record, label=\"{"+ label + "|" + id + "}\"]\n");
		for (Node n : children){        	
			((ObservationNode)n).toDotCreateNodes(w);
        }
	}
		
	public File toDot(){
		Writer writer = null;
		File file = null;
		File imagePath = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {			
			if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("unable to create "+ dir.getName() +" directory");

			file = new File(dir.getPath() + File.separatorChar + "tmp" + ".dot");
			writer = new BufferedWriter(new FileWriter(file));
            writer.write("digraph G {\n");
            toDotCreateNodes(writer);
            toDotWrite(writer);
            writer.write("}\n");
            writer.close();
            imagePath = GraphViz.dotToFile(file.getPath());
		} catch (IOException e) {
            LogManager.logException("Error writing dot file", e);
        }
        return imagePath;
	}

	public String toString(){
		return "[" + id + ", " + state + ", " + label+"]";
	}
}
