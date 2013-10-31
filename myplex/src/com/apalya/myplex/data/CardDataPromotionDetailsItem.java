package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPromotionDetailsItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8536060706610835844L;
	public String amount;
	public String promotionId;
	public String promotionName;
	public String promotionType;
	public String promotionalPrice;
	public String percentage;
	public CardDataPromotionDetailsItem(){
		
	}
}
