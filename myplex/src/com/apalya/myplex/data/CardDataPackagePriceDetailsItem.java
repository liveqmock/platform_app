package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPackagePriceDetailsItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5840267809143390511L;
	public float price;
	public String paymentChannel;
	public boolean doubleConfirmation;
	public boolean webBased;
	public String name;
	public CardDataPackagePriceDetailsItem(){}
}
