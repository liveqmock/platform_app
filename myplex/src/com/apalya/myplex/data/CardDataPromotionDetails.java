package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPromotionDetails {
	public List<CardDataPromotionDetailsItem> valueList = new ArrayList<CardDataPromotionDetailsItem>();
	public CardDataPromotionDetails(){}
}
