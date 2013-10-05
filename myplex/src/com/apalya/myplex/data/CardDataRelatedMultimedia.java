package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedMultimedia {
	public List<CardDataRelatedMultimediaItem> values = new ArrayList<CardDataRelatedMultimediaItem>();
	public CardDataRelatedMultimedia(){}
}
