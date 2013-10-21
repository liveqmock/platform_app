package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPromotionDetailsItem {
	public String amount;
	public String promotionId;
	public String promotionName;
	public String promotionType;
	public String promotionalPrice;
	public String percentage;
	public CardDataPromotionDetailsItem(){
		
	}
}
