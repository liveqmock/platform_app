package com.apalya.myplex.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCertifiedRatings implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7863532731979288052L;
	public List<CardDataCertifiedRatingsItem> values = new ArrayList<CardDataCertifiedRatingsItem>();
	public CardDataCertifiedRatings(){}
}
