package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataUserReviewsItem {
	public String username;
	public String userId;
	public float rating;
	public String review;
	public String timestamp;
	public CardDataUserReviewsItem(){}
}
