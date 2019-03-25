package drivers.mealy.real.mqtt;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import drivers.mealy.real.mqtt.MQTTClientDescriptor.Publish;
import options.driverOptions.TimeoutOption;
import options.TextOption;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;
import tools.Utils;
import tools.loggers.LogManager;

class ClientsOption extends ListOption<MQTTClientDescriptor> {

	public ClientsOption() {
		super("--DMQTT_client");
	}

	static final String CONNECT = "connect";
	static final String CONNECT_WITH_WILL = "connectWithWill";
	static final String DISCONNECT = "disconnect";
	static final String CLOSE = "closeTCP";
	static final String PUBLISH = "publish";
	static final String DELETE_RETAINED = "delRet";
	static final String SUBSCRIBE = "subscribe";
	static final String UNSUBSCRIBE = "unsubscribe";

	/**
	 * get arguments from a string representing a function (e.g.
	 * {@code func(arg1,arg2)})
	 * 
	 * @param element
	 *            the string to parse
	 * @param funcName
	 *            the name of function
	 * @param expectedSize
	 *            the expected number of argument
	 * @param errorStream
	 *            the stream to output error
	 * @return the arguments in a list or {@code null} if the string do not
	 *         match function name or do not have the good number of arguments
	 */
	static List<String> getArgs(String element, String funcName,
			int expectedSize, PrintStream errorStream) {
		if (!element.startsWith(funcName + "(") || !element.endsWith(")")) {
			errorStream.println("invalid syntax for '" + element
					+ "', should be '" + funcName + "(...)'");
			return null;
		}
		String args = element.substring(1 + funcName.length(),
				element.length() - 1);
		StringBuilder warnings = new StringBuilder();
		List<String> split = Utils.stringToList(args, ',', '\\', warnings);
		if (warnings.length() != 0) {
			errorStream.append(LogManager.prefixMultiLines("Warning : ",
					warnings.toString()));
		}
		if (split.size() != expectedSize) {
			errorStream.println("invalid syntax for '" + element + "', "
					+ expectedSize + " parameters are expected");
			return null;
		}
		return split;
	}

	static String buildFunc(String funcName, String... args) {
		return funcName + "("
				+ Utils.listToString(Arrays.asList(args), ',', '\\') + ")";
	}

	@Override
	protected MQTTClientDescriptor fromString(String s,
			PrintStream parsingErrorStream) {
		MQTTClientDescriptor desc = new MQTTClientDescriptor();
		StringBuilder warnings = new StringBuilder();
		List<String> split = Utils.stringToList(s, ':', '\\', warnings);
		if (warnings.length() != 0) {
			parsingErrorStream.append(LogManager.prefixMultiLines("Warning : ",
					warnings.toString()));
			warnings = new StringBuilder();
		}
		if (split.size() == 1) {
			s = split.get(0);
		} else if (split.size() == 2) {
			desc.id = split.get(0);
			s = split.get(1);
		} else {
			parsingErrorStream.println("error while parsing '" + s
					+ "', invalid syntax, too many ':'.");
			return null;
		}

		List<String> elements = Utils.stringToList(s, '|', '\\', warnings);
		if (warnings.length() != 0) {
			parsingErrorStream.append(LogManager.prefixMultiLines("Warning : ",
					warnings.toString()));
		}
		for (String e : elements) {
			if (e.equals(CONNECT))
				desc.connect = true;
			else if (e.equals(DISCONNECT))
				desc.disconnect = true;
			else if (e.equals(CLOSE))
				desc.close = true;
			else if (e.startsWith(CONNECT_WITH_WILL)) {
				List<String> args = getArgs(e, CONNECT_WITH_WILL, 3,
						parsingErrorStream);
				if (args == null)
					return null;
				MQTTClientDescriptor.Publish p = desc.new Publish();
				p.topic = args.get(0);
				p.message = args.get(1);
				p.retain = Boolean.parseBoolean(args.get(2));
				desc.connectWithWill.add(p);
			} else if (e.startsWith(PUBLISH)) {
				List<String> args = getArgs(e, PUBLISH, 3, parsingErrorStream);
				if (args == null)
					return null;
				MQTTClientDescriptor.Publish p = desc.new Publish();
				p.topic = args.get(0);
				p.message = args.get(1);
				p.retain = Boolean.parseBoolean(args.get(2));
				desc.publish.add(p);
			} else if (e.startsWith(DELETE_RETAINED)) {
				List<String> args = getArgs(e, DELETE_RETAINED, 1,
						parsingErrorStream);
				if (args == null)
					return null;
				desc.deleteRetain.add(args.get(0));
			} else if (e.startsWith(SUBSCRIBE)) {
				List<String> args = getArgs(e, SUBSCRIBE, 1,
						parsingErrorStream);
				if (args == null)
					return null;
				desc.subscribe.add(args.get(0));
			} else if (e.startsWith(UNSUBSCRIBE)) {
				List<String> args = getArgs(e, UNSUBSCRIBE, 1,
						parsingErrorStream);
				if (args == null)
					return null;
				desc.unsubscribe.add(args.get(0));
			} else {
				parsingErrorStream.println("cannot parse '" + e + "'.");
				return null;
			}
		}
		return desc;
	}

	@Override
	protected String valueToString(MQTTClientDescriptor desc) {
		List<String> elements = new ArrayList<>();
		if (desc.connect)
			elements.add(CONNECT);
		if (desc.disconnect)
			elements.add(DISCONNECT);
		if (desc.close)
			elements.add(CLOSE);
		for (Publish p : desc.connectWithWill) {
			String e = buildFunc(CONNECT_WITH_WILL, p.topic, p.message,
					p.retain.toString());
			elements.add(e);
		}
		for (Publish p : desc.publish) {
			String e = buildFunc(PUBLISH, p.topic, p.message,
					p.retain.toString());
			elements.add(e);
		}
		for (String topic : desc.deleteRetain)
			elements.add(buildFunc(DELETE_RETAINED, topic));
		for (String topic : desc.subscribe)
			elements.add(buildFunc(SUBSCRIBE, topic));
		for (String topic : desc.unsubscribe)
			elements.add(buildFunc(UNSUBSCRIBE, topic));
		String inputs = Utils.listToString(elements, '|', '\\');
		elements.clear();
		if (!desc.id.isEmpty())
			elements.add(desc.id);
		elements.add(inputs);
		return Utils.listToString(elements, ':', '\\');
	}

	@Override
	public String getHelp() {
		return "Describe a client for MQTT.";
	}

	@Override
	protected Component createComponentFromValue(MQTTClientDescriptor desc) {
		JPanel pane = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		pane.setLayout(new GridBagLayout());

		JTextField clientId = new JTextField(desc.id, 23);
		clientId.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				desc.id = clientId.getText();
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		clientId.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				desc.id = clientId.getText();
			}
		});

		pane.add(new JLabel("client id :"));
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		pane.add(clientId, c);
		c.gridwidth = 1;
		c.gridy = 1;

		JCheckBox connect = new JCheckBox("simple 'connect' input");
		connect.setSelected(desc.connect);
		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				desc.connect = connect.isSelected();
			}
		});
		pane.add(connect, c);

		JCheckBox disconnect = new JCheckBox("'disconnect' input");
		disconnect.setSelected(desc.disconnect);
		disconnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				desc.disconnect = disconnect.isSelected();
			}
		});
		pane.add(disconnect, c);

		JCheckBox close = new JCheckBox("'close TCP connection' input");
		close.setSelected(desc.close);
		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				desc.close = close.isSelected();
			}
		});
		pane.add(close, c);

		c.gridy = GridBagConstraints.RELATIVE;
		c.gridx = 0;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_START;

		abstract class ElementList<T> extends JPanel {
			private static final long serialVersionUID = 1L;

			abstract List<Component> getFields();

			abstract T createNewElement();

			abstract String getText(T el);

			final List<T> elements;

			public ElementList(List<T> elements) {
				this.elements = elements;
			}

			JPanel build() {
				for (Component field : getFields()) {
					add(field);
				}
				JButton button = new JButton("add");
				button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						T newEl = createNewElement();
						elements.add(newEl);
						new RemoveButton(newEl);
					}
				});
				add(button);
				for (T value : elements)
					new RemoveButton(value);
				return this;
			}

			class RemoveButton extends JButton {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public RemoveButton(T value) {
					super("Remove " + ElementList.this.getText(value));
					addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							ElementList.this.elements.remove(value);
							ElementList.this.remove(RemoveButton.this);
							ElementList.this.revalidate();
						}
					});
					ElementList.this.add(this);
					ElementList.this.revalidate();
				}
			}
		}

		class StringList extends ElementList<String> {
			private static final long serialVersionUID = 1L;
			String addFieldDescription;
			JTextField field;
			String action;
			String defaultText = "topic";

			public StringList(List<String> elements, String addFieldDescription,
					String action) {
				super(elements);
				this.addFieldDescription = addFieldDescription;
				this.action = action;
			}

			@Override
			List<Component> getFields() {
				field = new JTextField();
				field.setText(defaultText);
				return Arrays.asList(new JLabel(addFieldDescription), field);
			}

			@Override
			String createNewElement() {
				String el = field.getText();
				return el;
			}

			@Override
			String getText(String el) {
				return buildFunc(action, el);
			}

		}

		class PublishList extends ElementList<Publish> {
			private static final long serialVersionUID = 1L;
			JTextField topic;
			JTextField message;
			JCheckBox retain;
			String inputName;

			public PublishList(List<Publish> elements, String inputName) {
				super(elements);
				this.inputName = inputName;
			}

			@Override
			List<Component> getFields() {
				topic = new JTextField("topic", 10);
				message = new JTextField("message", 20);
				retain = new JCheckBox("retain");
				return Arrays.asList(
						new JLabel("create '" + inputName + "' on topic"),
						topic, new JLabel("with message"), message, retain);
			}

			@Override
			Publish createNewElement() {
				Publish p = desc.new Publish();
				p.topic = topic.getText();
				p.message = message.getText();
				p.retain = retain.isSelected();
				return p;
			}

			@Override
			String getText(Publish el) {
				return buildFunc(inputName, el.topic, el.message,
						el.retain ? "retained" : "not retained");
			}
		}

		pane.add(new PublishList(desc.connectWithWill, CONNECT_WITH_WILL)
				.build(), c);
		pane.add(new StringList(desc.subscribe, "add 'subscribe' for topic",
				SUBSCRIBE).build(), c);
		pane.add(new StringList(desc.unsubscribe, "add 'unsubscribe' for topic",
				UNSUBSCRIBE).build(), c);
		pane.add(new StringList(desc.deleteRetain,
				"add 'delete message retained' on topic", DELETE_RETAINED)
						.build(),
				c);
		pane.add(new PublishList(desc.publish, PUBLISH).build(), c);
		return pane;
	}

	@Override
	protected MQTTClientDescriptor createNewValue() {
		MQTTClientDescriptor desc = new MQTTClientDescriptor();
		desc.connect = true;
		desc.disconnect = true;
		return desc;
	}

	@Override
	protected String getOptionTitle() {
		return "MQTT clients";
	}

}

public class MQTTDriverOption extends DriverChoiceItem<MQTT> {

	ClientsOption clients;
	TextOption brokerAddress;
	TimeoutOption timeout;

	public MQTTDriverOption(DriverChoice<?> parent) {
		super("MQTT Driver", "mqttDriver", parent, MQTT.class);
		clients = new ClientsOption();
		brokerAddress = new TextOption("--DMQTT_broker", "tcp://localhost:1883",
				"broker address", "The adress of the broker to infer.");
		timeout = new TimeoutOption(500);
		subTrees.add(timeout);
		subTrees.add(brokerAddress);
		subTrees.add(clients);
	}

	@Override
	public MQTT createDriver() {
		MQTT driver = new MQTT(brokerAddress.getText(), clients.getValues());
		driver.setTimeout_ms(timeout.getValue_ms());
		return driver;
	}

}
