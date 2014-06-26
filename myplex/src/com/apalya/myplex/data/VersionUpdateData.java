package com.apalya.myplex.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)

public class VersionUpdateData extends BaseReponseData{
	public VersionData app = new VersionData();
	public VersionUpdateData(){}
	

}
