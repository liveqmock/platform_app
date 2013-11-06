package com.apalya.myplex.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDownloadData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8475605841159873373L;
	public CardDownloadData(){}
	public boolean mCompleted = false;
	public int mPercentage;
	public long mDownloadId = -1;
}
