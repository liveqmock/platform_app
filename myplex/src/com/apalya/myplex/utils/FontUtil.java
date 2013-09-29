package com.apalya.myplex.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class FontUtil {
	
	public static Typeface Roboto_Bold;
	public static Typeface Roboto_Medium;
	public static Typeface Roboto_Regular;
	public static Typeface Roboto_Thin;
	public static Typeface RobotoCondensed_BoldItalic;
	public static Typeface RobotoCondensed_Light;
	
	public static boolean isFontsLoaded = false;
	
	
	public static void loadFonts(AssetManager mgr){
		Roboto_Bold = Typeface.createFromAsset(mgr, "fonts/Roboto-Bold.ttf");
		Roboto_Medium = Typeface.createFromAsset(mgr, "fonts/Roboto-Medium.ttf");
		Roboto_Regular = Typeface.createFromAsset(mgr, "fonts/Roboto-Regular.ttf");
		Roboto_Thin = Typeface.createFromAsset(mgr, "fonts/Roboto-Thin.ttf");
		RobotoCondensed_BoldItalic = Typeface.createFromAsset(mgr, "fonts/RobotoCondensed-BoldItalic.ttf");
		RobotoCondensed_Light = Typeface.createFromAsset(mgr, "fonts/RobotoCondensed-Light.ttf");
		isFontsLoaded = true;
	}

}
