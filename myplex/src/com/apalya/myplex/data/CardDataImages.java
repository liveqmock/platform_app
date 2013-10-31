package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataImages implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5513734722684210985L;
	public List<CardDataImagesItem> values = new ArrayList<CardDataImagesItem>();
	public CardDataImages(){}
}
