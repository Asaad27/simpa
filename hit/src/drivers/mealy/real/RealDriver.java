package drivers.mealy.real;

import java.util.List;
import drivers.mealy.MealyDriver;

public abstract class RealDriver extends MealyDriver{

	public RealDriver(String name) {
		super(name);
		type = DriverType.MEALY;
	}
	
	public abstract void reset();
	
	public abstract List<String> getInputSymbols();
	
	public abstract List<String> getOutputSymbols();
	
	public abstract String execute(String input);
	

}
