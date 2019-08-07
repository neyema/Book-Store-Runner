package bgu.spl.mics.application.passiveObjects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static bgu.spl.mics.application.passiveObjects.OrderResult.*;
import static org.junit.Assert.*;

public class InventoryTest {

    private Inventory inventory = Inventory.getInstance();
    private BookInventoryInfo[] toBeLoaded = new BookInventoryInfo[]{new BookInventoryInfo("A", 1, 2)};

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getInstance() {
        assertNotNull(inventory);
        assertEquals(inventory,Inventory.getInstance());
    }

    @Test
    public void load() throws Exception {
        inventory.load(toBeLoaded);
        assertEquals(2, inventory.checkAvailabiltyAndGetPrice("A"));
        inventory.take("A");
    }

    @Test
    public void take() throws Exception {
        inventory.load(toBeLoaded);
        assertEquals(SUCCESSFULLY_TAKEN, inventory.take("A"));
        assertEquals(NOT_IN_STOCK, inventory.take("A"));
    }

    @Test
    public void checkAvailabiltyAndGetPrice() {
        inventory.load(toBeLoaded);
        assertEquals(2, inventory.checkAvailabiltyAndGetPrice("A"));
        inventory.take("A");
        assertEquals(-1, inventory.checkAvailabiltyAndGetPrice("A"));
    }

    @Test
    public void printInventoryToFile() {
    }
}