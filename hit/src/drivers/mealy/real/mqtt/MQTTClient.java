package drivers.mealy.real.mqtt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * a class representing the options for building one {@link MQTTClient}.
 * 
 * @author Nicolas BREMOND
 *
 */
class ClientDescriptor {
	/**
	 * The id to use when connecting to the broker. Will be automatically filled
	 * if empty.
	 */
	String id = "";
	boolean connect = false;
	boolean disconnect = false;
	Boolean close = false;

	class Publish {
		String topic;
		String message;
		Boolean retain;
	}

	List<Publish> connectWithWill = new ArrayList<>();
	List<Publish> publish = new ArrayList<>();
	List<String> deleteRetain = new ArrayList<>();

	List<String> subscribe = new ArrayList<>();
	List<String> unsubscribe = new ArrayList<>();

}

/**
 * a class representing one MQTT client. this class will perform actions on the
 * broker
 * 
 * @author Nicolas BREMOND
 *
 */
public class MQTTClient {

	final MQTT driver;
	String prefix = "";
	String name = null;

	class Connect extends MQTTOperation {
		MqttConnectOptions connOpts;

		Boolean willRetain = null;

		public Connect() {
			connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
		}

		public void setWill(String topic, String message, boolean retained) {
			connOpts.setWill(topic, message.getBytes(), qos, retained);
			assert usedTopic == null && willRetain == null;
			usedTopic = topic;
			usedMessage = message;
			willRetain = retained;

		}

		@Override
		public String execute() {
			try {
				sampleClient.connect(connOpts);
				return "Connected";
			} catch (MqttException e) {
				return "error";
			}
		}

		@Override
		protected String createInput_intern(boolean showTopic,
				boolean showMessage) {
			String input = "connect";
			if (willRetain != null) {
				input = input + " with will";
				if (showTopic)
					input = input + " on(" + usedTopic + ")";
				if (willRetain)
					input = input + " retain";
			}
			return input;
		}
	}

	class Disconnect extends MQTTOperation {
		public Disconnect() {
		}

		@Override
		public String execute() {
			try {
				sampleClient.disconnect();
				return "disconnected";
			} catch (MqttException e) {
				return "error";
			}
		}

		@Override
		protected String createInput_intern(boolean showTopic,
				boolean showMessage) {
			return "disconnect";
		}

	}

	class Close extends MQTTOperation {
		public Close() {
		}

		@Override
		public String execute() {
			String closeStatus;
			try {
				sampleClient.disconnectForcibly(0, 0, false);
				sampleClient.close();
				closeStatus = "closed";
			} catch (MqttException e) {
				closeStatus = "error";
			}
			createClient();
			return closeStatus;
		}

		@Override
		protected String createInput_intern(boolean showTopic,
				boolean showMessage) {
			return "close";
		}
	}

	class Publish extends MQTTOperation {
		final boolean retained;

		Publish(String topic, String message, boolean retained) {
			this.usedTopic = topic;
			this.usedMessage = message;
			this.retained = retained;
		}

		@Override
		public String execute() {
			MqttMessage message = new MqttMessage(usedMessage.getBytes());
			message.setQos(qos);
			message.setRetained(retained);
			try {
				sampleClient.publish(usedTopic, message);
				return "published";
			} catch (MqttException e) {
				return "error";
			}
		}

		@Override
		protected String createInput_intern(boolean showTopic,
				boolean showMessage) {
			String input = "publish";
			if (retained)
				input = input + "Retained";
			if (showMessage || showTopic) {
				input = input + "(";
				if (showTopic) {
					input = input + usedTopic;
					if (showMessage)
						input = input + ", ";
				}
				if (showMessage)
					input = input + "'" + usedMessage + "'";
				input = input + ")";
			}
			return input;
		}
	}

	class DeleteRetained extends MQTTOperation {

		DeleteRetained(String topic) {
			usedTopic = topic;
		}

		@Override
		public String execute() {
			MqttMessage message = new MqttMessage(new byte[0]);
			message.setQos(qos);
			message.setRetained(true);
			try {
				sampleClient.publish(usedTopic, message);
				return "published";
			} catch (MqttException e) {
				return "error";
			}
		}

		@Override
		protected String createInput_intern(boolean showTopic,
				boolean showMessage) {
			if (showTopic)
				return "deleteRetained(" + usedTopic + ")";
			return "deleteRetained";
		}
	}

	class Subscribe extends MQTTOperation {
		public Subscribe(String topic) {
			usedTopic = topic;
		}

		@Override
		public String execute() {
			try {
				sampleClient.subscribe(usedTopic);
				return "ok";
			} catch (MqttException e) {
				return "error";
			}
		}

		@Override
		protected String createInput_intern(boolean showTopic,
				boolean showMessage) {
			String end = "";
			if (showTopic)
				end = "(" + usedTopic + ")";
			return "subscribe" + end;
		}
	}

	class Unsubscribe extends MQTTOperation {

		public Unsubscribe(String topic) {
			usedTopic = topic;
		}

		@Override
		public String execute() {
			try {
				sampleClient.unsubscribe(usedTopic);
				return "ok";
			} catch (MqttException e) {
				return "error";
			}
		}

		@Override
		protected String createInput_intern(boolean showTopic,
				boolean showMessage) {
			String end = "";
			if (showTopic)
				end = "(" + usedTopic + ")";
			return "unsubscribe" + end;
		}
	}

	int qos = 2;
	/**
	 * The id used to connect to the broker. If empty, a number will be
	 * generated.
	 */
	String clientId = "";
	MemoryPersistence persistence = new MemoryPersistence();

	MqttClient sampleClient;

	private List<MQTTOperation> operations = new ArrayList<>();

	public List<MQTTOperation> getOperations() {
		return Collections.unmodifiableList(operations);
	}

	public void addPublishOperation(String topic, String message,
			boolean retained) {
		operations.add(new Publish(topic, message, retained));
	}

	public void addDeleteRetained(String topic) {
		operations.add(new DeleteRetained(topic));
	}

	public void addSubscribeOperation(String topic) {
		operations.add(new Subscribe(topic));
	}

	public void addUnsubscribeOperation(String topic) {
		operations.add(new Unsubscribe(topic));
	}

	public void addConnect() {
		operations.add(new Connect());
	}

	public void addDisconnect() {
		operations.add(new Disconnect());
	}

	public void addClose() {
		operations.add(new Close());
	}

	public void addConnectWithWill(String topic, String message,
			boolean retained) {
		Connect c = new Connect();
		c.setWill(topic, message, retained);
		operations.add(c);
	}

	static private int clientIdNb = 0;

	public void createClient() {
		if (clientId.isEmpty())
			clientId = "client" + clientIdNb++;
		try {
			sampleClient = new MqttClient(driver.broker, clientId, persistence);
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
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

	public MQTTClient(MQTT driver) {
		addDisconnect();
		this.driver = driver;
	}

	public MQTTClient(MQTT driver, ClientDescriptor desc) {
		this.driver = driver;
		if (!desc.id.isEmpty()) {
			clientId = desc.id;
			name = desc.id;
		}
		if (desc.connect)
			addConnect();
		if (desc.disconnect)
			addDisconnect();
		if (desc.close)
			addClose();
		for (ClientDescriptor.Publish p : desc.connectWithWill)
			addConnectWithWill(p.topic, p.message, p.retain);
		for (ClientDescriptor.Publish p : desc.publish)
			addPublishOperation(p.topic, p.message, p.retain);
		for (String topic : desc.deleteRetain)
			addDeleteRetained(topic);
		for (String topic : desc.subscribe)
			addSubscribeOperation(topic);
		for (String topic : desc.unsubscribe)
			addUnsubscribeOperation(topic);
	}

	LinkedList<String> received = new LinkedList<>();

}
