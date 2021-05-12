package drivers.mealy.simulation.scanette;

import java.util.HashMap;
import java.util.Map;

/**
 * Facade to interact with the Scanette and Caisse. All actions performed on Scanette or Caisse are encapsulated,
 * such that a FSM behaviour is ensured. In particular, this class maintains an upper limit for items of a kind to be
 * put to the caddie.
 */
public class FSMSupermarket {
    public static final String PRODUITS_SCANETTE_CSV = "resources/scanette/produitsScanette.csv";
    public static final String PRODUITS_CAISSE_CSV = "resources/scanette/produitsCaisse.csv";
    public static final long ITEM_A_EAN = 5410188006711L;
    public static final long ITEM_B_EAN = 3560070048786L;
    public static final long ITEM_INCONNUE_EAN = 3570590109324L; //knwon to caisse, but not scanette
    public static final int MAX_NR_OF_ITEMS_IN_CADDIE = 1;
    private Scanette scanette;
    private Caisse caisse;
    private Map<Long, Integer> caddie;
    private State scanetteState = State.SHOPPING;

    public FSMSupermarket() {
        caddie = new HashMap<>();
        try {
            scanette = new Scanette(PRODUITS_SCANETTE_CSV);
            caisse = new FSMCaisse(PRODUITS_CAISSE_CSV);
        } catch (Scanette.ProductDBFailureException e) {
            e.printStackTrace();
        }
    }

    public int scanetteScan(long ean) {
        if (scanetteState == State.SHOPPING) {
            int count = caddie.getOrDefault(ean, 0);
            if (count < MAX_NR_OF_ITEMS_IN_CADDIE) {
                caddie.put(ean, count + 1);
                return scanette.scanner(ean);
            } else {
                return -2;
            }
        } else { //checkout ("relecutre")
            return scanette.scanner(ean);
        }
    }

    public int scanetteSupprimer(long ean) {
        if (scanetteState == State.SHOPPING) {
            int count = caddie.getOrDefault(ean, 0);
            if (count > 0) {
                caddie.put(ean, count - 1);
            }
        }
        return scanette.supprimer(ean);
    }

    public int caisseScan(long ean) {
        int count = caddie.getOrDefault(ean, 0);
        if (count > 0) {
            caddie.put(ean, count - 1);
            return caisse.scanner(ean);
        } else {
            return -2;
        }
    }

    public int caisseSupprimer(long ean) {
        int returnCode = caisse.supprimer(ean);
        if (returnCode == 0) { //item successfully removed
            int count = caddie.getOrDefault(ean, 0);
            caddie.put(ean, count + 1);
        }
        return returnCode;
    }

    public int scanetteDebloquer() {
        int returnCode = scanette.debloquer();
        if (returnCode == 0) { //debloquer successful
            scanetteState = State.SHOPPING;
        }
        return returnCode;
    }

    public int scanetteAbandon() {
        scanette.abandon();
        return 0;
    }

    public int scanetteTransmission() {
        scanetteState = State.CHECKOUT;
        return scanette.transmission(caisse);
    }

    public int caissePayer(double amount) {
        double change = caisse.payer(amount);
        if (change == -42.0) return -42;
        return (int) Math.signum(change);
    }

    public int caisseAbandon() {
        caisse.abandon();
        return 0;
    }

    public int caisseOuvrir() {
        return caisse.ouvrirSession();
    }

    public int caisseFermer() {
        return caisse.fermerSession();
    }

    private enum State {SHOPPING, CHECKOUT};
}
