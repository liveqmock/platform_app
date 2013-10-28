package com.apalya.myplex.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.fasterxml.jackson.core.JsonParseException;

public class CacheManager {
	public static final String TAG = "CacheManager";
	private boolean mAutoSave = true;
	private CacheManagerCallback mListener;

	public void autoSaved(boolean value){
		this.mAutoSave = value;
	}
	public void getCardDetails(final List<CardData> Ids,IndexHandler.OperationType operationType,CacheManagerCallback listener){
		this.mListener = listener;
		if(operationType == IndexHandler.OperationType.DONTSEARCHDB)
		{
			String cardIds = new String();
			for (CardData cardData : Ids) {
				if(cardIds.length() > 0){
					cardIds += ",";
				}
				cardIds += cardData._id;
			}
			if(mListener != null){
				mListener.OnCacheResults(new HashMap<String, CardData>(),true);
			}
			issueOnlineRequest(cardIds);
			return;
		}
		myplexapplication.getCacheHolder().GetData(Ids,operationType, new SearchResult() {
			
			@Override
			public void searchComplete(HashMap<String, CardData> resultMap) {
				if(resultMap == null){
					if(mListener != null){
						mListener.OnCacheResults(null,false);
					}
				}else{
					boolean issuedOnlineRequest = false;
					Set<String> keySet = resultMap.keySet();
					Log.d(TAG,"Number of result found in cache :"+keySet.size());
					String missingCardId = new String();
					for(CardData data: Ids){
						if(!keySet.contains(data._id)){
							if(missingCardId.length() > 0){
								missingCardId += ",";
							}
							missingCardId += data._id;
						}
					}
					if(missingCardId.length() >0){
						issuedOnlineRequest = true;
						issueOnlineRequest(missingCardId);
					}
					if(mListener != null){
						mListener.OnCacheResults(resultMap,issuedOnlineRequest);
					}
				}
			}
		});
	}
	
	private void issueOnlineRequest(String cardIds){
		cardIds.replace(" ", "%20");
		String url = ConsumerApi.getContentDetail(cardIds, ConsumerApi.LEVELDEVICEMAX);
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(url, onlineRequestSuccessListener(), onlineRequestErrorListener());
//		myReg.setShouldCache(true);
		myReg.setRetryPolicy(new CacheRetry());
		queue.add(myReg);
		Log.d(TAG,"issueOnlineRequest :"+url+" timeout "+myReg.getRetryPolicy().getCurrentTimeout());
	}
	private class CacheRetry implements RetryPolicy{

		@Override
		public int getCurrentTimeout() {
			return 10000;
		}

		@Override
		public int getCurrentRetryCount() {
			return 0;
		}

		@Override
		public void retry(VolleyError error) throws VolleyError {
			// TODO Auto-generated method stub
			
		}
		
	}
	private void addToCache(final CardResponseData minResultSet){
		if(mAutoSave){
			myplexapplication.getCacheHolder().UpdataDataAsync(minResultSet.results, new InsertionResult() {
				
				@Override
				public void updateComplete(Boolean updateStatus) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		if(mListener != null){
			mListener.OnOnlineResults(minResultSet.results);
		}
	}
	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					CardResponseData maxResultSet  = (CardResponseData) Util.fromJson(response, CardResponseData.class);
					if(maxResultSet.results != null){
						Log.d(TAG,"Number of result from online request :"+maxResultSet.results.size());
					}
					addToCache(maxResultSet);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG,"Error for server  :"+error);
				if(mListener != null){
					mListener.OnOnlineError(error);
				}
			}
		};
	}
	public void unRegisterCallback(){
		this.mListener = null;
	}
	
	
}
