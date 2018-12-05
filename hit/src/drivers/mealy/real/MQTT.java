package drivers.mealy.real;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import drivers.mealy.real.RealDriver;
import tools.loggers.LogManager;

public class MQTT extends RealDriver {


	String topic = "SIMPA inference";
	String topic2 = "SIMPA inference2";
	int qos = 2;
	String broker = "tcp://localhost:1883";
	String clientId = "JavaSample";
	MemoryPersistence persistence = new MemoryPersistence();

	MqttClient sampleClient;
	MqttConnectOptions connOpts;

	public MQTT() {
		super("MQTT");
		try {
			sampleClient = new MqttClient(broker, clientId, persistence);
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
		connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		sampleClient.setCallback(new MqttCallback() {

			@Override
			public void messageArrived(String topic, MqttMessage message)
					throws Exception {
				received.add(message.toString());
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0) {

			}

			@Override
			public void connectionLost(Throwable arg0) {

			}
		});
	}

	LinkedList<String> received = new LinkedList<>();


	final static String CONNECT = "connect";
	final static String DISCONNECT = "disconnect";
	final static String PUBLISH_1 = "pub_1";
	final static String SUBSCRIBE_1 = "sub_1";
	final static String UNSUBSRIBE_1 = "unsub_1";
	final static String SUBSCRIBE_2 = "sub_2";
	final static String UNSUBSRIBE_2 = "unsub_2";

	@Override
	public void stopLog() {
	}

	@Override
	public List<String> getInputSymbols() {
		return Arrays.asList(CONNECT, DISCONNECT, PUBLISH_1, SUBSCRIBE_1,
				UNSUBSRIBE_1// , SUBSCRIBE_2, UNSUBSRIBE_2
		);
	}

	@Override
	public String execute(String input) {

		numberOfAtomicRequest++;
		String output = execute_intern(input);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (!received.isEmpty()) {
			output = output + ", m:" + received.poll();
		}
		LogManager.logRequest(input, output, numberOfAtomicRequest);
		System.out.println(numberOfAtomicRequest + ":" + input + "/" + output);
		return output;
	}

	public String execute_intern(String input) {
		if (input == CONNECT) {
			try {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sampleClient.connect(connOpts);
				return "Connected";
			} catch (MqttException e) {
				return "error";
			}
		} else if (input == PUBLISH_1) {
			MqttMessage message = new MqttMessage("publish1".getBytes());
			message.setQos(qos);
			try {
				sampleClient.publish(topic, message);
				return "published";
			} catch (MqttException e) {
				return "error";
			}
		} else if (input == DISCONNECT) {
			try {
				sampleClient.disconnect();
				return "disconnected";
			} catch (MqttException e) {
				return "error";
			}
		} else if (input == SUBSCRIBE_1) {
			try {
				sampleClient.subscribe(topic);
				return "ok";
			} catch (MqttException e) {
				return "error";
			}
		} else if (input == UNSUBSRIBE_1) {
			try {
				sampleClient.unsubscribe(topic);
				return "ok";
			} catch (MqttException e) {
				return "error";
			}
		} else if (input == SUBSCRIBE_2) {
			try {
				sampleClient.subscribe(topic2);
				return "ok";
			} catch (MqttException e) {
				return "error";
			}
		} else if (input == UNSUBSRIBE_2) {
			try {
				sampleClient.unsubscribe(topic2);
				return "ok";
			} catch (MqttException e) {
				return "error";
			}
		}

		else
			return null;
	}
}
