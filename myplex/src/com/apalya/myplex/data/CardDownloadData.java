package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDownloadData {
	public CardDownloadData(){}
	public int ESTStatus;
	public int ESTProgressStatus;
	public long ESTId;
}
