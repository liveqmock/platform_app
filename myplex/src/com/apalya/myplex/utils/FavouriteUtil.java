package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.cache.CacheHolder;
import com.apalya.myplex.cache.InsertionResult;
import com.apalya.myplex.data.BaseReponseData;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.google.android.gms.internal.er;

public class FavouriteUtil {
	public static final String TAG = "FavouriteUtil";
	public static final int FAVOURITEUTIL_ADD  = 1;
	public static final int FAVOURITEUTIL_REMOVE  = 2;
	private CardData mData;
	private int type = FAVOURITEUTIL_ADD;
	private FavouriteCallback mListener;
	public interface FavouriteCallback{
		public void response(boolean value);
	}
	public void addFavourite(int type,CardData data,FavouriteCallback listener){
		this.mListener = listener;
		this.mData = data;
		this.type = type;
		String url = ConsumerApi.getFavourite(data._id);
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(Method.POST,url, onlineRequestSuccessListener(), onlineRequestErrorListener());
		myReg.setShouldCache(false);
		queue.add(myReg);
		Log.e(TAG, "requestUrl: "+url);
		
	}
	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.e(TAG, "response: "+response);
					if(response == null){
						if(mListener != null){
							mListener.response(false);
						}
						return;
					}
					BaseReponseData responseData = (BaseReponseData) Util.fromJson(response, BaseReponseData.class);
					if(responseData.status == null){
						if(mListener != null){
							mListener.response(false);
						}
						return;
					}
					if(!responseData.status.equalsIgnoreCase("SUCCESS")){
						if(mListener != null){
							mListener.response(false);
						}
						return;
					}
					if(mData.currentUserData != null){
						if(type == FAVOURITEUTIL_ADD){
							mData.currentUserData.favorite = true;
						}else{
							mData.currentUserData.favorite = false;
						}
					}
					List<CardData> datalist = new ArrayList<CardData>();
					datalist.add(mData);
					myplexapplication.getCacheHolder().UpdataDataAsync(datalist, new InsertionResult() {
						
						@Override
						public void updateComplete(Boolean updateStatus) {
							if(mListener != null){
								mListener.response(true);
							}
						}
					});
					
				}catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	}
	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "error response: ");
				if(error != null && error.networkResponse != null){
					Log.e(TAG, "$$$  "+error.networkResponse.statusCode);	
				}
				if(mListener != null){
					mListener.response(false);
				}
			}
		};
	}
}
