package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.BookStoreRunner;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourcesHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService {
	private ResourcesHolder resourcesHolder;
	private CountDownLatch countDownLatch;
	private BlockingQueue<Future<DeliveryVehicle>> deliveryVehicleFutureQueue;

	public ResourceService(int id, CountDownLatch cdl) {
		super("Resource-Service" + id);
		resourcesHolder = ResourcesHolder.getInstance();
		countDownLatch = cdl;
		deliveryVehicleFutureQueue = new LinkedBlockingQueue<>();
	}

	@Override
	protected void initialize() {
		subscribeEvent(AcquireVehicleEvent.class, message -> {
			Future<DeliveryVehicle> deliveryVehicleFuture = resourcesHolder.acquireVehicle(); //
			deliveryVehicleFutureQueue.add(deliveryVehicleFuture);
			complete(message, deliveryVehicleFuture);
		});
		subscribeEvent(ReleaseVehicleEvent.class, message -> {
			resourcesHolder.releaseVehicle(message.getVehicle());
		});
		subscribeBroadcast(TerminateBroadcast.class, broadcast -> {
			for(Future<DeliveryVehicle> toResolve : deliveryVehicleFutureQueue) {
				if(!toResolve.isDone())
					toResolve.resolve(null);
			}
			terminate();

		});
		BookStoreRunner.countDown(countDownLatch);
	}
}