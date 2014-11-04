package tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class HTTPRequest {
	public static enum Method {
		GET, POST
	};

	public static enum Version {
		v11, v10
	};

	Method method;
	String url;
	Version version;
	HashMap<String, String> headers;
	String content;

	public HTTPRequest(String url) {
		this.method = Method.GET;
		this.url = url;
		this.version = Version.v11;
		headers = new HashMap<String, String>();
		content = "";
	}

	public HTTPRequest(Method m, String u, Version v) {
		this.method = m;
		this.url = u;
		this.version = v;
		headers = new HashMap<String, String>();
		content = "";
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(method.name() + " " + url + " HTTP/"
				+ (version == Version.v10 ? "1.0" : "1.1") + "\n");
		for (String key : headers.keySet()) {
			s.append(key + ": " + headers.get(key) + "\n");
		}
		s.append('\n');
		s.append(content);
		return s.toString();
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public String getHeader(String header) {
		return headers.get(header);
	}

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	public void addData(String key, String value) {
		try {
			if (content.length() > 0)
				content += "&";
			content += key + "=" + URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
