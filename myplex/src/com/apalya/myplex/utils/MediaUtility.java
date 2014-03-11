package com.apalya.myplex.utils;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataVideos;
import com.apalya.myplex.data.CardDataVideosItem;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Util.KeyRenewListener;

public class MediaUtility {
	private Context mContext;
	private String apiUrl;
	private String contentID;
	private String TAG = getClass().getSimpleName();
	private VideoUrlFetchListener listener;
	private boolean isTrailer = false;

	public MediaUtility(Context context, VideoUrlFetchListener listener,boolean isTrailer) {
		mContext = context;
		this.listener = listener;
		this.isTrailer = isTrailer;
	}

	public void fetchVideoUrl(String contentID) {
		this.contentID = contentID;
		apiUrl = ConsumerApi.getVideosDetail(contentID);
		Log.d(TAG,"Url  ="+apiUrl);
		String location_params = myplexapplication.locationUtil.getVideoUrlParams();
		if (location_params.length() > 0) {
			apiUrl += location_params;
		}
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReq = new StringRequest(apiUrl, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				if(Util.isInvalidSession(mContext,response,new KeyRenewListener() {					
					@Override
					public void onKeyRenewed() {
						fetchVideoUrl(MediaUtility.this.contentID);
					}					
					@Override
					public void onKeyRenewFailed(String message) {
						Util.showToast(mContext, message, Util.TOAST_TYPE_INFO);
					}
				})){
					return;
				}
					CardResponseData cardRespDatas = null;
					try {
						cardRespDatas = (CardResponseData) Util.fromJson(response, CardResponseData.class);
					}catch (Exception e) {
						listener.onUrlFetchFailed(mContext.getString(R.string.canot_fetch_url));
						return;
					}
					
					for(CardData data : cardRespDatas.results){
						if(data.videos == null || data.videos.values == null || data.videos.values.size() == 0)
						{
							if(data.videos != null && data.videos.status != null && !data.videos.status.equalsIgnoreCase("SUCCESS")){								
								if(data.videos.message != null && data.videos.status.equalsIgnoreCase("ERR_USER_NOT_SUBSCRIBED")){
										listener.onUrlFetchFailed(data.videos.status);
								}
								
								listener.onUrlFetchFailed(data.videos.message);
								return;
							}							
							listener.onUrlFetchFailed(mContext.getString(R.string.canot_fetch_url));
						}	
						CardDataVideos videos = data.videos;
						if((!videos.message.equalsIgnoreCase("SUCCESS")) || (videos.values!=null) || (videos.values.size()>0)){
							//Analytics.startVideoTime();
							if(isTrailer)	
								listener.onTrailerUrlFetched(videos.values);
							else
								listener.onUrlFetched(videos.values);
							return;
						}else{
							listener.onUrlFetchFailed(mContext.getString(R.string.canot_fetch_url));
						}						
					}
					
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if(error.networkResponse != null && error.networkResponse.data != null){
					String errorMsg = new String(error.networkResponse.data);
					listener.onUrlFetchFailed(errorMsg);
				}
			}
		});

		myReq.setShouldCache(false);
		queue.add(myReq);
	}

	protected boolean isSessionValid(String response) {
		return Util.isInvalidSession(mContext, response,
				new KeyRenewListener() {
			@Override
			public void onKeyRenewed() {
				fetchVideoUrl(contentID);
			}

			@Override
			public void onKeyRenewFailed(String message) {
				Util.showToast(mContext, message, Util.TOAST_TYPE_INFO);
			}
		});
	}

	public interface VideoUrlFetchListener {
		void onUrlFetched(List<CardDataVideosItem> videos);
		void onTrailerUrlFetched(List<CardDataVideosItem> videos);
		void onUrlFetchFailed(String message);
	}
}
