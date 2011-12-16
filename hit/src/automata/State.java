package automata;

import java.io.Serializable;

public class State implements Serializable{
	private static final long serialVersionUID = 3191363945864393433L;

	private String name;
	private Boolean initial;
	private Boolean flag;
	
	public State(String name, Boolean initial){
		this.name = name;
		this.initial = initial;
		this.flag = false;
	}
	
	public String getId(){
		return name.substring(1);
	}
	
	public void cleanMark(){
		flag = false;
	}
	
	public void mark(){
		flag = true;
	}
	
	public boolean isMarked(){
		return flag;
	}
	
	public String getName() {
		return name;
	}

	public Boolean isInitial() {
		return initial;
	}
	
	public boolean equals(Object comp){
		if (this == comp) return true;
		if (!(comp instanceof State)) return false;
		State to = (State)comp;
		return ((name.equals(to.name)) && (initial.equals(to.initial)));
	}
	
	public int hashCode(){
		return name.hashCode()*7 + initial.hashCode()*31;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
