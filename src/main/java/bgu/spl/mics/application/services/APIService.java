package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.BookStoreRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService {
    LinkedList<BookOrderEvent> orderSchedule;
    Customer customer;
    CountDownLatch countDownLatch;

    public APIService(int id, LinkedList<BookOrderEvent> orders, Customer customer, CountDownLatch cdl) {
        super("API-Service" + id);
        this.orderSchedule = orders;
        this.orderSchedule.sort(Comparator.comparing(BookOrderEvent::getTick));
        this.customer = customer;
        countDownLatch=cdl;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tick -> {
            while(!orderSchedule.isEmpty() && orderSchedule.peek().getTick() <= tick.getCurrentTick()) {
                sendEvent(orderSchedule.poll()); //sends event iff there are events to send and its tick fits
            }
            if(orderSchedule.isEmpty()) {
                terminate();
            }
        });
        subscribeBroadcast(TerminateBroadcast.class, broadcast -> {
            terminate();
        });
        BookStoreRunner.countDown(countDownLatch);
    }
}