package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataUserReviewsItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8937075285197127739L;
	public String username;
	public String userId;
	public float rating;
	public String review;
	public String timestamp;
	public String name;
	public CardDataUserReviewsItem(){}
}
