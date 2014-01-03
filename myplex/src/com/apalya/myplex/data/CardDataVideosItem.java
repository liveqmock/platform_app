package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataVideosItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5038410985071493754L;
	public String profile; 
	public String format;
	public String type; 
	public String bitrate;
	public String link;
	public String resolution; 
	public int elapsedTime;
	public CardDataVideosItem(){
		
	}
}
