package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardResponseData extends BaseReponseData {
	
	/**
	 * 
	 */
	public List<CardData> results = new ArrayList<CardData>();
	public Map<String, String> responseHeaders = Collections.emptyMap();
	public int mStartIndex = 1;
	public CardResponseData(){
		
	}
}
