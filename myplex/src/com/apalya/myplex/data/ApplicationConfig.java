package com.apalya.myplex.data;

import java.io.File;

public class ApplicationConfig {
	public static final String LDPI = "ldpi";
	public static final String MDPI = "mdpi";
	public static final String HDPI = "hdpi";
	public static final String XHDPI = "xhdpi";
	public static final String XXHDPI = "xxhdpi";
	public static final String TABLET = "tablet";
	
	public int screenWidth;
	public int screenHeight;
	public String type = MDPI;
	
	public String indexFilePath = null;
}
