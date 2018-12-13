package drivers.mealy.real.mqtt;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import drivers.mealy.real.mqtt.ClientDescriptor.Publish;
import options.ListOption;
import options.automataOptions.DriverChoice;
import options.automataOptions.DriverChoiceItem;
import tools.Utils;

class ClientsOption extends ListOption<ClientDescriptor> {

	public ClientsOption() {
		super("--mqttclient");
	}

	static final String CONNECT = "connect";
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
		List<String> split = Utils.stringToList(args, ',', '\\');
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
	protected ClientDescriptor fromString(String s,
			PrintStream parsingErrorStream) {
		ClientDescriptor desc = new ClientDescriptor();
		List<String> elements = Utils.stringToList(s, '|', '\\');
		for (String e : elements) {
			if (e.equals(CONNECT))
				desc.connect = true;
			else if (e.equals(DISCONNECT))
				desc.disconnect = true;
			else if (e.equals(CLOSE))
				desc.close = true;
			else if (e.startsWith(PUBLISH)) {
				List<String> args = getArgs(e, PUBLISH, 3, parsingErrorStream);
				if (args == null)
					return null;
				ClientDescriptor.Publish p = desc.new Publish();
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
	protected String valueToString(ClientDescriptor desc) {
		List<String> elements = new ArrayList<>();
		if (desc.connect)
			elements.add(CONNECT);
		if (desc.disconnect)
			elements.add(DISCONNECT);
		if (desc.close)
			elements.add(CLOSE);
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
		return Utils.listToString(elements, '|', '\\');
	}

	@Override
	public String getHelp() {
		return "describe a client for MQTT";
	}

	@Override
	protected Component createComponentFromValue(ClientDescriptor desc) {
		JPanel pane = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		pane.setLayout(new GridBagLayout());

		JCheckBox connect = new JCheckBox("'connect' input");
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

		pane.add(new StringList(desc.subscribe, "add 'subscribe' for topic",
				SUBSCRIBE).build(), c);
		pane.add(new StringList(desc.unsubscribe, "add 'unsubscribe' for topic",
				UNSUBSCRIBE).build(), c);
		pane.add(new StringList(desc.deleteRetain,
				"add 'delete message retained' on topic", DELETE_RETAINED)
						.build(),
				c);
		pane.add(new ElementList<Publish>(desc.publish) {
			private static final long serialVersionUID = 1L;
			JTextField topic;
			JTextField message;
			JCheckBox retain;

			@Override
			List<Component> getFields() {
				topic = new JTextField("topic", 10);
				message = new JTextField("message", 20);
				retain = new JCheckBox("retain");
				return Arrays.asList(new JLabel("create 'publish' on topic"),
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
				return buildFunc(PUBLISH, el.topic, el.message,
						el.retain ? "retained" : "not retained");
			}

		}.build(), c);

//		
//		abstract class RemoveButton extends JButton {
//			public RemoveButton(String value, JPanel parent, String action) {
//				super("Remove " + buildFunc(action, value));
//				addActionListener(new ActionListener() {
//
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						remove(value);
//						parent.remove(RemoveButton.this);
//						parent.revalidate();
//					}
//				});
//				parent.add(this);
//				parent.revalidate();
//			}
//
//			abstract void remove(String value);
//		}
//		{
//			class SubscribeRemoveButton extends RemoveButton {
//
//				public SubscribeRemoveButton(String value, JPanel parent) {
//					super(value, parent, "subscribe");
//				}
//
//				@Override
//				void remove(String value) {
//					desc.subscribe.remove(value);
//				}
//
//			}
//			JPanel subscribePane = new JPanel();
//			subscribePane.add(new JLabel("add subscribe on topic"));
//			JTextField topicField = new JTextField("topic");
//			subscribePane.add(topicField);
//			JButton button = new JButton("add");
//			button.addActionListener(new ActionListener() {
//
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					desc.subscribe.add(topicField.getText());
//					new SubscribeRemoveButton(topicField.getText(),
//							subscribePane);
//				}
//			});
//			subscribePane.add(button);
//			for (String topic : desc.subscribe)
//				new SubscribeRemoveButton(topic, subscribePane);
//			pane.add(subscribePane, c);
//		}

		return pane;
	}

	@Override
	protected ClientDescriptor createNewValue() {
		ClientDescriptor desc = new ClientDescriptor();
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

	public MQTTDriverOption(DriverChoice<?> parent) {
		super("MQTT Driver", "mqttDriver", parent, MQTT.class);
		clients = new ClientsOption();
		subTrees.add(clients);
	}

	@Override
	public MQTT createDriver() {
		return new MQTT(clients.getValues());
	}

}
