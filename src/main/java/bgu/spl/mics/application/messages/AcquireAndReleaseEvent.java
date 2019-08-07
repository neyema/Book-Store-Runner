package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class AcquireAndReleaseEvent implements Event<DeliveryVehicle> {
    private Customer customer;
    public AcquireAndReleaseEvent(Customer customer){
        this.customer = customer;
    }

    public Customer getCustomer() { return customer; }
}
