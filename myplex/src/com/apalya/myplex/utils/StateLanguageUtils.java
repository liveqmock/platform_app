package com.apalya.myplex.utils;

import java.util.HashMap;

public class StateLanguageUtils 
{
	private HashMap<String, String> hashMap = new HashMap<String, String>();
	private static StateLanguageUtils utils = null;
	
	private StateLanguageUtils() {
	}
	public static StateLanguageUtils getInstance(){
		if(utils==null)
			utils = new StateLanguageUtils();
		return utils;
	}
	
	
	public void init() {
		hashMap.put("Andhra Pradesh", "Telugu");
		hashMap.put("Assam", "Assamese");
		hashMap.put("Bihar", "Hindi");
		hashMap.put("Chhattisgarh", "Hindi");
		hashMap.put("Gujarat", "Gujarati");
		hashMap.put("Haryana", "Hindi");
		
		hashMap.put("Himachal Pradesh", "Hindi");
		hashMap.put("Jammu and Kashmir", "Hindi");
		hashMap.put("Jharkhand", "Hindi");
		hashMap.put("Karnataka", "Kannada");
		hashMap.put("Kerala", "Malayalam");
		
		hashMap.put("Madhya Pradesh", "Hindi");
		hashMap.put("Maharashtra", "Marathi");
		hashMap.put("Punjab", "Punjabi");
		hashMap.put("Rajasthan", "Hindi");
		
		hashMap.put("Tamil Nadu", "Tamil");
		hashMap.put("West Bengal", "Bengali");		
		hashMap.put("Orissa", "Oriya");		

	}
	public String getLanguage(String state){
		if(hashMap.get(state.trim())!= null){
			return hashMap.get(state);
		}else if((hashMap.get(state.split(" ")[0]))!=null){
			return hashMap.get(state);
		}else{
			return "Hindi";
		}
	}

}
