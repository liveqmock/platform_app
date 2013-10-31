package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCommentsItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3062094924676148622L;
	public String name;
	public int userId;
	public String comment;
	public String timestamp;
	public CardDataCommentsItem(){}
}
