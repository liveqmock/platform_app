package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleContent {
	public String contentId;
	public String contentName;
	public BundleContent(){
	}

}
