package com.apalya.myplex.utils;

public class EpgContent 
{	
	public final String Name;
	public final String StartTime;
	public final String EndTime;
	public final String assetType;
	public final String assetUrl;

	public EpgContent(String Name, String StartTime, String EndTime,String assetType,String assetUrl) {
		this.Name = Name;
		this.StartTime = StartTime;
		this.EndTime = EndTime;
		this.assetType = assetType;
		this.assetUrl = assetUrl;
	}
}
