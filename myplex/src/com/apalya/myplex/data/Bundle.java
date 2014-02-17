package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bundle extends BaseReponseData 
{

	public BundleResult results = new BundleResult();
	
}
