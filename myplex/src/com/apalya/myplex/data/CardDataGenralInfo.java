package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataGenralInfo {
    public String id;
    public String type;
    public String title;
    public String category;
    public boolean isSellable;
    public String description;
    public String myplexDescription;
    public String studioDescription;
    public CardDataGenralInfo(){}
}
