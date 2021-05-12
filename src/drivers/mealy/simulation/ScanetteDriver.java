package drivers.mealy.simulation;

import drivers.mealy.MealyDriver;
import drivers.mealy.simulation.scanette.FSMSupermarket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static drivers.mealy.simulation.scanette.FSMSupermarket.*;
import static java.lang.Math.signum;
import static java.util.Map.entry;

public class ScanetteDriver extends MealyDriver {
    public static final int I = 42;

    private FSMSupermarket supermarket;
    private final Map<String, Supplier<Integer>> actions;


    public ScanetteDriver(String name) {
        super(name);
        supermarket = new FSMSupermarket();
        actions = Map.ofEntries(
                entry("scanette_scanner_item_A", () -> supermarket.scanetteScan(ITEM_A_EAN)),
             //   entry("scanette_scanner_item_B", () -> supermarket.scanetteScan(ITEM_B_EAN)),
                entry("scanette_scan_unknown_item", () -> supermarket.scanetteScan(ITEM_INCONNUE_EAN)),
                entry("scanette_debloquer", () -> supermarket.scanetteDebloquer()),
                entry("scanette_supprimer_item_A", () -> supermarket.scanetteSupprimer(ITEM_A_EAN)),
         //       entry("scanette_supprimer_item_B", () -> supermarket.scanetteSupprimer(ITEM_B_EAN)),
                entry("scanette_supprimer_unknown_item", () -> supermarket.scanetteSupprimer(ITEM_INCONNUE_EAN)),
                entry("scanette_abandon", () -> supermarket.scanetteAbandon()),
                entry("scannette_transmisson", () -> supermarket.scanetteTransmission()),
                entry("caisse_payer_zero", () -> supermarket.caissePayer(0.0)),
                entry("caisse_payer_1000", () -> supermarket.caissePayer(1000.0)),
                entry("caisse_abandon", () -> supermarket.caisseAbandon()),
                entry("caisse_ouvrir", () -> supermarket.caisseOuvrir()),
                entry("caisse_fermer", () -> supermarket.caisseFermer()),
                entry("caisse_scanner_item_A", () -> supermarket.caisseScan(ITEM_A_EAN)),
         //       entry("caisse_scanner_item_B", () -> supermarket.caisseScan(ITEM_B_EAN)),
                entry("caisse_scanner_unknown_item", () -> supermarket.caisseScan(ITEM_INCONNUE_EAN)),
                entry("caisse_supprimer_item_A", () -> supermarket.caisseSupprimer(ITEM_A_EAN)),
          //      entry("caisse_supprimer_item_B", () -> supermarket.caisseSupprimer(ITEM_B_EAN)),
                entry("caisse_supprimer_unknown_item", () -> supermarket.caisseSupprimer(ITEM_INCONNUE_EAN))
                );
    }

    private String abstractOf(Integer i) {
        return String.valueOf(i);
    }

    private int scanItem(long ean) {
        return 0;
    }

    @Override
    protected String execute_implem(String input) {
        if (!actions.containsKey(input)) {
            throw new IllegalArgumentException("Learner tried to applied non-existing input " + input);
        }
        var concreteOutput = actions.get(input).get();
        return abstractOf(concreteOutput);
    }

    @Override
    protected void reset_implem() {
        supermarket = new FSMSupermarket();
    }

    @Override
    public List<String> getInputSymbols() {
        return new ArrayList<>(actions.keySet());
    }
}
