package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataGenre implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6905498495722059016L;
	public String id;
	public String name;
	public CardDataGenre(){}
}
