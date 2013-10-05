package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedContent {
	public int duration;
	public CardDataCertifiedRatings  certifiedRatings;
	public CardDataRelatedContent(){}
}
