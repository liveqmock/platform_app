package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedMultimedia implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8855822486678329965L;
	public List<CardDataRelatedMultimediaItem> values = new ArrayList<CardDataRelatedMultimediaItem>();
	public CardDataRelatedMultimedia(){}
}
