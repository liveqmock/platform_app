package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCommentsItem {
	public String name;
	public int userId;
	public String comment;
	public String timestamp;
	public CardDataCommentsItem(){}
}
