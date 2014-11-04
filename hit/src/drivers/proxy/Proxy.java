package drivers.proxy;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

	public class Handler implements Runnable {
		boolean in;
		Thread myThread = null;
		private BufferedInputStream socketInput = null;
		private PrintWriter socketOutput = null;

		public Handler(boolean in, Socket inSocket, Socket outSocket) {
			this.in = in;
			try {
				socketInput = new BufferedInputStream(inSocket.getInputStream());
				socketOutput = new PrintWriter(outSocket.getOutputStream(),
						true);
			} catch (Exception e) {
				System.err.println("Exception creating i/o streams:" + e);
				e.printStackTrace();
			}
			myThread = new Thread(null, this, "proxy");
			myThread.start();
		}

		public void run() {
			int bytesRead = 0;
			int bufLen = 10000;
			byte buf[] = new byte[bufLen];
			while (true) {
				try {
					bytesRead = socketInput.read(buf, 0, bufLen);
					if (bytesRead > 0) {
						String tmp = new String(buf, 0, bytesRead);
						System.out.println(tmp);
						if (in)
							Proxy.req += tmp;
						else
							Proxy.resp += tmp;
						socketOutput.write(tmp);
						socketOutput.flush();
					} else if (bytesRead < 0) {
						System.err.println("Socket closed on us");
						return;
					} else {
						System.err
								.println("Socket did something weird - returned 0 bytes read instead of blocking");
						return;
					}
				} catch (Exception e) {
					System.err.println("Exception creating serverSocket:" + e);
					e.printStackTrace();
				}
			}
		}
	}

	String destName = null;
	int destPort = 0;
	public static String req;
	public static String resp;
	boolean ready;

	ServerSocket serverSocket = null;

	public Proxy(int listenPort, String destName, int destPort) {
		this.destName = destName;
		this.destPort = destPort;
		try {
			serverSocket = new ServerSocket(listenPort);
		} catch (Exception e) {
			System.err.println("Exception creating serverSocket:" + e);
			e.printStackTrace();
		}
	}

	public ProxyData getData() {
		if (ready)
			return new ProxyData(req, resp);
		else
			return new ProxyData(ProxyData.NOT_READY);
	}

	public void start() {
		while (true) {
			try {
				Socket inConnection = serverSocket.accept();
				System.err.println("New connection established with "
						+ inConnection);
				ready = false;
				resp = "";
				req = "";
				Socket outConnection = new Socket(destName, destPort);
				Handler inHandler = new Handler(true, inConnection,
						outConnection);
				Handler outHandler = new Handler(false, outConnection,
						inConnection);
				inHandler.myThread.join();
				outHandler.myThread.join();
				ready = true;
			} catch (Exception e) {
				System.err.println("Exception in go:" + e);
				e.printStackTrace();
			}
		}
	}
}
