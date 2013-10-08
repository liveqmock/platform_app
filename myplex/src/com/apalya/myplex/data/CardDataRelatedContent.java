package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedContent {
	public String duration;
	public CardDataCertifiedRatings  certifiedRatings;
	public CardDataRelatedContent(){}
}
