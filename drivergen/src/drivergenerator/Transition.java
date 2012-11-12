package drivergenerator;

public class Transition {
	private int from;
	private int to;
	private String by;
	public Transition(int from, int to, String by) {
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
	public String getBy() {
		return by;
	}
	public String toString(){
		return from + " to " + to + " by " + by;
	}
	public String toDot() {
		return from  + " -> " + to + " [label=\""+by+"\"]";
	}
}
