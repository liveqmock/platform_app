package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataUserReviews {

	public int numUsersRated;
	public List<CardDataUserReviewsItem> values = new ArrayList<CardDataUserReviewsItem>();
	public float averageRating;
	public CardDataUserReviews(){
		
	}
}
