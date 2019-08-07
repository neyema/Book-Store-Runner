package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.BookStoreRunner;
import bgu.spl.mics.application.messages.CheckAvilabilityAndTakeEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;

import java.util.concurrent.CountDownLatch;

import static bgu.spl.mics.application.passiveObjects.OrderResult.SUCCESSFULLY_TAKEN;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{
	private Inventory inventory;
	private CountDownLatch countDownLatch;

	public InventoryService(int id, CountDownLatch cdl) {
		super("Inventory-Service"+id);
		inventory = Inventory.getInstance();
		countDownLatch=cdl;
	}

	@Override
	protected void initialize() {
		subscribeEvent(CheckAvilabilityAndTakeEvent.class, check -> {
			int price = inventory.checkAvailabiltyAndGetPrice(check.getBookTitle());
			if (price != -1 & check.getCustomerBalance() >= price) {
				if(inventory.take(check.getBookTitle()) == SUCCESSFULLY_TAKEN)
					complete(check, price);
			} else
				complete(check, -1);
		});
		subscribeBroadcast(TerminateBroadcast.class, broadcast -> {
			terminate();
		});
		BookStoreRunner.countDown(countDownLatch);
	}
}