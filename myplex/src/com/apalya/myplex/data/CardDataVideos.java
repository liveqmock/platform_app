package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataVideos implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4394934829056419470L;
	public List<CardDataVideosItem> values = new ArrayList<CardDataVideosItem>();
	public String status;
	public String message;
	public CardDataVideos(){
		
	}
}
