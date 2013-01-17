package drivers.mealy.real;

import java.util.ArrayList;
import java.util.List;

import tools.HTTPRequest;
import tools.HTTPResponse;
import tools.TCPSend;
import tools.Utils;
import tools.loggers.LogManager;

public class XSSDriver extends RealDriver{

	private static final String systemHost = "127.0.0.1";
	private static final int systemPort = 80;

	public XSSDriver() {
		super("XSS");
	}

	@Override
	public List<String> getInputSymbols() {
		return Utils.createArrayList("<", ">", "a", "0", "script", "/", "alert(1);", "fromCharCode", "A", "B");
	}
	
	public HTTPResponse executeWeb(HTTPRequest req){
		HTTPResponse rep = null;
		try{
			rep = new HTTPResponse(TCPSend.Send(systemHost, systemPort, req));
		}catch(Exception o){
			rep = null;
		}
		return rep;
	}
	
	public HTTPRequest abstractToConcrete(String in){
		return new HTTPRequest("http://" + systemHost + ":" + systemPort + "/xss.php?input=" + in);		
	}
	
	public String concreteToAbstract(HTTPResponse resp){
		if (resp != null) return resp.getContent();
		else return "0";
	}

	@Override
	public String execute(String input) {
		String output = concreteToAbstract(executeWeb(abstractToConcrete(input)));
		LogManager.logRequest(input, output);
		return output;
	}

}
