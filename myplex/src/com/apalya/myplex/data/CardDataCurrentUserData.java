package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCurrentUserData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5959237256889098035L;
	public float rating;
	public boolean favorite;
	public List<CardDataPurchaseItem> purchase;
	public CardDataCurrentUserData(){
		
	}
}
