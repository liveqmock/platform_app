package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPurchaseItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5717030035009298915L;
	public String type;
	public String contentType;
	public String packageId;
	public String validity;
	public CardDataPurchaseItem(){}
}
