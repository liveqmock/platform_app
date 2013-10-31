package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 474778913019096728L;
	public String duration;
	public CardDataCertifiedRatings  certifiedRatings;
	public CardDataRelatedContent(){}
}
