package drivers.efsm.real;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import tools.HTTPRequest;
import tools.HTTPRequest.Method;
import tools.HTTPRequest.Version;
import tools.HTTPResponse;
import tools.Utils;
import tools.loggers.LogManager;
import automata.efsm.Parameter;
import automata.efsm.ParameterizedInput;
import automata.efsm.ParameterizedOutput;

public class WGStoredXSSDriver extends LowWebDriver {

	private String basicAuth = "Basic Z3Vlc3Q6Z3Vlc3Q="; // guest:guest in
															// base64
	private String screen = null;

	public WGStoredXSSDriver() {
		super();
		this.systemHost = "localhost";
		this.systemPort = 8080;
		initConnection();
	}

	public void initConnection() {
		LogManager.logInfo("Initializing connection to the system");

		HTTPRequest res = new HTTPRequest(Method.GET, "/WebGoat/attack",
				Version.v11);
		res.addHeader("Authorization", basicAuth);
		HTTPResponse resp = executeWeb(res);
		cookie.updateCookies(resp.getHeader("Set-Cookie"));

		res = new HTTPRequest(Method.POST, "/WebGoat/attack", Version.v11);
		res.addHeader("Authorization", basicAuth);
		res.addHeader("Cookie", cookie.getCookieLine());
		res.addData("start", "Start WebGoat");
		resp = executeWeb(res);
		cookie.updateCookies(resp.getHeader("Set-Cookie"));

		screen = extractScreen(resp.toString(), "Stage 1: Stored XSS");

		LogManager.logInfo("Ready to infer");
	}

	private String extractScreen(String content, String lesson) {
		int pos = content.indexOf("LAB: Cross Site Scripting");
		pos = content.indexOf("Screen=", pos);
		return content.substring(pos + 7, content.indexOf("&", pos));
	}

	public ArrayList<String> getInputSymbols() {
		ArrayList<String> is = new ArrayList<String>();
		is.add("login");
		is.add("logout");
		is.add("viewProfile");
		is.add("editProfile");
		is.add("xssProfile");
		return is;
	}

	public ArrayList<String> getOutputSymbols() {
		ArrayList<String> os = new ArrayList<String>();
		os.add("listing");
		os.add("home");
		os.add("profilePage");
		os.add("editionPage");
		return os;
	}

	@Override
	public String getSystemName() {
		return "WebGoat Stored XSS";
	}

	public HashMap<String, List<ArrayList<Parameter>>> getDefaultParamValues() {
		HashMap<String, List<ArrayList<Parameter>>> defaultParamValues = new HashMap<String, List<ArrayList<Parameter>>>();
		ArrayList<ArrayList<Parameter>> params = null;

		// Login
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(
					new Parameter("101", Types.STRING), new Parameter("larry",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("111", Types.STRING), new Parameter("larry",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("101", Types.STRING), new Parameter("john",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("111", Types.STRING), new Parameter("john",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("111", Types.STRING), new Parameter("foo",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("101", Types.STRING), new Parameter("foo",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("666", Types.STRING), new Parameter("foo",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("666", Types.STRING), new Parameter("larry",
							Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("666", Types.STRING), new Parameter("john",
							Types.STRING)));
			defaultParamValues.put("login", params);
		}

		// ViewProfile
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils
					.createArrayList(new Parameter("101", Types.STRING)));
			params.add(Utils
					.createArrayList(new Parameter("111", Types.STRING)));
			defaultParamValues.put("viewProfile", params);
		}

		// EditProfile
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils
					.createArrayList(new Parameter("101", Types.STRING)));
			params.add(Utils
					.createArrayList(new Parameter("111", Types.STRING)));
			defaultParamValues.put("editProfile", params);
		}

		// XSSProfile
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils.createArrayList(
					new Parameter("101", Types.STRING), new Parameter(
							"<script>alert(1);</script>", Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("111", Types.STRING), new Parameter(
							"<script>alert(1);</script>", Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("111", Types.STRING), new Parameter(
							"22 Foo Street", Types.STRING)));
			params.add(Utils.createArrayList(
					new Parameter("101", Types.STRING), new Parameter(
							"22 Foo Street", Types.STRING)));
			defaultParamValues.put("xssProfile", params);
		}

		// Logout
		{
			params = new ArrayList<ArrayList<Parameter>>();
			params.add(Utils
					.createArrayList(new Parameter("101", Types.STRING)));
			params.add(Utils
					.createArrayList(new Parameter("111", Types.STRING)));
			defaultParamValues.put("logout", params);
		}

		return defaultParamValues;
	}

	@Override
	public void reset() {
		super.reset();
		HTTPRequest res = new HTTPRequest(Method.GET, "/WebGoat/attack?Screen="
				+ screen + "&menu=900&Restart=" + screen, Version.v11);
		res.addHeader("Authorization", basicAuth);
		res.addHeader("Cookie", cookie.getCookieLine());
		executeWeb(res);
	}

	public HTTPRequest abstractToConcrete(ParameterizedInput pi) {
		HTTPRequest req = null;
		if (!pi.isEpsilonSymbol()) {
			LogManager.logInfo("Abstract : " + pi);

			if (pi.getInputSymbol().equals("login")) {
				req = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="
						+ screen + "&menu=900", Version.v11);
				req.addData("employee_id", pi.getParameterValue(0));
				req.addData("password", pi.getParameterValue(1));
				req.addData("action", "Login");

			} else if (pi.getInputSymbol().equals("viewProfile")) {
				req = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="
						+ screen + "&menu=900", Version.v11);
				req.addData("employee_id", pi.getParameterValue(0));
				req.addData("action", "ViewProfile");

			} else if (pi.getInputSymbol().equals("editProfile")) {
				req = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="
						+ screen + "&menu=900", Version.v11);
				req.addData("employee_id", pi.getParameterValue(0));
				req.addData("action", "EditProfile");

			} else if (pi.getInputSymbol().equals("xssProfile")) {
				req = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="
						+ screen + "&menu=900", Version.v11);
				req.addData("employee_id", pi.getParameterValue(0));
				req.addData("action", "UpdateProfile");
				req.addData("address1", pi.getParameterValue(1));
				req.addData("address2", "New York, NY");
				req.addData("ccn", "2578546969853547");
				req.addData("ccnLimit", "5000");
				req.addData("description", "Does not work well with others");
				req.addData("disciplinaryDate", "10106");
				req.addData("disciplinaryNotes",
						"Constantly harassing coworkers");
				req.addData("firstName", "Larry");
				req.addData("lastName", "Stooge");
				req.addData("manager", "101");
				req.addData("phoneNumber", "443-689-0192");
				req.addData("salary", "55000");
				req.addData("ssn", "386-09-5451");
				req.addData("startDate", "1012000");
				req.addData("title", "Technician");

			} else if (pi.getInputSymbol().equals("logout")) {
				req = new HTTPRequest(Method.POST, "/WebGoat/attack?Screen="
						+ screen + "&menu=900", Version.v11);
				req.addData("employee_id", pi.getParameterValue(0));
				req.addData("action", "Logout");
			} else {
				LogManager
						.logError("AbstractToConcrete method is missing for symbol : "
								+ pi.getInputSymbol());
			}

			if (req != null) {
				req.addHeader("Authorization", basicAuth);
				if (!cookie.isEmpty())
					req.addHeader("Cookie", cookie.getCookieLine());
				LogManager.logConcrete(req.toString());
			}
		} else {
			LogManager
					.logError("AbstractToConcrete for Epsilon symbol is impossible in "
							+ pi.getInputSymbol());
		}
		return req;
	}

	public ParameterizedOutput concreteToAbstract(HTTPResponse resp) {
		ParameterizedOutput po = null;
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp == null || resp.getCode() == 404 || resp.getCode() == 503
				|| resp.getCode() == 500) {
			po = new ParameterizedOutput();
		} else if (resp.getCode() == 200) {
			po = new ParameterizedOutput();
			if (resp.getContent().contains("Staff Listing Page")) {
				po = new ParameterizedOutput("listing");
				po.getParameters().add(
						new Parameter(resp.getCodeString(), Types.STRING));
			} else if (resp.getContent().contains("<div id=\"lesson_login\">")) {
				po = new ParameterizedOutput("home");
				po.getParameters().add(
						new Parameter(resp.getCodeString(), Types.STRING));
			} else if (resp.getContent().contains("value=\"UpdateProfile\"")) {
				po = new ParameterizedOutput("editionPage");
				po.getParameters().add(
						new Parameter(resp.getCodeString(), Types.STRING));
			} else if (resp.getContent().contains("Credit Card Limit")) {
				po = new ParameterizedOutput("profilePage");
				po.getParameters().add(
						new Parameter(resp.getCodeString(), Types.STRING));
			} else {
				LogManager
						.logError("ConcreteToAbstract method is missing for this page");
				LogManager.logConcrete(resp.toString());
			}
		}

		LogManager.logInfo("Abstract : " + po);
		return po;
	}

	@Override
	public TreeMap<String, List<String>> getParameterNames() {
		TreeMap<String, List<String>> defaultParamNames = new TreeMap<String, List<String>>();
		defaultParamNames.put("login",
				Utils.createArrayList("profileIdLogin", "passwordLogin"));
		defaultParamNames.put("viewProfile",
				Utils.createArrayList("profileIdProfile"));
		defaultParamNames.put("editProfile",
				Utils.createArrayList("profileIdEdit"));
		defaultParamNames.put("xssProfile",
				Utils.createArrayList("profileIdXSS", "XSSPayload"));
		defaultParamNames.put("logout",
				Utils.createArrayList("profileIdLogout"));

		defaultParamNames.put("listing", Utils.createArrayList("codeListing"));
		defaultParamNames.put("home", Utils.createArrayList("codeHome"));
		defaultParamNames.put("profilePage",
				Utils.createArrayList("codeProfilePage"));
		defaultParamNames.put("editionPage",
				Utils.createArrayList("codeEditionPage"));
		return defaultParamNames;
	}
}
