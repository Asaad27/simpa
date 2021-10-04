package drivers.mealy.gui;

import de.fzi.guitesting.UIDriver;
import drivers.mealy.PartialMealyDriver;

import java.util.List;

/**
 * Adapter from UIDriver (from the gui-testing-demo package) to PartialMealyDriver in SIMPA.
 */
public class UIDriverWrapper extends PartialMealyDriver {
    private final UIDriver delegate;

    public UIDriverWrapper(UIDriver delegate) {
        super("swingUIDriver");
        this.delegate = delegate;
    }

    @Override
    protected void reset_implem() {
        delegate.reset_implem();
    }

    @Override
    protected String execute_defined(String input) {
        return delegate.execute_defined(input);
    }

    @Override
    public List<String> getDefinedInputs() {
        return delegate.getDefinedInputs();
    }
}
