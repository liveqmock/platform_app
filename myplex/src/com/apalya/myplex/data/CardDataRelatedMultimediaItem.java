package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedMultimediaItem {
	public String _id;
	public CardDataGenralInfo generalInfo;
	public CardDataRelatedMultimediaItem(){}
}
