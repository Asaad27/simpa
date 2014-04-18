package automata.efsm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.simpa.Options;
import tools.GraphViz;
import tools.loggers.LogManager;
import automata.Automata;
import automata.State;

public class EFSM extends Automata implements Serializable{
	private static final long serialVersionUID = 4584744792357781025L;

	protected List<EFSMTransition> transitions;
	protected Map<String, String> memory;
	
	public final static String OMEGA = "omega|symbol";
	public final static String EPSILON = "espilon|symbol";
	
	public EFSM(String name){
		super(name);
		transitions = new ArrayList<EFSMTransition>();
		memory = new HashMap<String, String>();	
	}
	
	public Boolean addTransition(EFSMTransition t){
		return transitions.add(t);
	}
	
	public List<EFSMTransition> getTransitions(){
		return transitions;
	}
	
	public EFSMTransition getTransition(int index){
		return transitions.get(index);
	}
	
	public EFSMTransition removeTransition(int index){
		return transitions.remove(index);
	}
	
	public int getTransitionCount(){
		return transitions.size();
	}
	
	public void setMemory(String var, String value){
		memory.put(var, value);
	}
	
	public String getMemory(String var){
		return memory.get(var);
	}
	
	public List<EFSMTransition> getTransitionFrom(State s) {
		return getTransitionFrom(s, true);
	}
	
	public List<EFSMTransition> getTransitionTo(State s) {
		return getTransitionFrom(s, true);
	}
	
	public List<EFSMTransition> getTransitionFrom(State s, boolean loop) {
		List<EFSMTransition> res = new ArrayList<EFSMTransition>();
		for (EFSMTransition t : transitions){
			if (t.getFrom().equals(s)){
				if (t.getTo().equals(s)){
					if (loop) res.add(t);
				}else
					res.add(t);
			}
		}
		return res;
	}
	
	public List<EFSMTransition> getTransitionTo(State s, boolean loop) {
		List<EFSMTransition> res = new ArrayList<EFSMTransition>();
		for (EFSMTransition t : transitions){
			if (t.getTo().equals(s)){
				if (t.getFrom().equals(s)){
					if (loop) res.add(t);
				}else
					res.add(t);
			}
		}
		return res;
	}

	public List<EFSMTransition> getTransitionFromWithInput(State s, String input) {
		List<EFSMTransition> res = new ArrayList<EFSMTransition>();
		for (EFSMTransition t : getTransitionFrom(s)){
			if (t.getInput().equals(input)) res.add(t);
		}
		return res;
	}
	
	public List<EFSMTransition> getTransitionFromWithInput(State s, String input, boolean loop) {
		List<EFSMTransition> res = new ArrayList<EFSMTransition>();
		for (EFSMTransition t : getTransitionFrom(s, loop)){
			if (t.getInput().equals(input)) res.add(t);
		}
		return res;
	}
	
	public void exportToDot(){
		Writer writer = null;
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {			
			if (!dir.isDirectory() && !dir.mkdirs()) throw new IOException("unable to create "+ dir.getName() +" directory");

			file = new File(dir.getPath() + File.separatorChar + name + "_orig.dot");
			writer = new BufferedWriter(new FileWriter(file));
            writer.write("digraph G {\n");
            for (EFSMTransition t : getTransitions()){
            	writer.write("\t" + t.toRawDot() + "\n");
            }
            writer.write("}\n");
            writer.close();
            File imagePath = GraphViz.dotToFile(file.getPath());
            if (imagePath!= null) LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
            LogManager.logException("Error writing dot file", e);
        }		
	}
	
	@Override
	public void reset(){
		memory.clear();
	}
}
