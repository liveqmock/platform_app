package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataVideosItem {
	public String profile; 
	public String format;
	public String type; 
	public String bitrate;
	public String link;
	public String resolution; 
	public CardDataVideosItem(){
		
	}
}
