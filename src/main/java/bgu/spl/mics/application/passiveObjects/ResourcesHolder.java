package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {

	ConcurrentLinkedQueue<DeliveryVehicle> frees;
	ConcurrentLinkedQueue<Future<DeliveryVehicle>> requests;

	private static class ResourcesSingletonHolder { //thread-safe singleton impl
		private static ResourcesHolder instance = new ResourcesHolder();
	}

	private ResourcesHolder() {
		frees = new ConcurrentLinkedQueue<>();
		requests = new ConcurrentLinkedQueue<>();
	}

	public static ResourcesHolder getInstance() {
		return ResourcesSingletonHolder.instance;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public synchronized Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> future = new Future<>();
		if(frees.isEmpty())
			requests.add(future);
		else
			future.resolve(frees.poll());
		return future;
	}
	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public synchronized void releaseVehicle(DeliveryVehicle vehicle) {
		if(!requests.isEmpty())
			requests.poll().resolve(vehicle);
		else
			frees.add(vehicle);
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		frees.addAll(Arrays.asList(vehicles));
	}
}