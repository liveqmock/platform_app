package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedMultimediaItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7658987893462658720L;
	public String _id;
	public CardDataGenralInfo generalInfo;
	public CardDataRelatedMultimediaItem(){}
}
