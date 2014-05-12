package LLTest;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;
import de.learnlib.algorithms.dhc.mealy.MealyDHC;
import de.learnlib.algorithms.lstargeneric.AbstractLStar;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.SUL;
import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.cache.mealy.MealyCacheOracle;
import de.learnlib.cache.sul.SULCaches;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;
import de.learnlib.statistics.StatisticSUL;
import drivers.mealy.MealyDriver;
import examples.mealy.TestLaurent2;
import automata.mealy.InputSequence;
import automata.mealy.Mealy;

/**
 * An experiment with learnlib, with a run() method.
 * @author Laurent
 *
 */


public class LLRunner {
	
	CsvBuffer buffer;
	int automatonNumber;
	int automatonTotal;
	Alphabet<String> inputs ;
	String algo;
	String CEMethod;
	String useDictionary;
	String closingStrategy;
	
	public LLRunner(String filename,Alphabet<String> alphabet,int automatonTotal,String algo,String CEMethod,String closingStrategy,String useDictionary){
		buffer = new CsvBuffer(filename,"id,states,requests,rounds,CElength,number of suffixes");
		automatonNumber =0;
		inputs = alphabet;
		this.automatonTotal = automatonTotal;
		this.algo=algo;
		this.CEMethod=CEMethod;
		this.useDictionary=useDictionary;
		this.closingStrategy=closingStrategy;
	}
	
	public void run(Mealy automaton, List<InputSequence> z) throws IOException{
		automatonNumber++;   
		System.out.println("LL "+automatonNumber);

        // create an oracle that can answer membership queries
        // using SIMPA
        MealyDriver md = new MealyDriver(automaton);
        SUL<String,String> sul = new Driver(md);
        
        // oracle for counting queries wraps sul
        StatisticSUL<String, String> statisticSul = new ResetCounterSUL<String,String>("membership queries", sul);
        
        SUL<String,String> effectiveSul = statisticSul;
        // use caching in order to avoid duplicate queries
        //effectiveSul = Caches.createSULCache(inputs, sul);
        /*
        MealyMembershipOracle<String,String> mqOracle =null;
        
        mqOracle = new SULOracle<String,String>(effectiveSul);
        */
        
        /*
        effectiveSul = SULCaches.createCache(inputs, sul);
        MealyMembershipOracle<String,String> mqOracle = new SULOracle<>(effectiveSul);
*/
        
        
        MealyMembershipOracle<String,String> mqOracle =null;
		switch (useDictionary) {
		case ("WITHOUT"):
			mqOracle = new SULOracle<String, String>(effectiveSul);
			break;
		case ("LLDIC"):
			effectiveSul = SULCaches.createCache(inputs, statisticSul);
			mqOracle = new SULOracle<>(effectiveSul);
			System.out.println("LLDIC");
			break;
		case ("NEWDIC"):
			mqOracle = new DictionnaryTreeOracle<String, String>(effectiveSul, inputs);
			break;
		}
       
        
       // MealyCacheOracle<String,String> mqOracle =Caches.createMealyCache(inputs,
         //       mqOracleaux);
        
        // create initial set of suffixes
        
        List<Word<String>> suffixes = new ArrayList<Word<String>>();
      //put all single letters of the alphabet
        /*
        for(String s : inputs){
        	suffixes.add(Word.fromSymbols(s));
        }
        */
        //or let the list empty
        if(z!=null){
        	for(InputSequence s : z){
        		suffixes.add(Word.fromList(s.sequence));
        	}
        }
        

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table 
        // instead of single outputs.
        ClosingStrategy<Object, Object> cs = null;
        switch(closingStrategy){
        case("CF"):
        	cs = ClosingStrategies.CLOSE_FIRST ;
        break;
        case("CLM"):
        	cs = ClosingStrategies.CLOSE_LEX_MIN;
        break;
        case("CR"):
        	cs = ClosingStrategies.CLOSE_RANDOM;
        break;
        case("CS"):
        	cs = ClosingStrategies.CLOSE_SHORTEST;
        break;
        }
        MealyLearner<String,String> lstar=null;
        switch(algo){
        case("L*"):
        	switch(CEMethod){
        	case("C"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.CLASSIC_LSTAR, // handling of counterexamples
                cs 
                );
        	break;
        	case("FL"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.FIND_LINEAR, // handling of counterexamples
                cs
                );
        	break;
        	case("FLA"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.FIND_LINEAR_ALLSUFFIXES, // handling of counterexamples
                cs
                );
        	break;
        	case("FLR"):
        		lstar =
                new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.FIND_LINEAR_REVERSE, // handling of counterexamples
                cs
                );
        	break;
        	case("FLRA"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.FIND_LINEAR_REVERSE_ALLSUFFIXES, // handling of counterexamples
                cs
                );
        	break;
        	case("MP"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.MAHLER_PNUELI, // handling of counterexamples
                cs
                );
        	break;
        	case("RS"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.RIVEST_SCHAPIRE, // handling of counterexamples
                cs
                );
        	break;
        	case("RSA"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.RIVEST_SCHAPIRE_ALLSUFFIXES, // handling of counterexamples
                cs
                );
        	break;
        	case("SH"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.SHAHBAZ, // handling of counterexamples
                cs
                );
        	break;
        	case("1B1"):
        		lstar =
                (MealyLearner<String, String>) new ExtensibleLStarMealy<String,String>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.SUFFIX1BY1, // handling of counterexamples
                cs
                );
        	break;
        	}
        	
        break;
        case("DHC"):
        	lstar =
             new MealyDHC<String,String>(
             inputs, // input alphabet
             mqOracle
             );
        break;
        }
       
        long LLseed = System.currentTimeMillis();
        System.out.println(LLseed);
        // create random walks equivalence test
        RandomWalkEQOracle<String,String> randomWalks =
                new RandomWalkEQOracle<String,String>(
                0.05, // reset SUL w/ this probability before a step 
                100000, // max steps (overall)
                false, // reset step count after counterexample 
                new Random(LLseed), // make results reproducible 
                sul // system under learning
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        MealyExperiment<String,String> experiment =
                new MealyExperiment<String,String>(lstar, randomWalks, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
       //System.out.println("cache size :"+mqOracle.getCacheSize());
        experiment.run();

        // get learned model
        MealyMachine<?, String, ?, String> result = experiment.getFinalHypothesis();
        
        /*
        if(useDictionary){
        	System.out.println("**************************");
            System.out.println("checkConsistency :"+((DictionnaryOracle<String, String>) mqOracle).checkConsistency());
            System.out.println("**************************");
        }
        */
        
        // writing statistics
        int cardZ = ((AbstractLStar<MealyMachine<?, String, ?, String>, String, Word<String>>) lstar).getObservationTable().getSuffixes().size();
        String[] s = statisticSul.getStatisticalData().getSummary().split(" ");
        System.out.println(Arrays.toString(s));
        String[] t = experiment.getRounds().getSummary().split(" ");
        buffer.write(automatonNumber,result.size(),new Integer(s[s.length-1]),new Integer(t[t.length-1]),randomWalks.getMaxCElength(),cardZ);
        
        if(automatonNumber==automatonTotal){
        	buffer.close();
        }
        
       
        /*
        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        Writer w = DOT.createDotWriter(true);
        GraphDOT.write(result, inputs, w);
        w.close();
       */
        

	}

}


