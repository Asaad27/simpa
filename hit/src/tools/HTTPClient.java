package tools;

import tools.HTTPRequest.Method;
import tools.HTTPRequest.Version;

public class HTTPClient {

	private String creds = null;
	private CookieManager cookie = null;
	private String host;
	private int port;

	public HTTPClient(String host) {
		cookie = new CookieManager();
		this.host = host;
	}

	public HTTPClient(String host, int port) {
		this(host);
		this.port = port;
	}

	private HTTPRequest buildRequest(Method m, String url) {
		HTTPRequest req = new HTTPRequest(m, url, Version.v11);
		if (creds != null)
			req.addHeader("Authorization", creds);
		if (!cookie.isEmpty())
			req.addHeader("Cookie", cookie.getCookieLine());
		return req;
	}

	public HTTPResponse get(String url) {
		HTTPRequest req = buildRequest(Method.GET, url);
		HTTPResponse resp = new HTTPResponse(TCPSend.Send(host, port, req));
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp.getCode() == 303) {
			req.setUrl(resp.getHeader("Location"));
			resp = new HTTPResponse(TCPSend.Send(host, port, req));
		}
		return resp;
	}

	public HTTPResponse post(String url) {
		HTTPRequest req = buildRequest(Method.POST, url);
		HTTPResponse resp = new HTTPResponse(TCPSend.Send(host, port, req));
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp.getCode() == 303) {
			req.setUrl(resp.getHeader("Location"));
			resp = new HTTPResponse(TCPSend.Send(host, port, req));
		}
		return resp;
	}

	public HTTPResponse get(String url, HTTPData data) {
		HTTPRequest req = buildRequest(Method.GET, url);
		for (String key : data.getData().keySet()) {
			req.addData(key, data.getData().get(key));
		}
		HTTPResponse resp = new HTTPResponse(TCPSend.Send(host, port, req));
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp.getCode() == 303) {
			req.setUrl(resp.getHeader("Location"));
			resp = new HTTPResponse(TCPSend.Send(host, port, req));
		}
		return resp;
	}

	public HTTPResponse post(String url, HTTPData data) {
		HTTPRequest req = buildRequest(Method.POST, url);
		for (String key : data.getData().keySet()) {
			req.addData(key, data.getData().get(key));
		}
		HTTPResponse resp = new HTTPResponse(TCPSend.Send(host, port, req));
		cookie.updateCookies(resp.getHeader("Set-Cookie"));
		if (resp.getCode() == 303) {
			req.setUrl(resp.getHeader("Location"));
			resp = new HTTPResponse(TCPSend.Send(host, port, req));
		}
		return resp;
	}

	public void setCredentials(String username, String password) {
		creds = "Basic "
				+ Base64.encodeBytes((username + ":" + password).getBytes());
	}

	public void reset() {
		cookie.reset();
	}

	public void clearCredentials(String username, String password) {
		creds = null;
	}
}
