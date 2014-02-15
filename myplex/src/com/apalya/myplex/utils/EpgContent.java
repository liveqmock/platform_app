package com.apalya.myplex.utils;

public class EpgContent 
{	
	public final String Name;
	public final String StartTime;
	public final String EndTime;

	public EpgContent(String Name, String StartTime, String EndTime) {
		this.Name = Name;
		this.StartTime = StartTime;
		this.EndTime = EndTime;
	}
}
