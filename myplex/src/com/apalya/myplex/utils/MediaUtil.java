package com.apalya.myplex.utils;

import java.io.IOException;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataVideos;
import com.apalya.myplex.data.CardDataVideosItem;
import com.apalya.myplex.data.CardResponseData;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MediaUtil {

	public interface MediaUtilEventListener{
		public void urlReceived(boolean aStatus,String url);
	}
	public static MediaUtilEventListener urlEventListener;
	private static String qualityType;
	private static boolean downloadStatus;

	public static void getVideoUrl(String aContentId,String bitRate,boolean isESTPackPurchased){
		qualityType=bitRate;
		downloadStatus=isESTPackPurchased;
		String url=ConsumerApi.getVideosDetail(aContentId);
		getContentUrlReq(url);
	}
	private static void getContentUrlReq(String aVideoUrl) {
		// TODO Auto-generated method stub
		RequestQueue queue = MyVolley.getRequestQueue();


		StringRequest myReq = new StringRequest(aVideoUrl, getVideoUrlSuccessListener(), getVideoUrlReqErrorListener());

		myReq.setShouldCache(false);
		queue.add(myReq);
	}
	private static void sendResponse(boolean status,String url){
		if (urlEventListener != null) {
			urlEventListener.urlReceived(status,url);
		}
	}
	private static Response.Listener<String> getVideoUrlSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("VIDEO URLS", response);
				CardResponseData minResultSet = null;
				try {
					//Analytics.endTimedEvent("RECOMMENDATIONS-REQUEST");
					//Analytics.trackEvent("RECOMMENDATIONS-REQUEST-SUCCESS");
					minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
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
				if(minResultSet.results == null){
					sendResponse(false,null);
				}
				for(CardData data:minResultSet.results){
					if(data.videos == null || data.videos.values == null || data.videos.values.size() == 0){sendResponse(false,null);}
					for(CardDataVideosItem video:data.videos.values){
						if(downloadStatus)
						{
							if(video.profile != null && video.profile.equalsIgnoreCase(qualityType) && video.link != null && video.type.equalsIgnoreCase("download")){
								sendResponse(true,video.link);
								return;
							}
						}
						else
						{
							if(video.profile != null && video.profile.equalsIgnoreCase(qualityType) && video.link != null){
								sendResponse(true,video.link);
								return;
							}
						}
					}
				}
				sendResponse(false,null);
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
