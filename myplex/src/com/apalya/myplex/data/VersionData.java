package com.apalya.myplex.data;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class VersionData {
	public int version;
	public String link;
	public String message;
	public String type;
	
	public boolean validate(){
		
		if(TextUtils.isEmpty(link) || TextUtils.isEmpty(message) || TextUtils.isEmpty(type) || version == 0){
			return false;
		}
		
		return true;
	}
}
