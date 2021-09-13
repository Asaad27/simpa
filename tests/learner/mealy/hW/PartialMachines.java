package learner.mealy.hW;

import drivers.mealy.CompleteMealyDriver;
import drivers.mealy.transparent.TransparentFromDotMealyDriver;
import learner.mealy.LmConjecture;
import options.valueHolders.SeedHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class PartialMachines {

    private HWOptions hwOptions;

    @BeforeEach
    void init() {
        hwOptions = new HWOptions(null);
        hwOptions.searchCeInTrace.setEnabled(false);
        hwOptions.addHInW.setEnabled(false);
        hwOptions.useReset.setEnabled(false);
        hwOptions.checkInconsistenciesHMapping.setEnabled(false);
        hwOptions.useDictionary.setEnabled(false);
        hwOptions.addIInW();
    }

    private Path getPath(Path outdir, int i) {
        return outdir.resolve(String.format("sample%02d.dot", i));
    }

    @Test
    void testSmallRandomSamples() throws IOException {
        Path outdir = Path.of("partial_automata").toAbsolutePath();

        for (int i = 0; i < 500; i++) {
            SeedHolder.setMainSeed(1);
            System.out.println(i);
            var driver = new TransparentFromDotMealyDriver(getPath(outdir, i).toFile());
            testMachine(driver);
        }

    }

    @Test
    void testMachine(CompleteMealyDriver driver) {
        HWLearner learner = new HWLearner(driver, hwOptions);

        learner.learn();

        LmConjecture conjecture = learner.createConjecture();

        boolean isConjectureCorrect = driver.searchConjectureError(conjecture);
        Assertions.assertTrue(isConjectureCorrect);
    }
}
