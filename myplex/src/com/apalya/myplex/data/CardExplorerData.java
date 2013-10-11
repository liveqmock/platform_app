package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.HashMap;

public class CardExplorerData {
	public static final int REQUEST_RECOMMENDATION = 1;
	public static final int REQUEST_SEARCH = 2;
	public static final int REQUEST_FAVOURITE = 3;
	public static final int REQUEST_SIMILARCONTENT = 4;
	public String requestUrl = new String();
	public int currentSelectedCard = 0;
	public  HashMap<String,CardData> mEntries = new HashMap<String,CardData>();
	public ArrayList<CardData> mMasterEntries = new ArrayList<CardData>();
	public int mStartIndex = 0;
	public String searchQuery = new String(); 
	public int requestType = REQUEST_SEARCH;
	public boolean continueWithExisting = false;
	
	public void reset(){
		requestType = REQUEST_SEARCH;
		searchQuery = new String();
		currentSelectedCard = 0;
		mStartIndex = 0;
		mEntries = new HashMap<String,CardData>();
		mMasterEntries = new ArrayList<CardData>();
		continueWithExisting = false;
	}
}
