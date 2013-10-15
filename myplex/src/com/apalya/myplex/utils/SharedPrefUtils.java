package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefUtils {

	private static SharedPreferences myPrefs;

	/**
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void writeToSharedPref(Context context, String key,
			String value) {

		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		
		if (value.equalsIgnoreCase("")) value = null;
		
		prefsEditor.putString(key, value);
		prefsEditor.commit();
	}
	
	public static void writeToSharedPref(Context context, String key,
			int value) {

		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		
		
		prefsEditor.putInt(key, value);
		prefsEditor.commit();
	}
	
	public static void writeToSharedPref(Context context, String key,
			Boolean value) {

		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefsEditor = myPrefs.edit();
		prefsEditor.putBoolean(key, value);
		prefsEditor.commit();
	}

	/**
	 * 
	 * @param context
	 * @param key
	 * @return String corresponding to the input key
	 */
	public static String getFromSharedPreference(Context context, String key) {

		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
		return myPrefs.getString(key, null);
	}
	
	public static int getIntFromSharedPreference(Context context, String key) {

		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
		return myPrefs.getInt(key, 0);
	}
	
	public static boolean getBoolFromSharedPreference(Context context, String key) {

		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
		return myPrefs.getBoolean(key, false);
	}
	public static void writeList(Context context, List<String> list, String key)
	{
		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = myPrefs.edit();

	    int size = myPrefs.getInt(key+"_size", 0);

	    // clear the previous data if exists
	    for(int i=0; i<size; i++)
	        editor.remove(key+"_"+i);

	    // write the current list
	    for(int i=0; i<list.size(); i++)
	        editor.putString(key+"_"+i, list.get(i));

	    editor.putInt(key+"_size", list.size());
	    editor.commit();
	}
	public static List<String> readList (Context context, String key)
	{
		if (myPrefs == null)
			myPrefs = context.getSharedPreferences(
					null, Context.MODE_PRIVATE);
	    int size = myPrefs.getInt(key+"_size", 0);

	    List<String> data = new ArrayList<String>(size);
	    for(int i=0; i<size; i++)
	        data.add(myPrefs.getString(key+"_"+i, null));

	    return data;
	}
}
