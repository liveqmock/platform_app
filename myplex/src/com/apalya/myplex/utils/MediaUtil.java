package com.apalya.myplex.utils;

import java.io.IOException;
import java.util.EventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.utils.MediaUtil.MediaUtilEventListener;
import com.apalya.myplex.utils.WidevineDrm.WidevineDrmLogEventListener;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MediaUtil {

	public interface MediaUtilEventListener{
		public void urlReceived(boolean aStatus,String url);
	}
	public static MediaUtilEventListener urlEventListener;
	private static String bitrateType;

	public static void getVideoUrl(String aContentId,String bitRate){
		bitrateType=bitRate;
		String url=ConsumerApi.getVideosDetail(aContentId);
		getContentUrlReq(url);
	}
	private static void getContentUrlReq(String aVideoUrl) {
		// TODO Auto-generated method stub
		RequestQueue queue = MyVolley.getRequestQueue();


		StringRequest myReq = new StringRequest(aVideoUrl, getVideoUrlSuccessListener(), getVideoUrlReqErrorListener());

		myReq.setShouldCache(true);
		queue.add(myReq);
	}
	private static Response.Listener<String> getVideoUrlSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("VIDEO URLS", response);
				try {
					//Analytics.endTimedEvent("RECOMMENDATIONS-REQUEST");
					//Analytics.trackEvent("RECOMMENDATIONS-REQUEST-SUCCESS");

					CardResponseData minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
					String url=null;
					for(int i=0; i<minResultSet.results.size();i++)
					{
						int urlCount=minResultSet.results.get(i).videos.values.size();
						if(urlCount==1){
							url=minResultSet.results.get(0).videos.values.get(0).link;
						}
						else
						{
						for(int j=0;j<urlCount;j++)
						{
							String urlType=minResultSet.results.get(i).videos.values.get(j).type;
							if(urlType.equalsIgnoreCase(bitrateType))
							{
								url=minResultSet.results.get(i).videos.values.get(j).link;
							}
						}
						}
					}
					if (urlEventListener != null) {
						urlEventListener.urlReceived(true,url);
					}

				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
	private static Response.ErrorListener getVideoUrlReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (urlEventListener != null) {
					urlEventListener.urlReceived(false,null);
				}
			}
		};
	}
	public static void setUrlEventListener(MediaUtilEventListener aUrlEventListener) {
		urlEventListener = aUrlEventListener;
	}
}
