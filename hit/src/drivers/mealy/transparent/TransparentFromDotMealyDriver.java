package drivers.mealy.transparent;

import java.io.File;
import java.io.IOException;

import automata.mealy.Mealy;
import drivers.mealy.MealyDriver;
import options.FileOption;
import options.FileOption.FileExistance;
import options.FileOption.FileSelectionMode;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class TransparentFromDotMealyDriver extends TransparentMealyDriver {
	public static class FromDotChoiceItem
			extends DriverChoiceItem<MealyDriver> {

		FileOption file;

		public FromDotChoiceItem(DriverChoice<MealyDriver> parent) {
			super(parent, TransparentFromDotMealyDriver.class);
			file = new FileOption("--loadDotFile",
					"select the file to load as driver", null,
					FileSelectionMode.FILES_ONLY, FileExistance.MUST_EXIST);
			subTrees.add(file);
		}

		@Override
		public MealyDriver createDriver() {
			try {
				return new TransparentFromDotMealyDriver(
						file.getcompletePath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public TransparentFromDotMealyDriver(File f) throws IOException {
		super(Mealy.importFromDot(f));
		automata.exportToDot();
	}
}
