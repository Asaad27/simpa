package drivers.mealy.gui;

import de.fzi.guitesting.UPMDriver;
import drivers.mealy.PartialMealyDriver;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class UPMDriverOption extends DriverChoiceItem<PartialMealyDriver> {
    public UPMDriverOption(DriverChoice<?> parent) {
        super("uiUPMDriver", "uiUPMDriver", parent, PartialMealyDriver.class);
    }

    @Override
    public PartialMealyDriver createDriver() {
        return new UIDriverWrapper(new UPMDriver());
    }
}
