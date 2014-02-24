package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.cache.InsertionResult;
import com.apalya.myplex.data.Bundle;
import com.apalya.myplex.data.BundleContent;
import com.apalya.myplex.data.BundleData;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;


public class BundleUpdateHelper{

	private String requestUrl;
	private static String TAG  = BundleUpdateHelper.class.getName();
	private List<String> contentIds = new ArrayList<String>();
	private List<CardData> cardDatas = new ArrayList<CardData>();
	private InsertionResult insertionResult;

	public BundleUpdateHelper(String url,List<CardData> cardDatas) 
	{
		requestUrl = url;
		this.cardDatas  = cardDatas;
	}

	public void getConnectIds(InsertionResult insertionResult){
		this.insertionResult = insertionResult;
		StringRequest mStringRequest = new StringRequest(requestUrl,new CurrentUserDataSuccessListener()
		,new CurrentUserDataErrorListener());
		RequestQueue queue = MyVolley.getRequestQueue();
		mStringRequest.setShouldCache(false);
		queue.add(mStringRequest);

	}

	private class CurrentUserDataSuccessListener implements Listener<String> {
		@Override
		public void onResponse(String response) {
			Log.d(TAG,"got response ->"+response);
			if(response==null)
				return;
			try{
				Bundle bundle = (Bundle) Util.fromJson(response, Bundle.class);
				if((bundle != null))
					return;
				if((bundle.code != 200) || (!bundle.status.equalsIgnoreCase("SUCCESS"))){
					return;
				}
				List<BundleData> datas = bundle.results.values;
				if(datas ==null && datas.size()==0){
					return;	
				}
				for(BundleData data : datas){
					List<BundleContent> contents = data.contents;
					if(contents!=null && contents.size()>0){
						for(BundleContent content : contents){
							
							String contentId = content.contentId;
							contentIds.add(contentId);						
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			updatecurrentUserData();
		}	
	}

	public void updatecurrentUserData(){
		for(String contentId : contentIds){
			Log.d(TAG,"content id "+contentId);
			CardData newContent = new CardData();
			newContent._id = contentId;
			cardDatas.add(newContent);
		}		
		myplexapplication.getCacheHolder().UpdataDataAsync(cardDatas, insertionResult);	
	}
	private class CurrentUserDataErrorListener implements ErrorListener{

		@Override
		public void onErrorResponse(VolleyError error) {

		}

	}
}
