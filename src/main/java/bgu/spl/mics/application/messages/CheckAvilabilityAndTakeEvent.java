package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;

public class CheckAvilabilityAndTakeEvent implements Event<Integer> {
    private String bookTitle;
    private int customerBalance;

    public CheckAvilabilityAndTakeEvent(String bookTitle, int customerBalance) {
        this.bookTitle = bookTitle;
        this.customerBalance = customerBalance;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public int getCustomerBalance() {
        return customerBalance;
    }
}