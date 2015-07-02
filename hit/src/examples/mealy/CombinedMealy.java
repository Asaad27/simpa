package examples.mealy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import main.simpa.Options;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class CombinedMealy extends Mealy implements Serializable {
	private static final long serialVersionUID = 4685322377371L;

	private Mealy m1;
	private Mealy m2;
	
	public CombinedMealy(Mealy m1, Mealy m2){
		super("Combined("+m1.getName()+","+m2.getName()+")");
		this.m1 = m1;
		this.m2 = m2;
		LogManager.logStep(LogManager.STEPOTHER, "Generate product of "+m1.getName() +" and "+m2.getName());
		Map<State,Map<State,State>> combinedStates = new HashMap<State,Map<State,State>>();
		for (State s1 : m1.getStates()){
			Map<State,State> s1States = new HashMap<State,State>();
			for (State s2 : m2.getStates()){
				State s1s2State = new State(s1.getName()+"_"+s2.getName(),
						s1.isInitial() && s2.isInitial());
				s1States.put(s2, s1s2State);
				addState(s1s2State);
			}
			combinedStates.put(s1, s1States);
		}
		for (MealyTransition t1 : m1.getTransitions()){
			for (State s2 : m2.getStates()){
				State from = combinedStates.get(t1.getFrom()).get(s2);
				State to = combinedStates.get(t1.getTo()).get(s2);
				MealyTransition t = new MealyTransition(this, from, to, t1.getInput(), t1.getOutput());
				addTransition(t);
			}
		}
		for (MealyTransition t2 : m2.getTransitions()){
			for (State s1 : m1.getStates()){
				State from = combinedStates.get(s1).get(t2.getFrom());
				State to = combinedStates.get(s1).get(t2.getTo());
				MealyTransition t = new MealyTransition(this, from, to, t2.getInput(), t2.getOutput());
				addTransition(t);
			}
		}
		if (!Options.TEST) exportToDot();
	}
	
	public static void serialize(CombinedMealy o) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(Options.OUTDIR + o.getName()
					+ ".serialized");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.flush();
			oos.close();
			fos.close();
		} catch (Exception e) {
			LogManager.logException("Error serializing generated Mealy", e);
		}
	}

	public static CombinedMealy deserialize(String filename) {
		Object o = null;
		File f = new File(filename);
		LogManager.logStep(LogManager.STEPOTHER, "Loading Randommealy from "
				+ f.getName());
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			o = ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			LogManager.logException("Error deserializing generated Mealy", e);
		}
		return (CombinedMealy) o;
	}


}
