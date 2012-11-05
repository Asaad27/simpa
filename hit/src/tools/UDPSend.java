package tools;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import tools.loggers.LogManager;

public class UDPSend {

	public static String Send(String host, int port, String request) {
		byte[] buffer = new byte[2048];
		Arrays.fill(buffer, (byte)0);		
		LogManager.logInfo("Sending request");
		try {			
			DatagramSocket sendSocket = new DatagramSocket();
			sendSocket.setSoTimeout(5000);
			
			DatagramPacket dataSend = new DatagramPacket(request.getBytes(), request.getBytes().length, InetAddress.getByName(host), port);
			DatagramPacket dataRecv = new DatagramPacket(buffer, buffer.length);
			
			sendSocket.send(dataSend);
			sendSocket.receive(dataRecv);
			
			sendSocket.close();
		
			return new String(buffer, 0, dataRecv.getLength());
		} catch (SocketTimeoutException e) {
			return "Timeout";
		} catch (Exception e) {
			LogManager.logException("Unable to connect to the system (" + host + ":" + port+")", e);
		}
		LogManager.logInfo("Request sent");
		return null;
	}
}
