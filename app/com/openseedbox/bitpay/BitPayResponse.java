package com.openseedbox.bitpay;

import java.util.Map;

public class BitPayResponse {
	
	public static final String STATUS_NEW = "new";
	public static final String STATUS_PAID = "paid";
	public static final String STATUS_CONFIRMED = "confirmed";
	public static final String STATUS_COMPLETE = "complete";
	public static final String STATUS_EXPIRED = "expired";
	public static final String STATUS_INVALID = "invalid";
	
	private String id;
	private String url;
	private String posData;
	private String status;
	private String price;
	private String currency;
	private String btcPrice;
	private long invoiceTime;
	private long expirationTime;
	private long currentTime;
	
	/* Getters and Setters */
	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public String getPosData() {
		return posData;
	}

	public String getStatus() {
		return status;
	}

	public String getPrice() {
		return price;
	}

	public String getCurrency() {
		return currency;
	}

	public String getBtcPrice() {
		return btcPrice;
	}

	public long getInvoiceTime() {
		return invoiceTime;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	public long getCurrentTime() {
		return currentTime;
	}
	
}
