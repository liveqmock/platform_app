package com.apalya.myplex.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataVideos;
import com.apalya.myplex.data.CardDataVideosItem;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.utils.Util.KeyRenewListener;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MediaUtil {

	private static String TAG ="MediaUtil";
	
	public interface MediaUtilEventListener{
		public void urlReceived(boolean aStatus,String url, String message , String statusCode);
		public void lastPausedTimeFetched(int ellapseTime);
	}
	public static MediaUtilEventListener urlEventListener;
	private static String sQualityType;
	private static boolean sDownloadStatus;
	private static String sStreamingType;
	private static String sStreamFormat=ConsumerApi.STREAMINGFORMATHLS;
	private static Context mContext;
	private static String url,aContentId;

	public static void getVideoUrl(String aContentId,String bitRate,String streamingType, boolean isESTPackPurchased, String streamFormat){
		sQualityType=bitRate;
		sDownloadStatus=isESTPackPurchased;
		sStreamingType = streamingType;
		MediaUtil.aContentId = aContentId;
		url=ConsumerApi.getVideosDetail(aContentId);
		String url1 = ConsumerApi.getPlayerEventDetails(aContentId, "Pause");
		String location_params  = myplexapplication.locationUtil.getVideoUrlParams();
		if(location_params.length()>0){
			Log.d(TAG, location_params);
			url += location_params; 
		}
		if(!TextUtils.isEmpty(streamFormat)){
			sStreamFormat=streamFormat;
		}
		getContentUrlReq(url);
	}
	private static void getContentUrlReq(String aVideoUrl) {
		// TODO Auto-generated method stub
		RequestQueue queue = MyVolley.getRequestQueue();


		StringRequest myReq = new StringRequest(aVideoUrl, getVideoUrlSuccessListener(), getVideoUrlReqErrorListener());

		myReq.setShouldCache(false);
		queue.add(myReq);
		Log.d(TAG, aVideoUrl);
	}
	private static void sendResponse(boolean status,String url, String message, String statusCode){
		if (urlEventListener != null) {
			if(url != null){
				if(url.contains(".wvm")){
					url = url.replace("http:", "widevine:");	
				}
			}
			urlEventListener.urlReceived(status,url, message,statusCode);
		}
	}
	private static Response.Listener<String> getVideoUrlSuccessListener() {
		return new Response.Listener<String>() {
			private int elapsedTime ;

			@Override
			public void onResponse(String response) {				
				Log.d(TAG, response);		
				if(Util.isInvalidSession(mContext,response,new KeyRenewListener() {					
					@Override
					public void onKeyRenewed() {
						getContentUrlReq(ConsumerApi.getVideosDetail(aContentId));
					}					
					@Override
					public void onKeyRenewFailed(String message) {
						urlEventListener.urlReceived(false,null, null,null);
						Util.showToast(mContext, message, Util.TOAST_TYPE_INFO);
					}
				})){
					return;
				}
				CardResponseData minResultSet = null;
				try {
					
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
					sendResponse(false,null, null,null);
					return;
				}
				
				String url="";
				for(CardData data:minResultSet.results){
					if(data.videos == null || data.videos.values == null || data.videos.values.size() == 0)
					{
						if(data.videos != null && data.videos.status != null && !data.videos.status.equalsIgnoreCase("SUCCESS")){
							
							sendResponse(false,null, data.videos.message,data.videos.status);
							return;
						}
						
						sendResponse(false,null, null,null);
					}				
					
					
					for(CardDataVideosItem video:data.videos.values){
						//if(sDownloadStatus)
						{
							if(video.profile != null && video.profile.equalsIgnoreCase(sQualityType) && video.link != null && video.type.equalsIgnoreCase(ConsumerApi.STREAMDOWNLOAD)){								
								sendResponse(true,video.link,null,null);
								return;
							}
						}
						//else
						{
							if (video.profile != null
									&& video.profile.equalsIgnoreCase(sQualityType)
									&& video.link != null
									&& video.format != null
									&& (video.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHTTP) ||
											video.format.equalsIgnoreCase(sStreamFormat))
									&& video.type !=null
									&& video.type.equalsIgnoreCase(sStreamingType)) {
								Log.i(TAG, video.link);
								url=video.link;
								elapsedTime  = video.elapsedTime;
								//sendResponse(true, video.link);
								//return;
							}
						}
					}
				}
				if(url.length()>0)
				{
					sendResponse(true, url, null,null);
					urlEventListener.lastPausedTimeFetched(0);
					return;
				}
				sendResponse(false,null, null,null);
			}
		};
	}
	private static Response.ErrorListener getVideoUrlReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (urlEventListener != null) {
					if(error.networkResponse != null && error.networkResponse.data != null){
						String errorMsg = new String(error.networkResponse.data);
						Log.d(TAG, errorMsg);
					}
					urlEventListener.urlReceived(false,null, null,null);
				}
			}
		};
	}
	
	public static void setUrlEventListener(MediaUtilEventListener aUrlEventListener) {
		urlEventListener = aUrlEventListener;
	}
	/*
	 *  <p>This method will save the state of a media(like PLAY/PAUSE/STOP/RESUME).
	 *  This will update the data in the server and while fetching the media data the response will return
	 *  while the media was paused. 
	 *  </p>
	 */
	public static  boolean savePlayerState(String contentId , int state , final int elapsedTime)
	{
		if (state == PlayerListener.STATE_PAUSED) {
			if (elapsedTime!=0) {
				String pauseRequestUrl = ConsumerApi.setPlayerEventDetails(contentId, "Pause", elapsedTime);
				Log.d(TAG, pauseRequestUrl);
				RequestQueue queue = MyVolley.getRequestQueue();
				StringRequest pauseRequest = new StringRequest(Request.Method.POST, pauseRequestUrl,
						new Listener<String>() {
							@Override
							public void onResponse(String response) {
								Log.d(TAG,
										"successfull upadted in server with response"
												+ response.toString());
							}
						}, new ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Log.d(TAG, "error occured with response"
										+ error.getLocalizedMessage());
							}})
				{
					@Override
					protected Map<String, String> getParams()
							throws AuthFailureError {
						Map<String, String> params = new HashMap<String, String>();
						params.put("action", "Pause");
						params.put("elapsedTime", ""+elapsedTime);
						return params;
				}
			};
				pauseRequest.setShouldCache(false);
				queue.add(pauseRequest);
				return true;
			} else
				return false;
		} else {
			return false;
		}
	}
	public static void getPlayerState(String contentID ){
		String url = ConsumerApi.getPlayerEventDetails(contentID, "Pause");
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest request = new StringRequest(Request.Method.GET, url, ellapseTimeCallBack, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
			}
		});
		queue.add(request);
		
	}
	static Listener<String> ellapseTimeCallBack = new Listener<String>() 
			{
			private int ellapsedTime = 0;
			@Override
			public void onResponse(String response){
				Log.d(TAG, response);
				if(response!=null && response.length()>0){
					try {
						JSONObject jsonObject = new JSONObject(response);
						ellapsedTime  =  jsonObject.getInt("elapsedTime");
					} catch (JSONException e) {
						e.printStackTrace();
					}catch (Exception e) {
						e.printStackTrace();
					}
					Log.d(TAG, "Ellapsed time is "+ellapsedTime);					
				}
				
					urlEventListener.lastPausedTimeFetched(0);
//				ELLAPSE_TIME = ellapsedTime;
					Log.d(TAG,"here we need to fetch the last played status");
				
			}
		};
		public static void setContext(Context context){
			mContext = context;
		}	
}
