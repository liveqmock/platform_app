package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPurchaseItem {
	public String type;
	public String validity;
	public CardDataPurchaseItem(){}
}
