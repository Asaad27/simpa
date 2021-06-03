package drivers.mealy.simulation;

import drivers.mealy.MealyDriver;
import drivers.mealy.simulation.scanette.FSMSupermarket;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static drivers.mealy.simulation.scanette.FSMSupermarket.*;
import static java.util.Map.entry;

public class ScanetteDriver extends MealyDriver {

    private FSMSupermarket supermarket;
    private final Map<String, Supplier<Integer>> actions;

    public ScanetteDriver(String name) {
        super(name);
        supermarket = new FSMSupermarket();
        actions = Map.ofEntries(
                entry("scanette_debloquer", () -> supermarket.scanetteDebloquer()),
                entry("scanette_abandon", () -> supermarket.scanetteAbandon()),
                entry("scanette_scanner_item_A", () -> supermarket.scanetteScan(ITEM_A_EAN)),
                entry("scanette_scan_unknown_item", () -> supermarket.scanetteScan(ITEM_INCONNUE_EAN)),
                entry("scanette_supprimer_item_A", () -> supermarket.scanetteSupprimer(ITEM_A_EAN)),
                entry("scanette_supprimer_unknown_item", () -> supermarket.scanetteSupprimer(ITEM_INCONNUE_EAN)),
                entry("scannette_transmisson", () -> supermarket.scanetteTransmission()),
                entry("scannette_transmisson_relecture", () -> supermarket.scanetteTransmissionRelecture()),
                entry("caisse_payer_zero", () -> supermarket.caissePayer(0.0)),
                entry("caisse_payer_all", () -> supermarket.caissePayEnough()),
                entry("caisse_abandon", () -> supermarket.caisseAbandon()),
                entry("caisse_ouvrir", () -> supermarket.caisseOuvrir()),
                entry("caisse_fermer", () -> supermarket.caisseFermer()),
                entry("caisse_scanner_item_A", () -> supermarket.caisseScan(ITEM_A_EAN)),
                entry("caisse_scanner_unknown_item", () -> supermarket.caisseScan(ITEM_INCONNUE_EAN))
                );
    }

    private String abstractOf(Integer i) {
        return String.valueOf(i);
    }

    @Override
    protected String execute_implem(String input) {
        if (!actions.containsKey(input)) {
            throw new IllegalArgumentException("Learner tried to applied non-existing input " + input);
        }
        return String.valueOf(actions.get(input).get());
    }

    @Override
    protected void reset_implem() {
        supermarket = new FSMSupermarket();
    }

    @Override
    public List<String> getInputSymbols() {
        return actions.keySet().stream().sorted().collect(Collectors.toList());
    }

}
