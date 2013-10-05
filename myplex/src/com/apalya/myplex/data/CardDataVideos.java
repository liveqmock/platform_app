package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataVideos {
	public List<CardDataVideosItem> values = new ArrayList<CardDataVideosItem>();
	public CardDataVideos(){
		
	}
}
