package com.apalya.myplex.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class FontUtil {
	
	public static Typeface helveticaNeueLTPro_Cn;
	public static Typeface helveticaNeueLTPro_Lt;
	public static Typeface helveticaNeueLTPro_Md;
	public static Typeface Roboto_Regular;
	public static Typeface Roboto_Medium;
	public static Typeface RobotoCondensed_BoldItalic;
	public static boolean isFontsLoaded = false;
	
	
	public static void loadFonts(AssetManager mgr){
		helveticaNeueLTPro_Cn = Typeface.createFromAsset(mgr, "fonts/HelveticaNeueLTPro-Cn.otf");
		helveticaNeueLTPro_Lt = Typeface.createFromAsset(mgr, "fonts/HelveticaNeueLTPro-Lt.otf");
		helveticaNeueLTPro_Md = Typeface.createFromAsset(mgr, "fonts/HelveticaNeueLTPro-Md.otf");
		Roboto_Regular = Typeface.createFromAsset(mgr, "fonts/Roboto-Regular.ttf");
		Roboto_Medium = Typeface.createFromAsset(mgr, "fonts/Roboto-Medium.ttf");
		RobotoCondensed_BoldItalic = Typeface.createFromAsset(mgr, "fonts/RobotoCondensed-BoldItalic.ttf");
		isFontsLoaded = true;
	}

}
