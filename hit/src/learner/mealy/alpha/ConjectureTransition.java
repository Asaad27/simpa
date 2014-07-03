package learner.mealy.alpha;




public class ConjectureTransition  {

	

	
	public StateLabel from;
	public StateLabel to;
	public String input;
	public String output;

	public ConjectureTransition(StateLabel s1, StateLabel s2, String input,
			String output) {
		from = s1;
		to = s2;
		this.input = input;
		this.output = output;
	}
	public StateLabel getFrom() {
		return from;
	}

	public void setFrom(StateLabel from) {
		this.from = from;
	}

	public StateLabel getTo() {
		return to;
	}

	public void setTo(StateLabel to) {
		this.to = to;
	}
	public String getOutput() {
		return output;
	}

	public void setOutput(String symbol) {
		output = symbol;
	}
	
	public boolean isLoop(){
		return from.equals(to);
	}

	@Override
	public String toString() {
		return from.name + "-" + input + "/" + output + "-"+ to.name;
	}

	public String toDot() {
		return from + " -> " + to + "[label=\"" + input + "/" + output + "\"];";
	}

	public String getName() {
		return from + "-" + to;
	}
	
	public int hashCode(){
		return (from + input).hashCode();
	}

}
