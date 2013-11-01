package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataImagesItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9006518291552596096L;
	public String profile;
	public String link;
	public String type;
	public String resolution;
	public CardDataImagesItem(){}
}
