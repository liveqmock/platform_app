package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataImagesItem {
	public String profile;
	public String link;
	public String type;
	public CardDataImagesItem(){}
}
