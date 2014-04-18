package crawler;

public class WebTransition {
	private int from;
	private int to;
	private WebInput by;

	public WebTransition(int from, int to, WebInput by) {
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

	public WebInput getBy() {
		return by;
	}
	
	public void setBy(WebInput i) {
		this.by = i;
	}

	public String toString() {
		return from + " to " + to + " by input_" + by;
	}
}
