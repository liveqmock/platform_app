package com.apalya.myplex.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GZipRequest;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardData.HTTP_SOURCE;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.HttpTimeOut;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.fasterxml.jackson.core.JsonParseException;

public class CacheManager {
	public static final String TAG = "CacheManager";
	private boolean mAutoSave = true;	
	private CacheManagerCallback mListener;

	private List<CardData> mDynamicDetailList;
	public void autoSaved(boolean value){
		this.mAutoSave = value;
	}
	
	public void deRegistration(){
		mListener = null;
	}
	public void getCardDetails(final List<CardData> Ids,IndexHandler.OperationType operationType,CacheManagerCallback listener){
		this.mListener = listener;
		this.mDynamicDetailList = Ids;
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
				mListener.OnCacheResults(new HashMap<String, CardData>(),false);
			}
			//issueOnlineRequest(cardIds);
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
					
					
					fillDynamicData(resultMap);
					
					if(mListener != null){
						mListener.OnCacheResults(resultMap,issuedOnlineRequest);
					}
				}
			}
		});
	}
	private void fillDynamicData(HashMap<String, CardData> resultMap){
		if(mDynamicDetailList == null || mDynamicDetailList.size() == 0 || resultMap == null || resultMap.size() == 0){
			return;
		}
		List<CardData> modifiedDataList = new ArrayList<CardData>();
		for(CardData data:mDynamicDetailList){
			if(data.currentUserData != null)
			{
				CardData resultData = resultMap.get(data._id);
				if(resultData == null ){continue;}
				Log.d(TAG,"updating dynamic data for "+data._id);
				

				if(resultData.currentUserData.purchase !=null && !resultData.currentUserData.purchase.isEmpty() && Util.isTokenValid(resultData.currentUserData.purchase.get(0).validity))
				{
					Log.i(TAG, "Cache data is valid");
					if(data.currentUserData.purchase !=null)
						data.currentUserData.purchase = resultData.currentUserData.purchase;
				}
				data.currentUserData.favorite = resultData.currentUserData.favorite;
				
				resultData.currentUserData = data.currentUserData;
				resultData.userReviews = data.userReviews;
				resultData.criticReviews = data.criticReviews;
				resultData._expiresAt = data._expiresAt;
				resultData.packages=data.packages;
				resultData.httpSource=data.httpSource;
				if(data.comments != null){
					Log.d(TAG,"number of comments for  "+data._id+" "+data.comments.numComments);	
				}
				resultData.comments = data.comments;
				if(resultData.httpSource == HTTP_SOURCE.ONLINE){
					modifiedDataList.add(resultData);
				}
			}
		}
		if(modifiedDataList.size() > 0){
			myplexapplication.getCacheHolder().UpdataDataAsync(modifiedDataList, new InsertionResult() {
				
				@Override
				public void updateComplete(Boolean updateStatus) {
					// TODO Auto-generated method stub
				}
			});
		}
	}
	private void issueOnlineRequest(String cardIds){
		cardIds = cardIds.replace(" ", "%20");
		String url = ConsumerApi.getContentDetail(cardIds, ConsumerApi.LEVELDEVICEMAX);
		RequestQueue queue = MyVolley.getRequestQueue();
		GZipRequest myReg = new GZipRequest(url, onlineRequestSuccessListener(), onlineRequestErrorListener());
//		myReg.printLogs(true);
		myReg.setShouldCache(false);
		myReg.setRetryPolicy(new HttpTimeOut(10000));
		queue.add(myReg);
		Log.d(TAG,"issueOnlineRequest :"+url+" timeout "+myReg.getRetryPolicy().getCurrentTimeout());
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
	private Response.Listener<CardResponseData> onlineRequestSuccessListener() {
		return new Response.Listener<CardResponseData>() {
			@Override
			public void onResponse(CardResponseData maxResultSet) {
				try {
//					CardResponseData maxResultSet  = (CardResponseData) Util.fromJson(response, CardResponseData.class);
					if(maxResultSet == null || maxResultSet.results == null){return;}
					
					if(maxResultSet.results != null){
						Log.d(TAG,"Number of result from online request :"+maxResultSet.results.size());
					}
					addToCache(maxResultSet);
				} catch (Exception e) {
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
