package learner.mealy.hW.dataManager;

public class LocalizedHZXWSequence {
	public final HZXWSequence sequence;
	public int transferPosition = 0;
	public FullyQualifiedState endOfTransferState = null;

	public LocalizedHZXWSequence(HZXWSequence sequence) {
		this.sequence = sequence;
	}
}
