/********************************************************************************
 * Copyright (c) 2012,2019 Institut Polytechnique de Grenoble 
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors:
 *     Karim HOSSEN
 ********************************************************************************/
package tools;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;

import tools.loggers.LogManager;

public class UDPSend {

	public static String Send(String host, int port, Request request) {
		byte[] buffer = new byte[2048];
		Arrays.fill(buffer, (byte) 0);
		LogManager.logInfo("Sending request");
		try {
			CSeqHeader nSeq = (CSeqHeader) request.getHeader("CSeq");
			DatagramSocket sendSocket = new DatagramSocket();
			sendSocket.setSoTimeout(10000);

			DatagramPacket dataSend = new DatagramPacket(request.toString()
					.getBytes(), request.toString().getBytes().length,
					InetAddress.getByName(host), port);
			DatagramPacket dataRecv = new DatagramPacket(buffer, buffer.length);

			sendSocket.send(dataSend);
			if (request.getMethod().equals(Request.ACK)){
				sendSocket.close();
				return "Timeout";
			} else {
				int code = -1;
				long seq = -1;
				if (request.getMethod().equals("INVITE")) {
					while (code < 200 || (code > 300 && code < 400)) {
						sendSocket.receive(dataRecv);
						code = Integer.parseInt(new String(buffer, 0, dataRecv
								.getLength()).split("\n")[0].split(" ")[1]);
					}
				} else {
					while (seq != nSeq.getSeqNumber()) {
						sendSocket.receive(dataRecv);
						String[] headers = new String(buffer, 0,
								dataRecv.getLength()).split("\n");
						for (String h : headers) {
							if (h.startsWith("CSeq")) {
								seq = Integer.parseInt(h.substring(6,
										h.lastIndexOf(" ")));
								break;
							}
						}
					}
				}
			}

			sendSocket.close();

			return new String(buffer, 0, dataRecv.getLength());
		} catch (SocketTimeoutException e) {
			return "Timeout";
		} catch (Exception e) {
			LogManager.logException("Unable to connect to the system (" + host
					+ ":" + port + ")", e);
		}
		LogManager.logInfo("Request sent");
		return null;
	}
}
