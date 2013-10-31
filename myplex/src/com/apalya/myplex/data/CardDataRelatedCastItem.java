package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedCastItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4050770043406016366L;
	public String _id;
	public String name;
	public List<String> types = new ArrayList<String>();
	public CardDataRelatedCastItem(){}
}
