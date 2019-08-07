package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.application.BookStoreRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static bgu.spl.mics.application.passiveObjects.OrderResult.*;

/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory implements Serializable {

	private static class InventorySingletonHolder { //thread-safe singleton impl
		private static Inventory instance = new Inventory();
	}

	private ConcurrentHashMap<String, BookInventoryInfo> inventoryConcurrentHashMap;
	private ConcurrentHashMap<String, Semaphore> semaphores;

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Inventory getInstance() {
		return InventorySingletonHolder.instance;
	}

	/**
	 * Initializes the store inventory. This method adds all the items given to the store
	 * inventory.
	 * <p>
	 * @param inventory 	Data structure containing all data necessary for initialization
	 * 						of the inventory.
	 */
	public void load (BookInventoryInfo[ ] inventory ) {
		inventoryConcurrentHashMap = new ConcurrentHashMap<>();
		semaphores = new ConcurrentHashMap<>();
		for(BookInventoryInfo book : inventory) {
			inventoryConcurrentHashMap.putIfAbsent(book.getBookTitle(), book);
			semaphores.putIfAbsent(book.getBookTitle(), new Semaphore(book.getAmount(), true));
		}
	}

	/**
	 * Attempts to take one book from the store.
	 * <p>
	 * @param book 		Name of the book to take from the store
	 * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
	 * 			The first should not change the state of the inventory while the
	 * 			second should reduce by one the number of books of the desired type.
	 */
	public OrderResult take (String book) {
		BookInventoryInfo toBeTaken = inventoryConcurrentHashMap.get(book);
		if(toBeTaken != null && semaphores.get(book).tryAcquire()) {
			toBeTaken.decrementAmountInInventory();
			return SUCCESSFULLY_TAKEN;
		}
		else
			return NOT_IN_STOCK;
	}



	/**
	 * Checks if a certain book is available in the inventory.
	 * <p>
	 * @param book 		Name of the book.
	 * @return the price of the book if it is available, -1 otherwise.
	 */
	public int checkAvailabiltyAndGetPrice(String book) {
		int price = -1;
		BookInventoryInfo toBeChecked = inventoryConcurrentHashMap.get(book);
		if(toBeChecked != null && toBeChecked.getAmount() > 0) {
			price = toBeChecked.getPrice();
		}
		return price;
	}

	/**
	 *
	 * <p>
	 * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
	 * should be the titles of the books while the values (type {@link Integer}) should be
	 * their respective available amount in the inventory.
	 * This method is called by the main method in order to generate the output.
	 */
	public void printInventoryToFile(String filename){
		HashMap<String, Integer> copiedInventory = new HashMap<>();
		for(String key : inventoryConcurrentHashMap.keySet()) {
			copiedInventory.put(key, inventoryConcurrentHashMap.get(key).getAmount());
		}
		BookStoreRunner.printToFile(copiedInventory, filename);
	}
}
