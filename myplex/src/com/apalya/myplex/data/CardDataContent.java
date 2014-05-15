package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6870595256947750470L;
	public String categoryName;
	public String drmType;
	public String releaseDate;
	public List<CardDataGenre> genre;
	public String duration;
	public boolean is3d;
	public CardDataCertifiedRatings certifiedRatings; 
	public List<String> language;
	public boolean drmEnabled;
	public String parentId;
	public String categoryType;
	public String serialNo;
	public String siblingOrder;
	public String startDate;
	public CardDataContent(){}
}
