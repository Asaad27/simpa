package crawler;

public class Transition {
	private int from;
	private int to;
	private Input by;

	public Transition(int from, int to, Input by) {
		super();
		this.from = from;
		this.to = to;
		this.by = by;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public Input getBy() {
		return by;
	}
	
	public void setBy(Input i) {
		this.by = i;
	}

	public String toString() {
		return from + " to " + to + " by input_" + by;
	}
}
