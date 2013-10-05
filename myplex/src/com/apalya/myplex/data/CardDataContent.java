package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataContent {
	public String categoryName;
	public String drmType;
	public String releaseDate;
	public List<CardDataGenre> genreList = new ArrayList<CardDataGenre>();
	public int duration;
	public boolean is3d;
	public CardDataCertifiedRatings certifiedRatings; 
	public List<String> language;
	public boolean drmEnabled;
	public String parentId;
	public String categoryType;
	public CardDataContent(){}
}
