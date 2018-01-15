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
import main.simpa.Options.LogLevel;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

/**
 * Create a product of two automata.
 * 
 * If the automata have the same set of input symbols, this will be a synchronous product.
 * If they have distinct set of input symbols, it will be an asynchronous product.
 * @author nbremond
 *
 */
public class CombinedMealy extends Mealy implements Serializable {
	private static final long serialVersionUID = 4685322377371L;

	private Mealy m1;
	private Mealy m2;
	
	public CombinedMealy(Mealy m1, Mealy m2){
		super("Combined("+m1.getName()+";"+m2.getName()+")");
		this.m1 = m1;
		this.m2 = m2;
		LogManager.logStep(LogManager.STEPOTHER, "Generate product of "+m1.getName() +" and "+m2.getName());
		Map<State,Map<State,State>> combinedStates = createStates();
		createTransitions(combinedStates);
		if (Options.LOG_LEVEL==LogLevel.ALL) exportToDot();
	}
	
	private Map<State, Map<State, State>> createStates(){
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
		return combinedStates;
	}
	
	private void createTransitions(Map<State,Map<State,State>> combinedStates){
		for (int i = 0; i < 2; i++){
			Mealy m_a, m_b;
			if (i == 0){
				m_a = m1;
				m_b = m2;
			}else{
				m_a = m2;
				m_b = m1;
			}
			for (MealyTransition t_a : m_a.getTransitions()){
				for (State s_b : m_b.getStates()){
					State from;
					if (i == 0)
						from = combinedStates.get(t_a.getFrom()).get(s_b);
					else
						from = combinedStates.get(s_b).get(t_a.getFrom());
					State to_b;
					String output = t_a.getOutput();
					MealyTransition t_b = m_b.getTransitionFromWithInput(s_b, t_a.getInput());
					if (t_b == null){ // we are making an asynchronous product
						to_b = s_b;
					}else{
						to_b = t_b.getTo();
						output += "_x_" + t_b.getOutput();
					}
					State to;
					if (i == 0)
						to = combinedStates.get(t_a.getTo()).get(to_b);
					else
						to = combinedStates.get(to_b).get(t_a.getTo());
					MealyTransition t = new MealyTransition(this, from, to, t_a.getInput(),output);
					if (getTransitionFromWithInput(from, t_a.getInput()) == null)//because in case of synchronous product we will add to times the transition with reversed output
						addTransition(t);
				}
			}
		}
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
