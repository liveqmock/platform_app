package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataGenralInfo implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1897942798104694998L;
	public String id;
    public String type;
    public String title;
    public String category;
    public boolean isSellable;
    public String description;
    public String briefDescription;
    public String myplexDescription;
    public String studioDescription;
    public CardDataGenralInfo(){}
}
