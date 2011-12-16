package automata.efsm;

import java.util.List;

public interface IOutputFunction {
	List<Parameter> process(EFSM automata, List<Parameter> inputParameters);
}
