package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedCast implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2783712815633378776L;
	public List<CardDataRelatedCastItem> values = new ArrayList<CardDataRelatedCastItem>();
}
