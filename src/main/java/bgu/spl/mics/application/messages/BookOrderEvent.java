package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

public class BookOrderEvent implements Event<OrderReceipt> {
    private Customer customer;
    private String bookTitle;
    private int tick;

    public BookOrderEvent(Customer customer, String bookTitle, int Tick) {
        this.customer = customer;
        this.bookTitle = bookTitle;
        this.tick = Tick;
    }

    public Customer getCustomer() { return this.customer; }

    public String getBookTitle() { return this.bookTitle; }

    public int getTick() { return this.tick; }
}