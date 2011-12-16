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
