package examples.mealy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import main.Options;
import tools.Utils;
import tools.loggers.LogManager;
import automata.State;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class RandomMealy extends Mealy implements Serializable {	
	private static final long serialVersionUID = -4610287835922377376L;
	
	private List<String> inputSymbols = null;
	private List<String> outputSymbols = null;
	
	public static String replaceCharAt(String s, int pos, char c) {
		  StringBuffer buf = new StringBuffer( s );
		  buf.setCharAt( pos, c );
		  return buf.toString( );
	}
	
	private void generateSymbols() {
		int nbSym = 0;
		String s = "a";
		inputSymbols = new ArrayList<String>();
		nbSym = Utils.randIntBetween(Options.MININPUTSYM, Options.MAXINPUTSYM);
		for(int i=0; i<nbSym; i++){
			inputSymbols.add(s);
			s = Utils.nextSymbols(s);
		}
		int o = 0;
		outputSymbols = new ArrayList<String>();
		nbSym = Utils.randIntBetween(Options.MININPUTSYM, Options.MAXINPUTSYM);
		for(int i=0; i<nbSym; i++){
			outputSymbols.add(String.valueOf(o++));
		}
	}

	public RandomMealy() {
		super("Random");
		LogManager.logStep(LogManager.STEPOTHER, "Generating random Mealy");
		generateSymbols();
		createStates();
		createTransitions();
		//exportToDot();
		//RandomMealy.serialize(this);
	}

	public static void serialize(RandomMealy o) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(Options.OUTDIR + o.getName() + ".serialized");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.flush();
			oos.close();
			fos.close();
		} catch (Exception e){
			LogManager.logException("Error serializing generated Mealy", e);
		}
	}
	
	public static RandomMealy deserialize(String filename){
		Object o = null;
		File f = new File(filename);
		LogManager.logStep(LogManager.STEPOTHER, "Loading Randommealy from " + f.getName());
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois= new ObjectInputStream(fis);
			o = ois.readObject();
			ois.close();
			fis.close();
			}catch (Exception e) {
				LogManager.logException("Error deserializing generated Mealy", e);
			}
		return (RandomMealy)o;
	}

	private void createTransitions() {
		for(State s1 : states){
			for (String is : inputSymbols){
				if (Utils.randBoolWithPercent(50)){
					addTransition(new MealyTransition(this, s1, Utils.randIn(states), is, Utils.randIn(outputSymbols)));
				}else{
					addTransition(new MealyTransition(this, s1, s1, is, Utils.randIn(outputSymbols)));
				}
			}
		}
	}

	private void createStates() {
		int nbStates = Utils.randIntBetween(Options.MINSTATES, Options.MAXSTATES);
		for (int i=0; i<nbStates; i++) addState(i==0);
		LogManager.logInfo("Number of states : " + nbStates);
	}

}
