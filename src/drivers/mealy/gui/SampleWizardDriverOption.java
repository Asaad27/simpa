package drivers.mealy.gui;

import de.fzi.guitesting.UIDriver;
import de.fzi.guitesting.sampleApp.Wizard;
import drivers.mealy.PartialMealyDriver;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class SampleWizardDriverOption extends DriverChoiceItem<PartialMealyDriver> {
    public SampleWizardDriverOption(DriverChoice<?> parent) {
        super("uiWizardDriver", "uiWizardDriver", parent, PartialMealyDriver.class);
    }

    @Override
    public PartialMealyDriver createDriver() {
        return new UIDriverWrapper(UIDriver.driverForApp(Wizard::new));
    }

}
