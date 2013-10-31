package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardResponseData extends BaseReponseData {
	
	/**
	 * 
	 */
	public List<CardData> results = new ArrayList<CardData>();
	public CardResponseData(){
		
	}
}
