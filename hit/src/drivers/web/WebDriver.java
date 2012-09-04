package drivers.web;

import tools.CookieManager;
import tools.HTTPRequest;
import tools.HTTPResponse;
import tools.TCPSend;
import tools.loggers.LogManager;
import automata.Automata;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedInputSequence;
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

public abstract class WebDriver extends EFSMDriver{
	public String systemHost;
	public int systemPort;
	
	public CookieManager cookie;
	
	public WebDriver(){
		super(null);
		this.cookie = new CookieManager();
	}
	
	public HTTPResponse executeWeb(HTTPRequest req){
		return new HTTPResponse(TCPSend.Send(systemHost, systemPort, req));
	}
	
	public ParameterizedOutput execute(ParameterizedInput pi) {
		numberOfAtomicRequest++;
		HTTPRequest req = abstractToConcrete(pi);
		ParameterizedOutput po = new ParameterizedOutput();
		if (req != null) po = concreteToAbstract(executeWeb(req));
		LogManager.logRequest(pi, po);
		return po;
	}
	
	public abstract HTTPRequest abstractToConcrete(ParameterizedInput pi);
	
	public abstract ParameterizedOutput concreteToAbstract(HTTPResponse resp);
	
	public ParameterizedInputSequence getCounterExample(Automata a){
		return null;		
	}
	
	public boolean isCounterExample(Object ce, Object conjecture){
		return false;
	}
}
