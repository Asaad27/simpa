package options.driverOptions;

import options.IntegerOption;

public class TimeoutOption extends IntegerOption {
	public TimeoutOption(int default_ms) {
		super("--D_timeout", "timeout to decide quiescence output (ms)",
				"When an input is sent to the System Under Learning, we do not know if we will get an output or not."
						+ " After this timeout (in ms), we consider that we will not have an answer.",
				default_ms);
	}

	/**
	 * To be compliant with future change of format, use {@link #getValue_ms() }
	 * instead.
	 * 
	 * @deprecated because it does not specified the unit of timeout. Use
	 *             {@link #getValue_ms()} instead.
	 */
	@Override
	@Deprecated
	public Integer getValue() {
		return super.getValue();
	}

	/**
	 * Get the timeout selected in milliseconds.
	 * 
	 * @return the timeout selected as milliseconds
	 */
	public int getValue_ms() {
		return super.getValue();
	}
}
