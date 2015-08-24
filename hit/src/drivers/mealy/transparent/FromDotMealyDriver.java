package drivers.mealy.transparent;

import java.io.File;
import java.io.IOException;

import automata.mealy.Mealy;

public class FromDotMealyDriver extends TransparentMealyDriver {

	public FromDotMealyDriver(File f) throws IOException {
		super(Mealy.importFromDot(f));
		automata.exportToDot();
	}
}
