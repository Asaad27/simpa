package drivers.mealy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import learner.mealy.LmConjecture;
import tools.Utils;
import tools.loggers.LogManager;
import automata.Automata;
import automata.State;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import automata.mealy.OutputSequence;
import drivers.Driver;

public class MealyDriver extends Driver{
	protected Mealy automata;
	protected State currentState;
	protected List<InputSequence> forcedCE;
	private int nbStates = 0;
	private String name = null;
	
	public MealyDriver(Mealy automata){
		super();
		type = DriverType.MEALY;
		this.automata = automata;
		this.forcedCE = getForcedCE();
		this.nbStates = automata.getStateCount();
		this.name = automata.getName();
	}
	
	public MealyDriver(String name) {
		this.name = name;
	}

	public List<String> getStats(){
		return Utils.createArrayList(
				String.valueOf(nbStates),
				String.valueOf(getInputSymbols().size()),
				String.valueOf(getOutputSymbols().size()),
				String.valueOf(((float)numberOfAtomicRequest/numberOfRequest)),
				String.valueOf(numberOfRequest),
				String.valueOf(((float)duration/1000000000)));
	}
	
	protected List<InputSequence> getForcedCE() {
		return null;
	}

	public String execute(String input) {
		String output = null;
		if (input.length()>0){
			if (addtolog) numberOfAtomicRequest++;
			MealyTransition currentTrans = null;
			for(MealyTransition t : (List<MealyTransition>)automata.getTransitions()){
				if (t.getFrom().equals(currentState) && t.getInput().equals(input)){
					currentTrans = t;
					break;
				}
			}
			if (currentTrans != null){
				output = new String(currentTrans.getOutput());
				currentState = currentTrans.getTo();					
			}else{
				output = new String();
			}
			if (addtolog) LogManager.logRequest(input, output);
		}
		return output;
	}
	
	public List<String> getInputSymbols(){
		List<String> is = new ArrayList<String>();
		for(MealyTransition t : automata.getTransitions()){
			if (!is.contains(t.getInput())) is.add(t.getInput());
		}
		Collections.sort(is);
		return is;
	}
	
	public List<String> getOutputSymbols(){
		List<String> os = new ArrayList<String>();
		for(MealyTransition t : automata.getTransitions()){
			if (!os.contains(t.getOutput())) os.add(t.getOutput());
		}
		Collections.sort(os);
		return os;		
	}
		
	@Override
	public String getSystemName(){
		return name;
	}
	
	public InputSequence getCounterExample(Automata c){
		LogManager.logInfo("Searching counter example");
		boolean found = false;
		InputSequence ce = null;
		if (forcedCE != null && !forcedCE.isEmpty()){
			found = true;
			ce = forcedCE.remove(0);
			LogManager.logInfo("Counter example found (forced) : " + ce);
		}else{
			LmConjecture conj = (LmConjecture)c;
			int maxTries = 100000;
			List<String> is = getInputSymbols();
			MealyDriver conjDriver = new MealyDriver(conj);
			stopLog();
			conjDriver.stopLog();
			int i = 0;
			while(i<maxTries && !found){
				ce = InputSequence.generate(is, Utils.randIntBetween(1, 12));
				OutputSequence osSystem = new OutputSequence();
				OutputSequence osConj = new OutputSequence();
				reset();
				conjDriver.reset();
				if (ce.getLength()>0){
					for(String input : ce.sequence){
						String _sys = execute(input);
						String _conj = conjDriver.execute(input);
						if (_sys.length()>0){
							osSystem.addOutput(_sys);
							osConj.addOutput(_conj);
						}
						if (!_sys.equals(_conj) && !osSystem.getLastSymbol().isEmpty()){
							found = true;
							ce = ce.getIthPreffix(osSystem.getLength());
							LogManager.logInfo("Counter example found : " + ce);
							LogManager.logInfo("On system : " + osSystem);
							LogManager.logInfo("On conjecture : " + osConj);
							break;
						}				
					}
					i++;
				}
			}
			startLog();
			conjDriver.startLog();
		}

		if (!found) LogManager.logInfo("No counter example found");
		
		return (found?ce:null);		
	}
		
	@Override
	public void reset(){
		super.reset();
		automata.reset();
		currentState = automata.getInitialState();
	}

	public boolean isCounterExample(Object ce, Object c) {
		if (ce == null) return false;
		InputSequence realCe = (InputSequence)ce;
		LmConjecture conj = (LmConjecture)c;
		MealyDriver conjDriver = new MealyDriver(conj);
		stopLog();
		conjDriver.stopLog();
		reset();
		conjDriver.reset();
		boolean isCe = false;
		for(int i=0; i<realCe.getLength(); i++){
			for(String input : realCe.sequence){
				if (!execute(input).equals(conjDriver.execute(input))) { isCe = true; break; };	
			}
		}
		startLog();
		conjDriver.startLog();
		return isCe;
	}	
}
