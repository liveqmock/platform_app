package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataAwardsItem {
	public String _id;
	public String name;
	public String description;
	public String awardDate;
	public String category;
	public CardDataAwardsItem(){}
}
