package learner.mealy.alpha;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import automata.Automata;
import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import main.simpa.Options;
import tools.GraphViz;
import tools.loggers.LogManager;



public class FSMConjecture {
	public Map<StateLabel, List<ConjectureTransition>> transitions;
	public List<String> inputs;


	public FSMConjecture(List<String> inputs) {
		transitions = new HashMap<StateLabel, List<ConjectureTransition>>();
		this.inputs=inputs;
	}
	
	public ConjectureTransition findSimilarTransition(ConjectureTransition t){
		if(!transitions.containsKey(t.from)){
			return null;
		}
		List<ConjectureTransition> l = transitions.get(t.from);
		for(ConjectureTransition lt : l){
			if(lt.input.equals(t.input)){
				return lt;
			}
		}
		return null;
	}

	public List<ConjectureTransition> getTransitionsFrom(StateLabel label){
		return transitions.get(label);
	}
	
	public List<ConjectureTransition> getTransitionsTo(StateLabel label){
		List<ConjectureTransition> list = new LinkedList<>();
		for(List<ConjectureTransition> l : transitions.values()){
			for(ConjectureTransition t : l){
				if(t.to.equals(label)){
					list.add(t);
					l.remove(t);
				}
			}
		}
		return list;
	}

	public Collection<ConjectureTransition> getTransitions() {
		Collection<ConjectureTransition> c = new HashSet<>();
		for(List<ConjectureTransition> l : transitions.values()){
			c.addAll(l);
		}
		return c;
	}

	

	
	

	public void exportToDot() {
		Writer writer = null;
		File file = null;
		File dir = new File(Options.OUTDIR + Options.DIRGRAPH);
		try {
			if (!dir.isDirectory() && !dir.mkdirs())
				throw new IOException("unable to create " + dir.getName()
						+ " directory");

			file = new File(dir.getPath() + File.separatorChar + "conjecture"
					+ "_inf.dot");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("digraph G {\n");
			for (ConjectureTransition t : getTransitions()) {
				writer.write("\t" + t.toDot() + "\n");
			}
			writer.write("}\n");
			writer.close();
			LogManager.logInfo("Conjecture have been exported to "
					+ file.getName());
			File imagePath = GraphViz.dotToFile(file.getPath());
			if (imagePath != null)
				LogManager.logImage(imagePath.getPath());
		} catch (IOException e) {
			LogManager.logException("Error writing dot file", e);
		}
	}

	public String firstInputUnknownFrom(StateLabel label) {
		List<ConjectureTransition> trlist = transitions.get(label);
		List<String> in = new LinkedList<>();
		in.addAll(inputs);
		System.out.println("trList :"+trlist);
		for(ConjectureTransition t : trlist){
			if(t.to.isQuestionMark) return t.input;
			in.remove(t.input);
		}
		if(!in.isEmpty()){
			return in.get(0);
		}
		return null;
		
	}

	public String transfertToNearestUncertainty(StateLabel rootLabel) {
		Set<StateLabel> alreadyVisited = new HashSet<>();
		List<StateLabel> queue = new LinkedList<>();
		Map<StateLabel,ConjectureTransition> history = new HashMap<>();
		queue.add(rootLabel);
		String in=null;
		StateLabel currentLabel = null;
		while(!queue.isEmpty()){
			currentLabel = queue.remove(0);
			alreadyVisited.add(currentLabel);
			in = firstInputUnknownFrom(currentLabel);
			if(in != null){
				break;
			}
			for(ConjectureTransition t : transitions.get(currentLabel)){
				if(!alreadyVisited.contains(t.to)){
					queue.add(t.to);
					history.put(t.to,t);
				}
			}
		}
		
		if(in == null )return null;
		ConjectureTransition t = history.get(currentLabel);
		while(!t.from.equals(rootLabel)){
			t = history.get(t.from);
		}
		
		return t.input;
	}

	public Mealy convertToMealy(StateLabel initialState) {
		Mealy mealy = new Mealy("conjecture");
		Map<StateLabel,State> stateMap = new HashMap<>();
		stateMap.put(initialState, mealy.addState(true));
		
		//PB!!!!!!!!!!!!!!!!!!!!!!
		Set<StateLabel> set2 = transitions.keySet();
		Set<StateLabel> set= new HashSet<>(set2);
		set.remove(initialState);
		for(StateLabel sl : set){
			stateMap.put(sl, mealy.addState());
		}
		for(List<ConjectureTransition> list : transitions.values()){
			for(ConjectureTransition t : list){
				mealy.addTransition(new MealyTransition(mealy, stateMap.get(t.from), stateMap.get(t.to), t.input, t.output));
			}
		}
		
		return mealy;
	}
	
	public String toString(){
		String res = "Conjecture :{";
		for(Entry<StateLabel,List<ConjectureTransition>> e : transitions.entrySet()){
			res += e.getKey().name+" [";
			for(ConjectureTransition t : e.getValue()){
				res += t.toString()+ "  ";
			}
			res += "]; ";
		}
		res += "}";
		return res;
	}
	
	public void merge(StateLabel l1, StateLabel l2,List<StateLabel> labelling) throws InconsistencyException {
		System.out.println("merge "+l1.name+" and "+l2.name);
		
		if(! l1.equals(l2)){
			if(!l1.isQuestionMark && !l2.isQuestionMark){
				throw new InconsistencyException();
			}
			if(!l1.isQuestionMark){
				replaceAll(l2,l1,labelling);
				List<ConjectureTransition> transFrom = getTransitionsFrom(l2);
				if(transFrom != null){
					for(ConjectureTransition old : transFrom){
						treatTransition(new ConjectureTransition(l1,old.to,old.input,old.output), labelling);
					}
					transitions.remove(l2);
				}
				
				for(ConjectureTransition old : getTransitionsTo(l2)){
					treatTransition(new ConjectureTransition(old.from,l1,old.input,old.output), labelling);
				}
			}
			else {
				replaceAll(l1,l2,labelling);
				List<ConjectureTransition> transFrom = getTransitionsFrom(l1);
				if(transFrom != null){
					for(ConjectureTransition old : transFrom){
						treatTransition(new ConjectureTransition(l2,old.to,old.input,old.output),labelling);
					}
					transitions.remove(l1);
				}
				
				for(ConjectureTransition old : getTransitionsTo(l1)){
					treatTransition(new ConjectureTransition(old.from,l2,old.input,old.output),labelling);
				}
			}
			
			
		}
		
		
	}
	
	public static void replaceAll(StateLabel old, StateLabel newl, List<StateLabel> in){
		ListIterator<StateLabel> iter =in.listIterator();
		
		while(iter.hasNext()){
			StateLabel current = iter.next();
			if(current.equals(old)){
				iter.previous();
				iter.remove();
				iter.add(newl);
			}
		}
	}
	
	public void treatTransition( ConjectureTransition t,List<StateLabel> labelling) throws InconsistencyException {
		if(!transitions.containsKey(t.from)){
			List<ConjectureTransition> newList = new LinkedList<>();
			newList.add(t);
			transitions.put(t.from, newList);
		}
		else {
			ConjectureTransition simt = findSimilarTransition(t);
			if(simt == null){
				transitions.get(t.from).add(t);
			}
			else{
				if(!simt.output.equals(t.output)){
					throw new InconsistencyException();
				}
				if(!simt.to.equals(t.to)){
					merge(simt.to,t.to,labelling);
				}
			}
		}
	}

	public String findFirstTransferInputTo(StateLabel origin, StateLabel destination) {
		System.out.println("find transfer from "+origin.name+" to "+destination.name);
		Set<StateLabel> alreadyVisited = new HashSet<>();
		List<StateLabel> queue = new LinkedList<>();
		Map<StateLabel,ConjectureTransition> history = new HashMap<>();
		queue.add(origin);
		StateLabel currentLabel = null;
		while(!queue.isEmpty()){
			currentLabel = queue.remove(0);
			alreadyVisited.add(currentLabel);
			if(currentLabel.equals(destination)) break;
			for(ConjectureTransition t : transitions.get(currentLabel)){
				if(!alreadyVisited.contains(t.to)){
					queue.add(t.to);
					history.put(t.to,t);
				}
			}
		}
		
		ConjectureTransition t = history.get(currentLabel);
		while(!t.from.equals(origin)){
			t = history.get(t.from);
		}
		return t.input;
	}
	


}
