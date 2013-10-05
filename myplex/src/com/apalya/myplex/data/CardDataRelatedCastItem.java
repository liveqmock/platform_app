package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedCastItem {
	public String _id;
	public String name;
	public List<String> types = new ArrayList<String>();
	public CardDataRelatedCastItem(){}
}
