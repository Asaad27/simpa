package drivers.mealy;

import java.io.File;
import java.io.IOException;

import automata.mealy.Mealy;
import options.FileOption;
import options.FileOption.FileExistance;
import options.FileOption.FileSelectionMode;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class FromDotMealyDriver extends AutomatonMealyDriver {
	public static class FromDotChoiceItem
			extends DriverChoiceItem<MealyDriver> {

		FileOption file;

		public FromDotChoiceItem(DriverChoice<MealyDriver> parent) {
			super(parent, FromDotMealyDriver.class);
			file = new FileOption("--loadDotFile",
					"Select the file to load as driver.", null,
					FileSelectionMode.FILES_ONLY, FileExistance.MUST_EXIST);
			subTrees.add(file);
		}

		@Override
		public MealyDriver createDriver() {
			try {
				return new FromDotMealyDriver(file.getcompletePath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public FromDotMealyDriver(File f) throws IOException {
		super(Mealy.importFromDot(f));
		automata.exportToDot();
	}
}
