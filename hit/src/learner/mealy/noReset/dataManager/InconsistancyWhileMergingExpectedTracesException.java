package learner.mealy.noReset.dataManager;

public class InconsistancyWhileMergingExpectedTracesException extends
		RuntimeException {

	private static final long serialVersionUID = 5548780977037123061L;
	private TraceTree mergingTraces;
	private TraceTree existingExpectedTraces;
	private FullyKnownTrace existingTransition;

	public InconsistancyWhileMergingExpectedTracesException(
			TraceTree mergingTraces, FullyKnownTrace knownTrace) {
		super();
		this.existingTransition = knownTrace;
		this.mergingTraces = mergingTraces;
		this.existingExpectedTraces = null;
	}

	public InconsistancyWhileMergingExpectedTracesException(
			TraceTree mergingTraces, TraceTree existingExpectedTraces) {
		super();
		this.existingTransition = null;
		this.mergingTraces = mergingTraces;
		this.existingExpectedTraces = existingExpectedTraces;
	}

	public String toString() {
		if (existingTransition != null)
			return "inconsistency between merged expected traces and known transition. We already knew transition '"
					+ existingTransition
					+ "' and we tried to add expected traces '"
					+ mergingTraces
					+ "'";
		else
			return "inconsistency while merging two groups of expected traces. We previously had '"
					+ existingExpectedTraces
					+ "' and we tried to add expected traces '"
					+ mergingTraces
					+ "'";
	}

}
