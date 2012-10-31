package drivers.efsm.real;

import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;
import drivers.efsm.EFSMDriver;

/*
 * Driver - Help
 * 
 * The driver class must extend driver.web.WebDriver.
 * The others classes is reserved for testing or other algorithms (not used in SPaCIoS)
 * 
 * For an example of a complete class see WGStoredXSSDriver.java
 * 
 * The contructor defines the ip address and port to access the system
 * Then it can calls initConnection to be able to connect to the system (Basic auth for Webgoat)
 * 
 * getOutputSymbols / getInputSymbols functions returns a list of symbols (String)
 * 
 * Each symbols have at least one parameter, the default values is specified by the getDefaultParamValues function.
 * And their name is defined in the getParameterNames function.
 * 
 * abstractToConcrete take ParameterizedInput (one symbol and its parameter) as input and return the corresponding HTTP request
 * 
 * concreteToAbstract take HTTPResponse as input and return the corresponding abstract response (one symbol and its parameter)
 *  
 */

public abstract class RealDriver extends EFSMDriver{
	public String systemHost;
	public int systemPort;
	
	public RealDriver(){
		super(null);
	}

	public abstract ParameterizedOutput execute(ParameterizedInput pi);
	
}
