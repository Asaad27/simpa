package drivers.mealy.real;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import automata.mealy.InputSequence;
import tools.Utils;
import tools.loggers.LogManager;

/**
 * This driver is an interface to an arduino-based heating manager. The original
 * manager is too complex but it can be simplified (reducing counters) to be
 * able to infer it.
 * 
 * @author Nicolas BREMOND
 *
 */
public class HeatingSystem extends RealDriver {
	public static class SUIDiedException extends RuntimeException {
		private static final long serialVersionUID = -8200274705215297825L;

		public SUIDiedException(int traceLength) {
			super("SUI died during inference after transition " + traceLength);
		}
	}

	private static String EXEC_PATH = "../../cheminée/arduino/simu/simulator";
	Runtime RT = Runtime.getRuntime();
	Process process = null;
	private OutputStream processInput;
	private InputStream processOutput;

	public boolean doRandomWalkOnKilled = true;

	public HeatingSystem() {
		super("heating system");

	}

	@Override
	public String execute(String input) {
		if (process == null)
			reset();
		numberOfAtomicRequest++;
		assert !input.contains("\n");
		try {
			processInput.write(input.getBytes());
			processInput.write("\n".getBytes());
			processInput.flush();

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		boolean EOLseen = false;
		String output = "";
		while (!EOLseen) {
			if (!process.isAlive()) {
				if (doRandomWalkOnKilled) {
					InputSequence randomSequence = InputSequence.generate(
							getInputSymbols(), 10 * numberOfAtomicRequest);
					HeatingSystem other = new HeatingSystem();
					other.doRandomWalkOnKilled = false;
					try {
						other.execute(randomSequence);
						LogManager.logConsole("Random walk of length "
								+ randomSequence.getLength()
								+ " did not kill SUI");
					} catch (SUIDiedException e) {
						LogManager.logConsole("Random walk killed SUI after "
								+ other.numberOfAtomicRequest + " inputs");
					}
				}
				throw new SUIDiedException(numberOfAtomicRequest);
			}
			byte[] outputBytes = new byte[1024];
			int numberRead;
			try {
				numberRead = processOutput.read(outputBytes);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			if (numberRead >= 0)
				output = output + new String(outputBytes, 0, numberRead);
			if (output.contains("\n")) {
				output = output.substring(0, output.lastIndexOf("\n"));
				assert !output.contains("\n");
				EOLseen = true;
			}
		}
		byte b[] = new byte[4096];
		try {
			process.getErrorStream().read(b);
			// System.out.println(input+new String(b));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (addtolog)
			LogManager.logRequest(input, output, numberOfAtomicRequest);
		return output;
	}

	@Override
	public void reset() {
		super.reset();
		if (process != null)
			process.destroy();
		try {
			process = RT.exec(EXEC_PATH);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		processOutput = process.getInputStream();
		processInput = process.getOutputStream();

	}

	@Override
	public List<String> getInputSymbols() {
		// the inputs symbols can be customized depending on the complexity
		// wanted for the SUI. the executable should give the complete list of
		// available command by sending input "help"
		return Utils.createArrayList("tickTime", "ambiant10", "ambiant15",
				"ambiant20", "water-tank50", "water-tank0", "depart_plancher0",
				"depart_plancher20", "depart_plancher35");
	}
}
