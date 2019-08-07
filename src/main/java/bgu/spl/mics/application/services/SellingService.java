package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.BookStoreRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.concurrent.CountDownLatch;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{
	private MoneyRegister moneyRegister;
	private int tick = 0;
	private CountDownLatch countDownLatch;

	public SellingService(int id, CountDownLatch cdl) {
		super("Selling-Service"+id);
		moneyRegister = MoneyRegister.getInstance();
		countDownLatch = cdl;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, tick -> {
			this.tick = tick.getCurrentTick();
		});
		subscribeEvent(BookOrderEvent.class, message -> {
			OrderReceipt output = null;
			boolean purchaseSucceed = false;
			synchronized (message.getCustomer()) {
				Future<Integer> priceFuture = sendEvent(new CheckAvilabilityAndTakeEvent(message.getBookTitle(), message.getCustomer().getAvailableCreditAmount()));
				if (priceFuture != null) {
					Integer price = priceFuture.get();
					if (price != null && price != -1) {
						moneyRegister.chargeCreditCard(message.getCustomer(), price);
						output = new OrderReceipt(0, this.getName(), message.getCustomer().getId(), message.getBookTitle(), price, tick, message.getTick(), tick);
						moneyRegister.file(output);
						message.getCustomer().getCustomerReceiptList().add(output);
						purchaseSucceed = true;
					}
				}
			}
			complete(message, output);
			if(purchaseSucceed) {
				sendEvent(new DeliveryEvent(message.getCustomer().getAddress(), message.getCustomer().getDistance()));
			}
		});
		subscribeBroadcast(TerminateBroadcast.class, broadcast -> {
			terminate();
		});
		BookStoreRunner.countDown(countDownLatch);
	}
}