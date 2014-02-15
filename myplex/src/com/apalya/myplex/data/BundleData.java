package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleData 
{
	public String contentType;
	public boolean couponFlag;	
	public String packageName;
	public String bbDescription;
	public String packageId;
	public String duration;
	public String commercialModel;
	public boolean packageIndicator;
	public boolean renewalFlag;
	public List<BundleContent> contents = new ArrayList<BundleContent>();
	public BundleData(){		
	}

}
