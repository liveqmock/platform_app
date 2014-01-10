package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsData extends BaseReponseData {
	
	public Result results = new Result(); 
	public ResultsData() {
		
	}
}
