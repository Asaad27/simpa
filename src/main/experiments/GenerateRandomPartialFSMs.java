package main.experiments;

import automata.mealy.Mealy;
import automata.mealy.MealyTransition;
import examples.mealy.RandomMealy;
import learner.mealy.hW.HWOptions;
import main.simpa.Options;
import options.valueHolders.SeedHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static java.nio.file.StandardOpenOption.CREATE;

public class GenerateRandomPartialFSMs {
    private final HWOptions options = new HWOptions(null);
    private final Path outdir = Path.of("partial_automata").toAbsolutePath();

    public GenerateRandomPartialFSMs() {
        //automata options
        Options.MINSTATES = 50;
        Options.MAXSTATES = 50;
        Options.MININPUTSYM = 30;
        Options.MAXINPUTSYM = 30;
        Options.MINOUTPUTSYM = 40;
        Options.MAXOUTPUTSYM = 40;


    }

    private void generate(int n) throws IOException {

        Files.createDirectories(outdir);
        SeedHolder.setMainSeed(1);
        SeedHolder seedHolder = new SeedHolder("");
        seedHolder.initRandom();

        for (int i = 0; i < n; i++) {
            Mealy automaton = new RandomMealy(seedHolder, true);
            MealyTransition transitionToRemove;
            do {
                ArrayList<MealyTransition> transitions = new ArrayList<>(automaton.getTransitions());
                transitionToRemove = seedHolder.getRandomGenerator().randIn(transitions);
                automaton.removeTransition(transitionToRemove);
            } while (automaton.isConnex());
            automaton.addTransition(transitionToRemove);
            var serialized = automaton.asDotString("");

            Files.writeString(getPath(i), serialized, StandardOpenOption.TRUNCATE_EXISTING, CREATE);
        }
    }

    private Path getPath(int i) {
        return outdir.resolve(String.format("sample%02d.dot", i));
    }

    public static void main(String[] args) throws IOException {
        GenerateRandomPartialFSMs generator = new GenerateRandomPartialFSMs();
        generator.generate(500);
    }


}
