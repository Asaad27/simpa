package tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

import tools.HTTPRequest.Method;
import tools.loggers.LogManager;

public class TCPSend {
	public static String Send(String host, int port, HTTPRequest request) {
		StringBuffer fromServer = new StringBuffer();
		LogManager.logInfo("Sending request");
		try {			
			Socket clientSocket;
			clientSocket = new Socket(host, port);
			request.addHeader("Host", host+":"+port);
			request.addHeader("Connection", "close");
			request.addHeader("Accept-Encoding", "identity");
			
			if (request.method == Method.POST){
				request.addHeader("Content-Type", "application/x-www-form-urlencoded");
				request.addHeader("Content-Length", String.valueOf(request.content.length()));
			}else{
				if (!request.content.isEmpty()) request.url += request.content;
			}
			
			BufferedOutputStream outToServer = new BufferedOutputStream(clientSocket.getOutputStream());
			BufferedInputStream inFromServer = new BufferedInputStream(
					clientSocket.getInputStream());

			outToServer.write(request.toString().getBytes());
			outToServer.flush();

			int bytesRead = 0;
			int bufLen = 20000;
			byte buf[] = new byte[bufLen];
			while (true) {
				bytesRead = inFromServer.read(buf, 0, bufLen);
				if (bytesRead != -1) fromServer.append(new String(buf, 0, bytesRead));
				else break;
			}
			clientSocket.close();
		} catch (Exception e) {
			LogManager.logException("Unable to connect to the system (" + host + ":" + port+")", e);
		}
		LogManager.logInfo("Request sent");
		return fromServer.toString();
	}
}
