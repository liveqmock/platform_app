package com.apalya.myplex.utils;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.CardData;

public class MessagePost {
	public static final String TAG = "MessgaePost";
	public interface MessagePostCallback{
		public void sendMessage(boolean status);
	}
	private MessagePostCallback mListener;
	public void sendComment(final String comment,CardData data,MessagePostCallback listener){
		this.mListener = listener;
		String requestURl = ConsumerApi.getCommentPostUrl(data._id);
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(Method.POST,requestURl, onlineRequestSuccessListener(), onlineRequestErrorListener()){
			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
		        Map<String, String> params = new HashMap<String, String>();
		        params.put("clientKey", ConsumerApi.DEBUGCLIENTKEY);
		        params.put("comment", comment);
		        return params;
			}
		};
		myReg.setShouldCache(true);
		queue.add(myReg);
		
	}
	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.e(TAG, "response: "+response);
					if(response == null){return;}
					
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
			}
		};
	}
}
