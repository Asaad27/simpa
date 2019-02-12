package drivers.mealy.real;

import drivers.mealy.MealyDriver;

public abstract class RealDriver extends MealyDriver {

	public RealDriver(String name) {
		super(name);
		type = DriverType.MEALY;
	}

}
