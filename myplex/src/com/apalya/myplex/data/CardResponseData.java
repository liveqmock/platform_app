package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardResponseData {
	public String status;
	public String message;
	public int code;
	public List<CardData> results = new ArrayList<CardData>();
	public CardResponseData(){
		
	}
}
