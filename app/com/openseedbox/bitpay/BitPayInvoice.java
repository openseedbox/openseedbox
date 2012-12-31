package com.openseedbox.bitpay;

import java.util.HashMap;
import java.util.Map;

public class BitPayInvoice {
	
	public static final String TRANSACTION_SPEED_HIGH = "high";
	public static final String TRANSACTION_SPEED_MEDIUM = "medium";
	public static final String TRANSACTION_SPEED_LOW = "low";
	
	private String price;
	private BitPayCurrency currency;
	private String posData;
	private String notificationURL;
	private String transactionSpeed;
	private boolean fullNotifications;
	private String redirectURL;
	private String orderID;
	private String itemDesc;
	private String notificationEmail;
	private String buyerName;
	private String buyerEmail;
	
	public BitPayInvoice() {
		this.transactionSpeed = TRANSACTION_SPEED_HIGH;
		this.fullNotifications = false;
	}
	
	public enum BitPayCurrency {
		USD, EUR, BTC
	}
	
	/* Getters and Setters */
	public void setPrice(String price) {
		this.price = price;
	}

	public void setCurrency(BitPayCurrency currency) {
		this.currency = currency;
	}

	public void setPosData(String posData) {
		this.posData = posData;
	}

	public void setNotificationURL(String notificationURL) {
		this.notificationURL = notificationURL;
	}

	public void setFullNotifications(boolean fullNotifications) {
		this.fullNotifications = fullNotifications;
	}

	public void setTransactionSpeed(String transactionSpeed) {
		this.transactionSpeed = transactionSpeed;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
		this.posData = orderID;
	}

	public void setItemDesc(String itemDesc) {
		this.itemDesc = itemDesc;
	}

	public void setNotificationEmail(String notificationEmail) {
		this.notificationEmail = notificationEmail;
	}

	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}

	public void setBuyerEmail(String buyerEmail) {
		this.buyerEmail = buyerEmail;
	}
	
}
