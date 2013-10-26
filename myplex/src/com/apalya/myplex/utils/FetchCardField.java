package com.apalya.myplex.utils;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;
import com.fasterxml.jackson.core.JsonParseException;

public class FetchCardField {
	public interface FetchComplete{
		public void response(CardResponseData data);
	}
	private static final String TAG = "FetchCardField";
	private FetchComplete mListener;
	
	public void Fetch(CardData data,String field,FetchComplete listener){
		mListener = listener;
		String requestURl = new String();
		requestURl = ConsumerApi.getContentDetail(data._id, ConsumerApi.LEVELDEVICEMAX);
		requestURl += ConsumerApi.AMPERSAND +ConsumerApi.FIELDS+field;
		RequestQueue queue = MyVolley.getRequestQueue();
		
		StringRequest myReg = new StringRequest(requestURl, successListener(), errorListener());
		myReg.setShouldCache(false);
		Log.d(TAG,"Min Request:"+requestURl);
		queue.add(myReg);
	}
	private Response.Listener<String> successListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				CardResponseData minResultSet = null;
				try {
					Log.d(TAG,"server response "+response);
					minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(mListener != null){
					mListener.response(minResultSet);
				}
			}
		};
	}

	private Response.ErrorListener errorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG,"Error from server "+error.networkResponse);
				if(mListener != null){
					mListener.response(null);
				}
			}
		};
	}
}
