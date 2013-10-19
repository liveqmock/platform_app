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
	public static Typeface Roboto_Light;
	public static Typeface ss_gizmo;
	public static Typeface ss_symbolicons_line;
	public static Typeface digital;
	public static boolean isFontsLoaded = false;
	
	
	public static void loadFonts(AssetManager mgr){
		Roboto_Bold = Typeface.createFromAsset(mgr, "fonts/Roboto-Bold.ttf");
		Roboto_Medium = Typeface.createFromAsset(mgr, "fonts/Roboto-Medium.ttf");
		Roboto_Regular = Typeface.createFromAsset(mgr, "fonts/Roboto-Regular.ttf");
		Roboto_Thin = Typeface.createFromAsset(mgr, "fonts/Roboto-Thin.ttf");
		RobotoCondensed_BoldItalic = Typeface.createFromAsset(mgr, "fonts/RobotoCondensed-BoldItalic.ttf");
		RobotoCondensed_Light = Typeface.createFromAsset(mgr, "fonts/RobotoCondensed-Light.ttf");
		Roboto_Light = Typeface.createFromAsset(mgr, "fonts/Roboto-Light.ttf");
		ss_gizmo = Typeface.createFromAsset(mgr, "fonts/ss-gizmo.ttf");
		ss_symbolicons_line = Typeface.createFromAsset(mgr, "fonts/ss-symbolicons-line.ttf");
		digital = Typeface.createFromAsset(mgr, "fonts/digital_7.ttf");
		isFontsLoaded = true;
	}

}
