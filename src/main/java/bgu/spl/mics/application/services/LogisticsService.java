package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.BookStoreRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.concurrent.CountDownLatch;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
	CountDownLatch countDownLatch;

	public LogisticsService(int id, CountDownLatch cdl) {
		super("Logistics-Service" + id);
		countDownLatch = cdl;
	}

	@Override
	protected void initialize() {
		subscribeEvent(DeliveryEvent.class, message -> {
			DeliveryVehicle deliveryVehicle = null;
			Future<Future<DeliveryVehicle>> deliveryVehicleFutureFuture = sendEvent(new AcquireVehicleEvent());
			Future<DeliveryVehicle> deliveryVehicleFuture = null;
			if(deliveryVehicleFutureFuture != null) { //checks if the event was assigned to a micro service
				deliveryVehicleFuture = deliveryVehicleFutureFuture.get();
			}
			if(deliveryVehicleFuture != null) { //checks if this future was created
				deliveryVehicle = deliveryVehicleFuture.get();
				if(deliveryVehicle != null) {
					deliveryVehicle.deliver(message.getAddress(), message.getDistance()); //sleeps during delivery
					sendEvent(new ReleaseVehicleEvent(deliveryVehicle));
				}
			}
			complete(message, deliveryVehicle);
		});
		subscribeBroadcast(TerminateBroadcast.class, broadcast -> {
			terminate();
		});
		BookStoreRunner.countDown(countDownLatch);
	}
}