package examples.mealy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import main.simpa.Options;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class CounterMealy extends Mealy implements Serializable {
	private static final long serialVersionUID = 4685322377371L;

	private String noResetSymbol;
	private String ResetSymbol;
	
	public CounterMealy(int nbStates, String inputSymbol){
		super("Counter_"+inputSymbol+"_"+nbStates);
		LogManager.logStep(LogManager.STEPOTHER, "Generate Counter Mealy");
		noResetSymbol = "NoReset";
		ResetSymbol = "Reset";
		createStates(nbStates, true,inputSymbol);
		createTransitions(inputSymbol);
		if (!Options.TEST) exportToDot();
	}
	
	private void createTransitions(String inputSymbol){
		addTransition(new MealyTransition(this, states.get(states.size()-1), states.get(0), inputSymbol, ResetSymbol));
		for (int i = 1; i < states.size(); i ++){
			State s2 = states.get(i);
			State s1 = states.get(i-1);
			addTransition(new MealyTransition(this, s1, s2, inputSymbol, noResetSymbol));
			
		}
	}

	private void createStates(int nbStates, boolean verbose, String inputSymbol) {
		for (int i = 0; i < nbStates; i++)
			addState(new State(inputSymbol + i, i==0));;
		if (verbose) LogManager.logInfo("Number of states : " + nbStates);
	}

	public static void serialize(CounterMealy o) {
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

	public static CounterMealy deserialize(String filename) {
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
		return (CounterMealy) o;
	}


}
