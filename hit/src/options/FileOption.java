package options;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import options.OptionTree.ArgumentDescriptor.AcceptedValues;

public class FileOption extends OptionTree {
	public enum FileSelectionMode {
		FILES_ONLY(JFileChooser.FILES_ONLY),
		DIRECTORIES_ONLY(JFileChooser.DIRECTORIES_ONLY);
		// Note : there is also the possibility to allow any kind but it should
		// probably not be used for options.
		// FILES_AND_DIRECTORIES(JFileChooser.FILES_AND_DIRECTORIES);

		public final int fileChooserMode;

		private FileSelectionMode(int fileChooserMode) {
			this.fileChooserMode = fileChooserMode;
		}
	}

	public enum FileExistance {
		MUST_EXIST, SHOULD_NOT_EXIST, WILL_OVERWITE, NO_CHECK;
	}

	class FileExistValidator extends OptionValidator {
		FileOption parent;

		public FileExistValidator(FileOption parent) {
			super(parent);
			this.parent = parent;
		}

		@Override
		public void check() {
			File path = parent.getcompletePath();
			if (parent.fileExistance == FileExistance.MUST_EXIST
					&& !path.exists()) {
				setCriticality(CriticalityLevel.ERROR);
				setMessage("file not found");
			} else if (parent.fileExistance == FileExistance.SHOULD_NOT_EXIST
					&& path.exists()) {
				setCriticality(CriticalityLevel.ERROR);
				setMessage("file already exists");
			} else if (parent.fileExistance == FileExistance.WILL_OVERWITE
					&& path.exists()) {
				setCriticality(CriticalityLevel.WARNING);
				setMessage("file will be overwritten");
			} else {
				setCriticality(CriticalityLevel.NOTHING);
				setMessage("");
			}
		}
	}

	class FileModeValidator extends OptionValidator {
		FileOption parent;

		public FileModeValidator(FileOption parent) {
			super(parent);
			this.parent = parent;
		}

		@Override
		public void check() {
			File path = parent.getcompletePath();
			if (path.isDirectory()
					&& parent.fileSelectionMode == FileSelectionMode.FILES_ONLY) {
				setCriticality(CriticalityLevel.ERROR);
				setMessage("only normal files are allowed");
			} else if (path.isFile()
					&& parent.fileSelectionMode == FileSelectionMode.DIRECTORIES_ONLY) {
				setCriticality(CriticalityLevel.ERROR);
				setMessage("only directories are allowed");
			} else {
				setCriticality(CriticalityLevel.NOTHING);
				setMessage("");
			}
		}
	}

	private JFileChooser fileChooser;
	private JTextField text;
	private JLabel suffixLabel;
	private File selectedFile;
	private String pathSufix = "";
	private final List<OptionTree> subOptions = new ArrayList<>();// Sub options
																	// are not
																	// supported
																	// by
																	// fileOption
																	// at this
																	// time.

	private final ArgumentDescriptor argument;

	private FileSelectionMode fileSelectionMode = FileSelectionMode.DIRECTORIES_ONLY;
	private FileExistance fileExistance = FileExistance.MUST_EXIST;

	public FileOption(String argument) {
		assert argument.startsWith("-");
		this.argument = new ArgumentDescriptor(AcceptedValues.ONE, argument,
				this);
		selectedFile = new File("./");

		addValidator(new FileExistValidator(this));
		addValidator(new FileModeValidator(this));
	}

	public FileOption(String argument, FileSelectionMode fileSelectionMode,
			FileExistance fileExistance) {
		this(argument);
		this.fileSelectionMode = fileSelectionMode;
		this.fileExistance = fileExistance;
	}

	public FileOption(String argument, String description, File defaultFile,
			FileSelectionMode fileSelectionMode, FileExistance fileExistance) {
		this(argument, fileSelectionMode, fileExistance);
		this.description = description;
		if (defaultFile != null)
			setSelectedFile(defaultFile);
	}

	public void setSuffix(String suffix) {
		pathSufix = suffix;
		if (mainConponent != null) {
			if (suffix.equals(""))
				suffixLabel.setText("");
			else if (suffix.startsWith(File.separator))
				suffixLabel.setText(suffix);
			else
				suffixLabel.setText(File.separator + suffix);
		}
	}

	public File getcompletePath() {
		assert text == null || new File(text.getText()).equals(selectedFile);
		return new File(selectedFile.getPath() + File.separator + pathSufix);
	}

	@Override
	protected void createMainComponent() {
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		text = new JTextField();
		text.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				setSelectedFile(new File(text.getText()));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setSelectedFile(new File(text.getText()));
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				setSelectedFile(new File(text.getText()));
			}
		});
		panel.add(text);
		suffixLabel = new JLabel();
		panel.add(suffixLabel);
		panel.add(new Box.Filler(new Dimension(20, 0), new Dimension(50, 0),
				null));
		JButton button = new JButton("Browse");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(text);
				setSelectedFile(fileChooser.getSelectedFile());
			}
		});
		panel.add(button);

		mainConponent = panel;
		setSelectedFile(selectedFile);
		setSuffix(pathSufix);
	}

	private void setSelectedFile(File file) {
		selectedFile = file;
		if (mainConponent != null) {
			if (fileChooser.getSelectedFile() == null
					|| !fileChooser.getSelectedFile().equals(file)) {
				fileChooser.setSelectedFile(file);
			}
			if (!file.equals(new File(text.getText()))) {
				text.setText(file.getAbsolutePath());
			}
		}
		validateSelectedTree();
	}

	@Override
	protected List<OptionTree> getSelectedChildren() {
		return subOptions;
	}

	@Override
	protected boolean isActivatedByArg(ArgumentValue arg) {
		return arg.getName().equals(argument.name);
	}

	@Override
	protected boolean setValueFromArg(ArgumentValue arg,
			PrintStream parsingErrorStream) {
		assert isActivatedByArg(arg);
		if (arg.getValues().size() == 0)
			return false;
		setSelectedFile(new File(arg.getValues().get(0)));
		return true;
	}

	@Override
	protected void setValueFromSelectedChildren(
			List<OptionTree> selectedChildren) {
		assert selectedChildren == subOptions;
	}

	@Override
	protected ArgumentValue getSelectedArgument() {
		ArgumentValue argValue = new ArgumentValue(argument);
		argValue.addValue(selectedFile.getPath());
		return argValue;
	}

	@Override
	protected List<ArgumentDescriptor> getAcceptedArguments() {
		List<ArgumentDescriptor> args = new ArrayList<>();
		args.add(argument);
		return args;
	}

	@Override
	public String getHelpByArgument(ArgumentDescriptor arg) {
		return description;
	}

}
