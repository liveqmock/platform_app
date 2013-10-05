package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataAwards {
	public List<CardDataAwardsItem> values = new ArrayList<CardDataAwardsItem>();
	public CardDataAwards(){}
}
