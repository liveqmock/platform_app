package com.apalya.myplex.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCertifiedRatingsItem {
	public String name;
	public String rating;
	public CardDataCertifiedRatingsItem(){}
}
