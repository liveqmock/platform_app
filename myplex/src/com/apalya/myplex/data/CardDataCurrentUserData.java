package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCurrentUserData {
	public float rating;
	public boolean favorite;
	public CardDataPurchase purchase;
	public CardDataCurrentUserData(){
		
	}
}
