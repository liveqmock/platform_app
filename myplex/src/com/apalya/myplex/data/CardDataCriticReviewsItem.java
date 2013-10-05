package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCriticReviewsItem {
    public String name;
    public float rating;
    public String review;
    public CardDataCriticReviewsItem(){}
}
