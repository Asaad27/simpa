package learner.mealy.hW.dataManager;

import java.util.List;

public class NewInputsFoundException extends RuntimeException {

    private final List<String> delta;

    public NewInputsFoundException(List<String> delta) {
        this.delta = delta;
    }

    public List<String> getDelta() {
        return delta;
    }
}
