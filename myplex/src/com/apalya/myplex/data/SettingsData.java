package com.apalya.myplex.data;

public class SettingsData {
	public static final int ITEM = 0;
	public static final int SECTION = 1;
	public String mSettingName;
	public final int type;
	public int imageId; 
	
	public SettingsData(int type, String text, int resourceId) {
		this.type = type;
		this.mSettingName = text;
		this.imageId = resourceId;
	}
}
