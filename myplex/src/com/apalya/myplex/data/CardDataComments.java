package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataComments implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 564071273732829007L;
	public List<CardDataCommentsItem> values = new ArrayList<CardDataCommentsItem>();
	public CardDataComments(){
		
	}
}
