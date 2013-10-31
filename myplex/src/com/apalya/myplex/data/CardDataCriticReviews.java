package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCriticReviews implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8436168085963617386L;
	public List<CardDataCriticReviewsItem> values = new ArrayList<CardDataCriticReviewsItem>();
	public CardDataCriticReviews(){}
}
