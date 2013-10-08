package com.apalya.myplex.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCurrentUserData {
	public float rating;
	public boolean favorite;
	public List<CardDataPurchaseItem> purchase;
	public CardDataCurrentUserData(){
		
	}
}
