package com.apalya.myplex.utils;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.BaseReponseData;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;

public class MessagePost {
	public static final String TAG = "MessagePost";
	public static final int POST_COMMENT = 1;
	public static final int POST_RATING = 2;
	public interface MessagePostCallback{
		public void sendMessage(boolean status);
	}
	private MessagePostCallback mListener;
	
	public void sendComment(final String message,final int value, CardData data,final int type,MessagePostCallback listener){
		this.mListener = listener;
		String requestURl = new String();
		if(type == POST_COMMENT){
			requestURl = ConsumerApi.getCommentPostUrl(data._id);
		}else{
			requestURl = ConsumerApi.getRatingPostUrl(data._id);
		}
		
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(Method.POST,requestURl, onlineRequestSuccessListener(), onlineRequestErrorListener()){
			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				
		        Map<String, String> params = new HashMap<String, String>();
		        params.put("clientKey", ConsumerApi.DEBUGCLIENTKEY);
		        if(type == POST_COMMENT){
		        	params.put("comment", message);	
		        }else{
		        	params.put("review", message);
		        	params.put("rating", ""+value);
		        }
		        return params;
			}
		};
		Log.e(TAG, "request: "+requestURl);
		myReg.setShouldCache(false);
		queue.add(myReg);
	}
	private void sendCallBack(boolean value){
		if(mListener != null){
			mListener.sendMessage(value);
		}
	}
	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
			BaseReponseData responseData = null;
			Log.e(TAG, "response: "+response);
			if(response == null){sendCallBack(false);return;}
			try {
				responseData = (BaseReponseData) Util.fromJson(response, CardResponseData.class);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if(responseData == null ){sendCallBack(false);return;}
			if(responseData.code >= 200 &&  responseData.code < 300){
				sendCallBack(true);
				return;
			}
			sendCallBack(false);
			return;
			}
		};
	}
	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "error response: "+error);
				sendCallBack(false);
			}
		};
	}
}
