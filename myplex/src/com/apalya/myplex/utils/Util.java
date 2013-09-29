package com.apalya.myplex.utils;

import java.io.File;

import android.content.res.Resources;
import android.util.TypedValue;

public class Util {

	public static int dpToPx(Resources res, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
	}
	
	/**
	   * Checks if the phone is rooted.
	   * 
	   * @return <code>true</code> if the phone is rooted, <code>false</code>
	   * otherwise.
	   */
	  public static boolean isPhoneRooted() {

	    // get from build info
	    String buildTags = android.os.Build.TAGS;
	    if (buildTags != null && buildTags.contains("test-keys")) {
	      return true;
	    }

	    // check if /system/app/Superuser.apk is present
	    try {
	      File file = new File("/system/app/Superuser.apk");
	      if (file.exists()) {
	        return true;
	      }
	    } catch (Throwable e1) {
	      // ignore
	    }

	 // try executing commands
	    return canExecuteCommand("/system/xbin/which su")
	        || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
	  }
	  
	// executes a command on the system
	  private static boolean canExecuteCommand(String command) {
	    boolean executedSuccesfully;
	    try {
	      Runtime.getRuntime().exec(command);
	      executedSuccesfully = true;
	    } catch (Exception e) {
	      executedSuccesfully = false;
	    }

	    return executedSuccesfully;
	  }

}
