package com.apalya.myplex.data;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCertifiedRatingsItem implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8404342724773295227L;
	public String name;
	public String rating;
	public CardDataCertifiedRatingsItem(){}
}
