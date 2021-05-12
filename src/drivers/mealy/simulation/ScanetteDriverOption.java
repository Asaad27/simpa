package drivers.mealy.simulation;

import drivers.Driver;
import drivers.mealy.simulation.scanette.Scanette;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;

public class ScanetteDriverOption extends DriverChoiceItem<ScanetteDriver> {

    public ScanetteDriverOption(DriverChoice<?> parent) {
        super("ScanetteDriver", "scanetteDriver", parent, ScanetteDriver.class);
    }

    @Override
    public ScanetteDriver createDriver() {
        return new ScanetteDriver("scanette");
    }
}
