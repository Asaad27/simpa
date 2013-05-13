package crawler.driver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tools.HTTPData;
import tools.HTTPRequest;
import tools.HTTPResponse;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import crawler.Input;
import crawler.Input.Type;
import crawler.Output;
import crawler.configuration.Configuration;
import drivers.efsm.real.LowWebDriver;

public abstract class GenericDriver extends LowWebDriver {
	
	protected WebClient client = null;
	public static Configuration config = null; 
	public List<Input> inputs;
	public List<Output> outputs;
	
	public GenericDriver(String xml) throws IOException{
		inputs = new ArrayList<Input>();
		outputs = new ArrayList<Output>();
		config = LoadConfig(xml);
		
		client = new WebClient();
		client.setThrowExceptionOnFailingStatusCode(false);
		client.setTimeout(2000);
		BasicCredentialsProvider creds = new BasicCredentialsProvider();
		if (config.getBasicAuthUser() != null
				&& config.getBasicAuthPass() != null) {
			creds.setCredentials(
					new AuthScope(config.getHost(), config.getPort()),
					new UsernamePasswordCredentials(config.getBasicAuthUser(),
							config.getBasicAuthPass()));
		}
		client.setCredentialsProvider(creds);
		initConnection();
		updateParameters();
	}
	
	protected abstract void updateParameters();

	public String getSystemName(){
		return config.getName();
	}
	
	public ParameterizedOutput execute(ParameterizedInput pi) {
		numberOfAtomicRequest++;
		Input in = inputs.get(Integer.parseInt(pi.getInputSymbol().substring(pi.getInputSymbol().indexOf("_")+1)));
		
		String source = null;
		try {
			source = submit(in, pi);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		ParameterizedOutput po = null;
		Output out = new Output(source, false);
		for (int i = 0; i < outputs.size(); i++) {
			if (out.isEquivalentTo(outputs.get(i))) {
				po = new ParameterizedOutput(getOutputSymbols().get(i));
				for(String p : outputs.get(i).getParams()){
					po.getParameters().add(new Parameter(extractParam(out, p), Types.STRING));
				}
			}
		}
		
		if (po != null){
			LogManager.logRequest(pi, po);
			return po;
		}else{
			System.out.println("wtf");
			System.out.println(source);
			System.out.println(out.getPageTree());
			return null;
		}		
	}
	
	private String extractParam(Output out, String p) {
		String path[] = p.split("/");
		org.jsoup.nodes.Element e = out.getDoc().get(Integer.parseInt(path[0]));
		for(int i=1; i<path.length; i++){
			e = e.child(Integer.parseInt(path[i]));
		}
		return e.text();
	}

	private HTTPData getValuesForInput(Input in, ParameterizedInput pi) {
		HTTPData data = new HTTPData();
		if (in.getType() == Type.FORM) {
			HashMap<String, List<String>> inputs = in.getParams();
			int i = 0;
			for (String key : inputs.keySet()) {
				data.add(key, pi.getParameterValue(i++));
			}
		}
		return data;
	}
	
	private String submit(Input in, ParameterizedInput pi) throws MalformedURLException{
		WebRequest request = null;
		HTTPData values = getValuesForInput(in, pi);
		if (in.getType()==Type.FORM){
			request = new WebRequest(new URL(in.getAddress()), in.getMethod());
			request.setRequestParameters(values.getNameValueData());
			
			HtmlPage page;
			try {
				page = client.getPage(request);
			} catch (Exception e) {
				return null;
			}
			return page.asXml();
		}else if (in.getType()==Type.LINK){
			String link = in.getAddress() + "?";
			if (!in.getParams().isEmpty()){
				for(String name : in.getParams().keySet()){
					for(String value : in.getParams().get(name)){
						link += name + "=" + value + "&";
					}
				}
			}
			HtmlPage page;
			try {
				page = client.getPage(link.substring(0, link.length()-1));
			} catch (Exception e) {
				return null;
			}
			return page.asXml();
		}
		return null;
	}	
	
	private Configuration LoadConfig(String xml) throws IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		config = new Configuration();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document dom = db.parse(xml);
			Element root = dom.getDocumentElement();
			config.setName(root.getElementsByTagName("target").item(0).getTextContent());
			config.setHost(root.getElementsByTagName("host").item(0).getTextContent());
			config.setPort(Integer.parseInt(root.getElementsByTagName("port").item(0).getTextContent()));
			config.setBasicAuthUser(root.getElementsByTagName("basicAuthUser").item(0).getTextContent());
			config.setBasicAuthPass(root.getElementsByTagName("basicAuthPass").item(0).getTextContent());
			config.setLimitSelector(root.getElementsByTagName("limitSelector").item(0).getTextContent());
			ArrayList<String> rtParam = new ArrayList<String>();
			Node n = root.getElementsByTagName("runtimeParameters").item(0);
			for(int i=0; i<n.getChildNodes().getLength(); i++){
				if (n.getChildNodes().item(i).getNodeName().equals("parameter")) rtParam.add(n.getChildNodes().item(i).getTextContent());
			}			
			config.setRuntimeParameters(rtParam);
			
			NodeList inputs = root.getElementsByTagName("inputs").item(0).getChildNodes();
			for (int i=0; i<inputs.getLength(); i++){
				if (inputs.item(i).getNodeName().equals("input")){
					Input in = new Input();
					in.setType(Type.valueOf(inputs.item(i).getAttributes().getNamedItem("type").getNodeValue()));
					in.setAddress(inputs.item(i).getAttributes().getNamedItem("address").getNodeValue());
					in.setMethod(HttpMethod.valueOf(inputs.item(i).getAttributes().getNamedItem("method").getNodeValue()));
					in.setType(Type.valueOf(inputs.item(i).getAttributes().getNamedItem("type").getNodeValue()));			
					
					for(int j=0; j<inputs.item(i).getChildNodes().item(1).getChildNodes().getLength(); j++){
						if (inputs.item(i).getChildNodes().item(1).getChildNodes().item(j).getNodeName().equals("parameter")){
							String name = inputs.item(i).getChildNodes().item(1).getChildNodes().item(j).getAttributes().getNamedItem("name").getNodeValue();
							String value = inputs.item(i).getChildNodes().item(1).getChildNodes().item(j).getTextContent();
							if (in.getParams().get(name) == null) in.getParams().put(name, new ArrayList<String>());
							in.getParams().get(name).add(value);
						}
							
					}
					this.inputs.add(in);
				}
			}
			
			NodeList outputs = root.getElementsByTagName("outputs").item(0).getChildNodes();
			for (int i=0; i<outputs.getLength(); i++){
				if (outputs.item(i).getNodeName().equals("output")){
					Output out = new Output(outputs.item(i).getChildNodes().item(1).getTextContent(), true);					
					for(int j=0; j<outputs.item(i).getChildNodes().item(3).getChildNodes().getLength(); j++){
						if (outputs.item(i).getChildNodes().item(3).getChildNodes().item(j).getNodeName().equals("parameter")){
							String value = outputs.item(i).getChildNodes().item(3).getChildNodes().item(j).getTextContent();
							out.getParams().add(value);
						}							
					}
					System.out.println(out.getPageTree());
					System.out.println("-----------------------------------");
					this.outputs.add(out);
				}
			}
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return config;
	}
	

	@Override
	public HTTPRequest abstractToConcrete(ParameterizedInput pi) {
		// TODO Auto-generated method stub
		return null;
	}

	public ParameterizedOutput concreteToAbstract(Output out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();		
		ArrayList<ArrayList<Parameter>> params = null;
		
		int index = 0;
		for(Input i : inputs)
		{
			params = new ArrayList<ArrayList<Parameter>>();
			// Only one
			ArrayList<Parameter> one = new ArrayList<Parameter>();
			for(String key : i.getParams().keySet()){
				one.add(new Parameter(i.getParams().get(key).get(0), Types.STRING));
			}
			params.add(one);
			defaultParamValues.put("input_" + String.valueOf(index), params);
			index++;
		}
		return defaultParamValues;
	}
	
	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> res = new TreeMap<String, List<String>>();
		int index = 0;
		for(Input i : inputs)
		{
			List<String> names = new ArrayList<String>();
			for(String key : i.getParams().keySet()){
				names.add(key);
			}
			res.put("input_" + String.valueOf(index), names);
			index++;
		}
		index = 0;
		for(Output o : outputs)
		{
			List<String> names = new ArrayList<String>();
			for(int n=0; n< o.getParams().size(); n++){
				names.add("output_" + String.valueOf(index) + "_param" + String.valueOf(n));
			}
			res.put("output_" + String.valueOf(index), names);
			index++;
		}
		return res;
	}
	
	@Override
	public List<String> getInputSymbols() {
		List<String> is = new ArrayList<String>();
		for (int i=0; i<inputs.size(); i++){
			is.add("input_" + i);
		}
		return is;
	}

	@Override
	public List<String> getOutputSymbols() {
		List<String> os = new ArrayList<String>();
		for (int i=0; i<outputs.size(); i++){
			os.add("output_" + i);
		}
		return os;
	}

	@Override
	public ParameterizedOutput concreteToAbstract(HTTPResponse resp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public abstract void initConnection();

}
