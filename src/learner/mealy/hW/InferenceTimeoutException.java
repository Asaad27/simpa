package learner.mealy.hW;

public class InferenceTimeoutException extends RuntimeException {

    public InferenceTimeoutException() {
    }

    public InferenceTimeoutException(String message) {
        super(message);
    }
}
