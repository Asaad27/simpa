package tools;

import java.util.TreeMap;

public class HTTPResponse {
	public int getCode() {
		return code;
	}

	public String getCodeString() {
		return codeString;
	}

	public static enum Version {
		v11, v10
	};

	private Version version;
	private int code;
	private String codeString;
	private TreeMap<String, String> headers;
	private StringBuffer content;

	public HTTPResponse(String response) {
		String[] resp = response.split("\n");
		String[] status = resp[0].trim().split(" ");
		if (status[0].equals("HTTP/1.1"))
			this.version = Version.v11;
		else
			this.version = Version.v10;
		code = Integer.parseInt(status[1]);
		codeString = status[2];
		for (int i = 3; i < status.length; i++)
			codeString += " " + status[i];
		headers = new TreeMap<String, String>();
		int i;
		for (i = 1; !resp[i].trim().isEmpty(); i++) {
			headers.put(resp[i].trim().split(": ")[0].trim(), resp[i].trim()
					.split(": ")[1].trim());
		}
		if (resp.length > i + 1) {
			content = new StringBuffer(resp[++i].trim());
			for (i = i + 1; i < resp.length; i++)
				content.append("\n" + resp[i]);
		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("HTTP/" + (version == Version.v10 ? "1.0" : "1.1") + " "
				+ code + " " + codeString + "\n");
		for (String key : headers.keySet()) {
			s.append(key + ": " + headers.get(key) + "\n");
		}
		s.append("\n");
		s.append(content);
		s.append("\n");
		return s.toString();
	}

	public boolean isRedirection() {
		return headers.containsKey("Location");
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public String getContent() {
		return content.toString();
	}

	public void setContent(String content) {
		this.content = new StringBuffer(content);
	}

	public TreeMap<String, String> getHeaders() {
		return headers;
	}

	public String getHeader(String header) {
		return headers.get(header);
	}

	public void addHeader(String header, String value) {
		headers.put(header, value);
	}
}
