package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseReponseData {
	public String status;
	public String message;
	public int code;

	public BaseReponseData() {

	}

}
