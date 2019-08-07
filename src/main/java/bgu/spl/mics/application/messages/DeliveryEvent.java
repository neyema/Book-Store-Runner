package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class DeliveryEvent implements Event<DeliveryVehicle> {
    private String address;
    private int distance;
    public DeliveryEvent(String address, int distance){
        this.address = address;
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public int getDistance() {
        return distance;
    }
}