package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPackagePriceDetailsItem {
	public float price;
	public String paymentChannel;
	public boolean doubleConfirmation;
	public boolean webBased;
	public String name;
	public CardDataPackagePriceDetailsItem(){}
}
