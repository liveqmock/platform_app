package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;



import com.android.volley.VolleyError;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;


public class LastWatchedCardDetails implements CacheManagerCallback{

	public List<CardData> lastVisitedCardData=new ArrayList<CardData>();
	private CacheManager mCacheManager = new CacheManager();

	public LastWatchedCardDetails() {
		// TODO Auto-generated constructor stub
	}

	public void getLastWatchedCardDetails(){


		mCacheManager.getCardDetails(myplexapplication.getUserProfileInstance().lastVisitedCardData,IndexHandler.OperationType.IDSEARCH,this);

	}

	@Override
	public void OnCacheResults(HashMap<String, CardData> object,boolean issuedRequest) {
		 if(object == null){
		return;
	}
	Set<String> keySet = object.keySet();
	List<CardData> cardList=new ArrayList<CardData>();
	for(String key:keySet){
		cardList.add(object.get(key));
	}
	myplexapplication.getUserProfileInstance().lastVisitedCardData.clear();
	myplexapplication.getUserProfileInstance().lastVisitedCardData=cardList;
	mCacheManager.unRegisterCallback();
}

@Override
public void OnOnlineResults(List<CardData> dataList) {
	// TODO Auto-generated method stub
	for(CardData data:dataList)
	myplexapplication.getUserProfileInstance().lastVisitedCardData.add(data);
}

@Override
public void OnOnlineError(VolleyError error) {
	// TODO Auto-generated method stub

}

}
