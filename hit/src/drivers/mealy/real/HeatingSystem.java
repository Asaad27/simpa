package drivers.mealy.real;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
	private static String EXEC_PATH = "../../cheminée/arduino/simu/simulator";
	Runtime RT = Runtime.getRuntime();
	Process process = null;
	private OutputStream processInput;
	private InputStream processOutput;

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
			byte[] outputBytes = new byte[1024];
			try {
				processOutput.read(outputBytes);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			output = output + new String(outputBytes);
			if (output.contains("\n")) {
				assert output.split("\n").length == 1;
				output = output.split("\n")[0];
				EOLseen = true;
			}
		}
		byte b[] = new byte[4096];
		try {
			while (process.getErrorStream().read(b) > 0) {
				// System.out.println(input+new String(b));
			}
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
