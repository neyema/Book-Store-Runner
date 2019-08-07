package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer implements Serializable {
	private String name;
	private int id;
	private String address;
	private int distance;
	private List<OrderReceipt> receipts;
	private int availableAmountInCreditCard;
	private int creditCard;

	public Customer(String name, int id, String address, int distance, int availableAmountInCreditCard, int creditCard) {
		this.name = name;
		this.id = id;
		this.address = address;
		this.distance = distance;
		receipts = new LinkedList<>();
		this.availableAmountInCreditCard = availableAmountInCreditCard;
		this.creditCard = creditCard;
	}

	/**
     * Retrieves the name of the customer.
     */
	public String getName() {
		return name;
	}

	/**
     * Retrieves the ID of the customer  . 
     */
	public int getId() {
		return id;
	}
	
	/**
     * Retrieves the address of the customer.  
     */
	public String getAddress() {
		return address;
	}
	
	/**
     * Retrieves the distance of the customer from the store.  
     */
	public int getDistance() {
		return distance;
	}

	
	/**
     * Retrieves a list of receipts for the purchases this customer has made.
     * <p>
     * @return A list of receipts.
     */
	public List<OrderReceipt> getCustomerReceiptList() {
		return receipts;
	}
	
	/**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     * @return Amount of money left.   
     */
	public int getAvailableCreditAmount() {
		return availableAmountInCreditCard;
	}
	
	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditCard;
	}

	public void chargeCreditCard(int amount) {
		availableAmountInCreditCard = availableAmountInCreditCard - amount;
	}
	
}
