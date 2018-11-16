package examples.mealy;

import java.util.concurrent.Semaphore;

import tools.Utils;

import main.simpa.Options;
import main.simpa.Options.LogLevel;
import drivers.ExhaustiveGenerator;

import automata.mealy.Mealy;
import automata.mealy.MealyTransition;

public class EnumeratedMealy extends Mealy implements ExhaustiveGenerator {
	private static final long serialVersionUID = -4011527445498341153L;

	private static Semaphore produced = new Semaphore(0);
	private static Semaphore waiting = new Semaphore(0);

	private static Mealy lastComputed = null;

	private static ProducerThread producer = new ProducerThread();

	static class ProducerThread extends Thread {
		private long tried=0;
		private int statesNb;
		private int inputNb;
		private int outputNb;
		private String[] inputSymbols;
		private String[] outputSymbols;
		private EnumeratedMealy automata;
		protected boolean allDone=false;
		
		private long seed=0;
		


		public ProducerThread() {
			automata = new EnumeratedMealy();
			statesNb = Options.MAXSTATES;
			if (statesNb <= 0)
				throw new RuntimeException(
						"A valid number of states is needed.");
			for (int i = 0; i < statesNb; i++) {
				automata.addState(i == 0);
			}

			String s = "a";
			inputNb = Options.MAXINPUTSYM;
			inputSymbols = new String[inputNb];
			for (int i = 0; i < inputNb; i++) {
				inputSymbols[i] = s;
				s = Utils.nextSymbols(s);
			}
			int o = 0;
			outputNb = Options.MAXOUTPUTSYM;
			outputSymbols = new String[outputNb];
			for (int i = 0; i < outputNb; i++) {
				outputSymbols[i] = String.valueOf(o++);
			}
		}

		public void run() {
			try {
				waiting.acquire();
				addTransitionsFromState(1, 0, 0);
				allDone=true;
				lastComputed=null;
				produced.release();//give back hand to learner to let him notice that all conjectures where tried.
			System.out.println("\nGenerator made "+tried+" automata");
			} catch (InterruptedException e) {
				System.err.println("generetor interupted before generating all automata");
				throw new RuntimeException (e);
			}
		}

		private void addTransitionsFromState(long seedFactor, int currentState,
				int maxDiscoveredState) throws InterruptedException {
			if (currentState == statesNb) {
				if (seed!=0)
					throw new RuntimeException("invalid seed");
				lastComputed = automata;//for multi-learner, we need to clone automaton here.
				tried++;
				produced.release();
				waiting.acquire();
			} else {
				addTransitions(seedFactor, currentState, maxDiscoveredState, 0);
			}
		}

		private void addTransitions(long seedFactor, int currentState,
				int maxDiscoveredState, int currentInput)
				throws InterruptedException {
			if (currentInput == inputNb)
				addTransitionsFromState(seedFactor, currentState + 1,
						maxDiscoveredState);
			else {
				int newMaxDiscoveredState = maxDiscoveredState;
				int minState = 0;
				if (currentState == maxDiscoveredState
						&& currentInput == inputNb - 1
						&& currentState != statesNb - 1)
					minState = maxDiscoveredState + 1;
				long stateSeedFactor = (statesNb - minState) * seedFactor;
				for (int i = minState; i < statesNb; i++) {
//					if (seed!=0){
//						i=seed
//					}
					if (i > maxDiscoveredState + 1)
						break;
					if (i == maxDiscoveredState + 1)
						newMaxDiscoveredState = maxDiscoveredState + 1;
					automata.seed += i * seedFactor;
					long outputSeedFactor = stateSeedFactor * outputNb;
					for (int output = 0; output < outputNb; output++) {
						if (currentInput == 0 && currentState == 0
								&& output != 0)
							continue;
						automata.seed += output * stateSeedFactor;
						MealyTransition t = new MealyTransition(automata,
								automata.getState(currentState),
								automata.getState(i),
								inputSymbols[currentInput],
								outputSymbols[output]);
						automata.addTransition(t);
						addTransitions(outputSeedFactor, currentState,
								newMaxDiscoveredState, currentInput + 1);
						automata.removeTransition(t);
						automata.seed -= output * stateSeedFactor;
					}
					automata.seed -= i * seedFactor;
				}
			}
		}
	}

	protected long seed = 0;

	public long getSeed() {
		return seed;
	}

	private EnumeratedMealy() {
		super("Enumerated");
	}

	static int a=0;
	static public Mealy getConnexMealy() {
		if (!producer.isAlive())
			producer.start();
		Mealy automaton = null;
do {
		do {
			waiting.release();
			produced.acquireUninterruptibly();
			if (producer.allDone)
				throw new EndOfLoopException();
			automaton = lastComputed;
		} while (!automaton.isConnex());
a++;
}while(((EnumeratedMealy)automaton).seed!=1310113);
		if (Options.getLogLevel() == LogLevel.ALL)
			automaton.exportToDot();
		return automaton;
	}
}

//	class NotCannonicMealyException extends RuntimeException {
//		private static final long serialVersionUID = 6742678886697876367L;
//
//		public NotCannonicMealyException(String what) {
//			super(what);
//		}
//	}
//
//	private List<String> inputSymbols = null;
//	private List<String> outputSymbols = null;
//	private int nbStates;
//	private long seed = 0;
//
//	public EnumeratedMealy() {
//		this(Options.SEED);
//	}
//
//	public EnumeratedMealy(long seed) {
//		super("Enumerated");
//		this.seed = seed;
//		Utils.setSeed(seed);
//		LogManager.logStep(LogManager.STEPOTHER, "Generating enumerated Mealy");
//		seed = Utils.randLong();
//		generateSymbols();
//		createStates(true);
//		createTransitions();
//		if (!Options.TEST)
//			exportToDot();
//		// RandomMealy.serialize(this);
//	}
//
//	private void generateSymbols() {
//		int nbSym = 0;
//		String s = "a";
//		inputSymbols = new ArrayList<String>();
//		nbSym = Utils.randIntBetween(Options.MININPUTSYM, Options.MAXINPUTSYM);
//		for (int i = 0; i < nbSym; i++) {
//			inputSymbols.add(s);
//			s = Utils.nextSymbols(s);
//		}
//		int o = 0;
//		outputSymbols = new ArrayList<String>();
//		nbSym = Utils
//				.randIntBetween(Options.MINOUTPUTSYM, Options.MAXOUTPUTSYM);
//		for (int i = 0; i < nbSym; i++) {
//			outputSymbols.add(String.valueOf(o++));
//		}
//	}
//
//	private void createStates(boolean verbose) {
//		nbStates = Options.MAXSTATES;
//		for (int i = 0; i < nbStates; i++)
//			addState(i == 0);
//		if (verbose)
//			LogManager.logInfo("Number of states : " + nbStates);
//	}
//
//	static long pow(long a, int b) {
//		if (b == 0)
//			return 1;
//		if (b == 1)
//			return a;
//		if (b % 2 == 0)
//			return pow(a * a, b / 2); // even a=(a^2)^b/2
//		else
//			return a * pow(a * a, b / 2); // odd a=a*(a^2)^b/2
//
//	}
//
//	private void createTransitions() {
//		if (seed < 0) {
//			throw new RuntimeException("seed must be positive");
//		}
//		int inputsNb = inputSymbols.size();
//		int outputsNb = outputSymbols.size();
//
//		long intRepr = seed;
//
//		int outputStatesReprSize = 1;
//		for (int i = 0; i < nbStates - 1; i++) {
//			long encSize = inputsNb * pow(i + 2, inputsNb - 1);
//			outputStatesReprSize *= encSize;
//		}
//		// last state=
//		outputStatesReprSize *= pow(nbStates, inputsNb);
//		long outputStatesRepr = intRepr % outputStatesReprSize;
//		intRepr /= outputStatesReprSize;
//
//		long outputSymbolsReprSize = pow(outputsNb, nbStates * inputsNb);
//		outputSymbolsReprSize /= outputsNb;// forced first output
//
//		long outputSymbolsRepr = intRepr % outputSymbolsReprSize;
//		intRepr /= outputSymbolsReprSize;
//
//		if (intRepr > 0)
//			throw new TooBigSeedException(seed);
//
//		for (int i = 0; i < nbStates - 1; i++) {
//			long encSize = inputsNb * pow(i + 2, inputsNb - 1);
//			long enc = outputStatesRepr % encSize;
//			outputStatesRepr /= encSize;
//			long discoveringInput = enc % inputsNb;
//			enc /= inputsNb;
//			for (int j = 0; j < inputsNb; j++) {
//				State s1 = states.get(i);
//				State s2;
//				if (j == discoveringInput) {
//					s2 = states.get(i + 1);
//				} else {
//					long s = enc % (i + 2);
//					enc /= (i + 2);
//					if (j < discoveringInput && s == i + 1)
//						throw new NotCannonicMealyException(
//								"seed is not a canonical representation");
//					s2 = states.get((int) s);
//				}
//				long output = outputSymbolsRepr % outputsNb;
//				if (i == 0 && j == 0)
//					output = 0;
//				else {
//					outputSymbolsRepr /= outputsNb;
//				}
//				MealyTransition t = new MealyTransition(this, s1, s2,
//						inputSymbols.get(j), outputSymbols.get((int) output));
//				addTransition(t);
//			}
//
//		}
//		// last state:
//		long encSize = pow(nbStates, inputsNb);
//		long enc = outputStatesRepr % encSize;
//		outputStatesRepr /= encSize;
//		for (int j = 0; j < inputsNb; j++) {
//			State s1 = states.get(nbStates - 1);
//			State s2;
//			long s = enc % nbStates;
//			enc /= nbStates;
//			s2 = states.get((int) s);
//
//			long output = outputSymbolsRepr % outputsNb;
//			outputSymbolsRepr /= outputsNb;
//
//			MealyTransition t = new MealyTransition(this, s1, s2,
//					inputSymbols.get(j), outputSymbols.get((int) output));
//			addTransition(t);
//		}
//
//	}
//
//	public long getSeed() {
//		return seed;
//	}
//
//	static public EnumeratedMealy getConnexMealy() {
//		EnumeratedMealy automaton = null;
//		while (automaton == null) {
//			try {
//				automaton = new EnumeratedMealy();
//
//			} catch (NotCannonicMealyException e) {
//				Options.SEED++;
//				continue;
//			}
//			if (!automaton.isConnex()) {
//				Options.SEED++;
//				automaton = null;
//			}
//		}
//		return automaton;
//	}
//}
