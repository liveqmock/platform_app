package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataImages {
	public List<CardDataImagesItem> values = new ArrayList<CardDataImagesItem>();
	public CardDataImages(){}
}
