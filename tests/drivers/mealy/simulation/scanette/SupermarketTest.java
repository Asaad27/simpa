package drivers.mealy.simulation.scanette;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static drivers.mealy.simulation.scanette.FSMSupermarket.ITEM_A_EAN;
import static drivers.mealy.simulation.scanette.FSMSupermarket.ITEM_INCONNUE_EAN;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class SupermarketTest {
    private FSMSupermarket supermarket;

    @BeforeEach
    public void create() {
        supermarket = new FSMSupermarket();
    }

    @Test
    public void scanetteScanCaddieLimit() {
        assertEquals(FSMSupermarket.MAX_NR_OF_ITEMS_IN_CADDIE, 2);
        supermarket.scanetteDebloquer();
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(-2, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(-2, supermarket.scanetteScan(ITEM_A_EAN));
    }

    @Test
    public void scanetteScanAndRemoveCaddieLimit() {
        supermarket.scanetteDebloquer();
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(0, supermarket.scanetteSupprimer(ITEM_A_EAN));
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(-2, supermarket.scanetteScan(ITEM_A_EAN));
    }

    @Test
    public void relecture() {
        supermarket.scanetteDebloquer();
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(1, supermarket.scanetteTransmission());
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(0, supermarket.scanetteTransmission());
        assertEquals(1, supermarket.caissePayer(1000));
        assertEquals(0, supermarket.scanetteAbandon());
    }

    @Test
    public void caisseScanLimit() {
        //toogle relecture
        supermarket.scanetteDebloquer();
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(1, supermarket.scanetteTransmission());
        assertEquals(0, supermarket.scanetteScan(ITEM_A_EAN));
        assertEquals(0, supermarket.scanetteTransmission());
        assertEquals(1, supermarket.caissePayer(1000));
        assertEquals(0, supermarket.scanetteAbandon());

        supermarket.scanetteDebloquer();
        assertEquals(-2, supermarket.scanetteScan(ITEM_INCONNUE_EAN));
        assertEquals(0, supermarket.scanetteTransmission()); //no relecture
        assertEquals(0, supermarket.caisseOuvrir());
        assertEquals(0, supermarket.caisseScan(ITEM_INCONNUE_EAN));
        assertEquals(-2, supermarket.caisseScan(ITEM_INCONNUE_EAN));
        assertEquals(0, supermarket.caisseFermer());
    }
}



