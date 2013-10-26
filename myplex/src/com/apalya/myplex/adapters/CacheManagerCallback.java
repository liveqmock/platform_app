package com.apalya.myplex.adapters;

import java.util.HashMap;
import java.util.List;

import com.android.volley.VolleyError;
import com.apalya.myplex.data.CardData;

public interface CacheManagerCallback {

	public void OnCacheResults(HashMap<String,CardData> obj,boolean issuedRequest);
	public void OnOnlineResults(List<CardData> dataList);
	public void OnOnlineError(VolleyError error);
}
