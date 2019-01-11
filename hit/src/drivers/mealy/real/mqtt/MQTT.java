package drivers.mealy.real.mqtt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import drivers.mealy.real.RealDriver;
import tools.loggers.LogManager;

public class MQTT extends RealDriver {

	/**
	 * the broker to infer
	 */
	String broker = "tcp://localhost:1883";
	/**
	 * the timeout during which messages are waited from broker.
	 */
	int timeout_ms = 500;
	/**
	 * the topics which should be cleared for a fake reset.
	 */
	List<String> topics = null;

	public MQTT() {
		super("MQTT");

		String topic = "top";
		MQTTClient c1 = new MQTTClient(this);
		MQTTClient c2 = new MQTTClient(this);
		addClient(c1);
		addClient(c2);

		// // "non_clean"
		// {
		// c1.addConnect();
		// c2.addConnect();
		// c1.addPublishOperation(topic, "message from C1", false);
		// c2.addSubscribeOperation(topic);
		// c2.addUnsubscribeOperation(topic);
		// // disconnect
		// }

		// // "two_client_will_retain"
		// {
		// c1.addConnectWithWill(topic, "will", true);
		// c1.addConnectWithWill(topic, "will", false);
		// c2.addConnect();
		// c2.addSubscribeOperation(topic);
		// c2.addUnsubscribeOperation(topic);
		// c1.addDeleteRetained(topic);
		// c2.addDeleteRetained(topic);
		// c1.addClose();
		// }

		MQTTClient c3 = new MQTTClient(this);
		addClient(c3);
		c1.name = "C1";
		c2.name = "C2";
		c3.name = "C3";
		c1.addConnect();
		c2.addConnect();
		c1.addPublishOperation(topic, "simple", false);
		c1.addPublishOperation(topic, "retained", true);
		c2.addSubscribeOperation(topic);
		c2.addUnsubscribeOperation(topic);
		// c1.addSubscribeOperation(topic);
		c3.addDeleteRetained(topic);
		c3.addConnect();
	}

	public MQTT(String broker, List<MQTTClientDescriptor> clientsDescriptors) {
		super("MQTT");
		this.broker = broker;
		for (MQTTClientDescriptor desc : clientsDescriptors) {
			addClient(new MQTTClient(this, desc));
		}
	}

	void setTimeout_ms(int t) {
		timeout_ms = t;
	}

	public void addClient(MQTTClient c) {
		clients.add(c);
	}

	ArrayList<MQTTClient> clients = new ArrayList<>();

	Map<String, MQTTOperation> operations;

	private void updateOperations() {
		assert operations == null;

		HashMap<String, HashMap<String, List<MQTTOperation>>> sortedOperation = new HashMap<>();
		final String NO_MESSAGE = "";
		// sort operations
		for (int i = 0; i < clients.size(); i++) {
			MQTTClient client = clients.get(i);
			for (MQTTOperation operation : client.getOperations()) {
				if (operation.usedTopic == null) {
					assert operation.usedMessage == null;
				} else {
					HashMap<String, List<MQTTOperation>> topicOperations = sortedOperation
							.get(operation.usedTopic);
					if (topicOperations == null) {
						topicOperations = new HashMap<>();
						sortedOperation.put(operation.usedTopic,
								topicOperations);
					}
					String message = operation.usedMessage;
					if (message == null)
						message = NO_MESSAGE;
					List<MQTTOperation> operations = topicOperations
							.get(message);
					if (operations == null) {
						operations = new ArrayList<>();
						topicOperations.put(message, operations);
					}
					operations.add(operation);
				}
			}
		}
		// compute local operations
		boolean showTopics = (sortedOperation.size() > 1);
		for (int i = 0; i < clients.size(); i++) {
			MQTTClient client = clients.get(i);
			for (MQTTOperation operation : client.getOperations()) {
				if (operation.usedTopic == null) {
					operation.createInput(false, false);
				} else {
					HashMap<String, List<MQTTOperation>> topicOperations = sortedOperation
							.get(operation.usedTopic);
					int messages = topicOperations.size();
					if (topicOperations.containsKey(NO_MESSAGE))
						messages--;
					boolean showMessage = messages > 1;
					operation.createInput(showTopics, showMessage);
				}
			}
		}

		// create the list of duplicates client names
		List<String> clientsNames = new ArrayList<>(clients.size());
		for (MQTTClient client : clients) {
			if (client.name != null)
				clientsNames.add(client.name);
		}
		for (String name : new HashSet<String>(clientsNames)) {
			if (clientsNames.indexOf(name) == clientsNames.lastIndexOf(name)) {
				clientsNames.remove(name);
			}
		}

		// create input of high level
		operations = new HashMap<>();
		for (int i = 0; i < clients.size(); i++) {
			MQTTClient client = clients.get(i);
			if (clients.size() > 1) {
				client.prefix = "C" + i + "::";
			}
			if (client.name != null) {
				client.prefix = client.name + "::";
				if (clientsNames.contains(client.name)) {
					List<String> names = new ArrayList<>(clientsNames);
					names.retainAll(Arrays.asList(client.name));
					client.prefix = client.name + '(' + names.size() + ")::";
					clientsNames.remove(client.name);
				}
			}
			client.createClient();
			for (MQTTOperation operation : client.getOperations()) {
				String input = client.prefix + operation.getInput();
				assert !operations.containsKey(input);
				operations.put(input, operation);
			}
		}
		topics = new ArrayList<>(sortedOperation.keySet());
		fakeReset();
	}

	@Override
	public void stopLog() {
	}

	@Override
	public List<String> getInputSymbols() {
		if (operations == null)
			updateOperations();
		return new ArrayList<>(operations.keySet());
	}

	@Override
	public String execute(String input) {

		numberOfAtomicRequest++;
		String output = execute_intern(input);
		sleepTimeout();
		for (MQTTClient client : clients) {
			while (!client.received.isEmpty()) {
				output = output + ", " + client.prefix + "received("
						+ client.received.poll() + ")";
			}
		}
		LogManager.logRequest(input, output, numberOfAtomicRequest);
		System.out.println(numberOfAtomicRequest + ":" + input + "/" + output);
		return output;
	}

	public String execute_intern(String input) {
		assert operations.containsKey(input);
		return operations.get(input).execute();
	}

	protected void sleepTimeout() {
		try {
			Thread.sleep(timeout_ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reset() {
		super.reset();
		fakeReset();
	}

	/**
	 * Replace the driver in an initial state without restarting it. This method
	 * disconnect clients and remove retained messages on known topics on the
	 * driver.
	 * 
	 * Notice that when connecting the first time to a driver, it may already
	 * have some messages retained. To have an initial state coherent, this
	 * method should be called before starting inference.
	 */
	protected void fakeReset() {
		for (MQTTClient client : clients)
			client.new Close().execute();
		sleepTimeout();
		// some clients might have received a last will message, we have to
		// clear it
		for (MQTTClient client : clients)
			client.received.clear();
		deleteRetained();
	}

	/**
	 * Delete retained messages for the {@link #topics recorded topics}.
	 * 
	 * Suppose that other clients are not sending messages.
	 */
	private void deleteRetained() {
		if (topics == null)
			updateOperations();
		try {
			ArrayList<String> topicsWithRet = new ArrayList<>();
			MqttClient client = new MqttClient(broker, "retainedRemover",
					new MemoryPersistence());
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			client.connect(connOpts);
			client.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message)
						throws Exception {
					assert message
							.isRetained() : "other clients are not supposed to send messages";
					topicsWithRet.add(topic);
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
				}

				@Override
				public void connectionLost(Throwable cause) {
				}
			});
			sleepTimeout();// let non-retained messages get lost before
							// subscribing

			// subscribe on topics to check if we receive a retained message
			for (String topic : topics) {
				client.subscribe(topic);
			}
			sleepTimeout();

			// delete retained on topics which have one
			MqttMessage message = new MqttMessage(new byte[0]);
			message.setRetained(true);
			for (String topic : topicsWithRet) {
				client.publish(topic, message);
			}
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
