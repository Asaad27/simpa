package drivers.mealy.real.mqtt;

import java.util.ArrayList;
import java.util.List;

/**
 * a class representing the options for building one {@link MQTTClient}.
 * 
 * @author Nicolas BREMOND
 *
 */
class MQTTClientDescriptor {
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
