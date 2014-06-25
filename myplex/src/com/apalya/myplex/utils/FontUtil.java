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
		Roboto_Bold = createFromAsset(mgr, "fonts/Roboto-Bold.ttf");
		Roboto_Medium = createFromAsset(mgr, "fonts/Roboto-Medium.ttf");
		Roboto_Regular = createFromAsset(mgr, "fonts/Roboto-Regular.ttf");
		Roboto_Thin = createFromAsset(mgr, "fonts/Roboto-Thin.ttf");
		RobotoCondensed_BoldItalic = createFromAsset(mgr, "fonts/RobotoCondensed-BoldItalic.ttf");
		RobotoCondensed_Light = createFromAsset(mgr, "fonts/RobotoCondensed-Light.ttf");
		Roboto_Light = createFromAsset(mgr, "fonts/Roboto-Light.ttf");
		ss_gizmo = createFromAsset(mgr, "fonts/ss-gizmo.ttf");
		ss_symbolicons_line = createFromAsset(mgr, "fonts/ss-symbolicons-line.ttf");
		digital = createFromAsset(mgr, "fonts/digital_7.ttf");
		isFontsLoaded = true;
	}

	private static Typeface createFromAsset(AssetManager mgr , String fontPath){

		 try {
			 return  Typeface.createFromAsset(mgr, fontPath);				 
		 }catch(Throwable e){
			 e.printStackTrace();
		 }

		return Typeface.DEFAULT;
	}


}
