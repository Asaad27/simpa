package drivers.proxy;

public class ProxyData {
	String request = null;
	String response = null;
	int ready = OK;
	
	static int NOT_READY = -1;
	static int OK = 0;
	
	public ProxyData(int msg){
		this.request = null;
		this.response = null;
		ready = NOT_READY;
	}
	
	public ProxyData(String req, String resp){
		this.request = req;
		this.response = resp;
		ready = OK;
	}

	public String getRequest() {
		return request;
	}

	public String getResponse() {
		return response;
	}
}

