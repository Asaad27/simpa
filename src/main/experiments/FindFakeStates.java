package main.experiments;

import automata.mealy.Mealy;
import drivers.Driver;
import drivers.mealy.MealyDriver;
import drivers.mealy.transparent.RandomMealyDriver;
import examples.mealy.RandomMealy;
import learner.Learner;
import learner.mealy.LmConjecture;
import learner.mealy.hW.HWLearner;
import learner.mealy.hW.HWOptions;
import learner.mealy.hW.HWStatsEntry;
import main.simpa.Options;
import options.valueHolders.SeedHolder;
import org.apache.commons.collections.BagUtils;
import stats.StatsWriter;
import tools.GraphViz;
import tools.loggers.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FindFakeStates {
    public static final int ITERATIONS = 100;
    private int fakeStatesFound = 0;

    private void searchForFakeStates(HWOptions options) {
        LogManager.start();
        try (StatsWriter statsLog = new StatsWriter()) {
            for (int i = 0; i < ITERATIONS; ++i) {
                SeedHolder seedHolder = new SeedHolder("find fake states");
                seedHolder.initRandom();
                Mealy automaton = new RandomMealy(seedHolder, true);
                RandomMealyDriver driver =  new RandomMealyDriver(automaton);
                HWLearner learner = new HWLearner(driver, options);

                learner.learn();


                LmConjecture conjecture = learner.createConjecture();

                boolean isConjectureCorrect = driver.searchConjectureError(conjecture);
                if (!isConjectureCorrect) {
                    System.out.println("Conjecture not correct");
                }
                HWStatsEntry stats = learner.getStats();
                if (stats.get(HWStatsEntry.MAX_FAKE_STATES) != 0) {
                    System.out.println("Found fake state");
                    saveAutomaton(automaton);
                }
                statsLog.append(stats);
                learner.logStats();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LogManager.end();
        }
    }

    private void saveAutomaton(Mealy automaton) {
        Path path = Paths.get("samples/fakestates5_3_3");
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path file = path.resolve("fakeStateSample" + fakeStatesFound++ + ".dot");
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            automaton.writeInDotFormat(writer, "");
            File imagePath = GraphViz.dotToFile(file.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //automata options
        Options.MINSTATES = 5;
        Options.MAXSTATES = 5;
        Options.MININPUTSYM = 3;
        Options.MAXINPUTSYM = 3;
        Options.MINOUTPUTSYM = 3;
        Options.MAXOUTPUTSYM = 3;


        //hW options
        HWOptions options = new HWOptions(null);
        options.searchCeInTrace.setEnabled(false);
        options.addHInW.setEnabled(false);
        options.useReset.setEnabled(false);
        options.checkInconsistenciesHMapping.setEnabled(false);
        options.useDictionary.setEnabled(false);

        FindFakeStates main = new FindFakeStates();
        main.searchForFakeStates(options);
    }
}
