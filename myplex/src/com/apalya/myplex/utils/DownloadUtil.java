package com.apalya.myplex.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmStore;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataPurchaseItem;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardDownloadedDataList;
import com.apalya.myplex.data.myplexapplication;

public class DownloadUtil {

	public static final String TAG = "DownloadUtil";

	public void actionDownloadComplete(Context context, Intent intent) {

		long download_id = intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, -1);

		Log.d(TAG, "DownlaodId:" + download_id);

		CardDownloadedDataList downloadlist = null;

		try {

			myplexapplication.getApplicationConfig().downloadCardsPath = context
					.getFilesDir() + "/" + "downloadlist.bin";
			downloadlist = (CardDownloadedDataList) Util
					.loadObject(myplexapplication.getApplicationConfig().downloadCardsPath);
		} catch (Exception e) {
			return;
		}

		if(downloadlist == null){
			return;
		}
		HashMap<String, CardDownloadData> mDownloadedList = downloadlist.mDownloadedList;

		if (mDownloadedList == null || mDownloadedList.isEmpty()) {
			return;
		}

		CardDownloadData cardDownloadData = null;

		for (String contentId : mDownloadedList.keySet()) {

			cardDownloadData = mDownloadedList.get(contentId);

			if(cardDownloadData == null){
				continue;
			}
			
			if (cardDownloadData.mDownloadId == download_id) {
				
				Log.d(TAG, "download complete for content id :" + contentId);				

				if (ConsumerApi.DEBUGCLIENTKEY == null) {					
					
					String clientKey = SharedPrefUtils.getFromSharedPreference(
							context, context.getString(R.string.devclientkey));
					ConsumerApi.DEBUGCLIENTKEY = clientKey;
				}
				
				notifyDownloadComplete(contentId);
				showNotification(context,contentId,cardDownloadData);
				
				
				break;
			}
		}

	}

	public void notifyDownloadComplete(String contentId) {

		String url = ConsumerApi.getDownloadNotifyUrl(contentId);
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(Method.POST, url,
				onlineRequestSuccessListener(), onlineRequestErrorListener());
		myReg.setShouldCache(false);
		queue.add(myReg);
		Log.e(TAG, "requestUrl: " + url);

	}
	private void acquireRights(CardDownloadData cardDownloadData, Context context,String contentId, CardData cardData) {
		
		if(cardData != null && cardData.currentUserData != null &&
				cardData.currentUserData.purchase != null && !cardData.currentUserData.purchase.isEmpty()){
			
			CardDataPurchaseItem  purchaseitem = cardData.currentUserData.purchase.get(0);
			
			if(purchaseitem.type != null && purchaseitem.type.equalsIgnoreCase("Rental")){
				Log.e(TAG, "skiping acquireRights for DTR");
				return;
			}
		}
		
		String url="file://"+cardDownloadData.mDownloadPath;
		WidevineDrm drmManager = new WidevineDrm(context);
		drmManager.registerPortal(WidevineDrm.Settings.PORTAL_NAME);
		int rightStatus= drmManager.checkRightsStatus(url);
		if(rightStatus!=DrmStore.RightsStatus.RIGHTS_VALID)
		{
			int status=drmManager.acquireRights(url);
			if(status == DrmStore.RightsStatus.RIGHTS_VALID)
			{				
				Util.showToast(context, "Rights Installed", Util.TOAST_TYPE_INFO);
			}else {
				Util.showToast(context, "accquired rights failed", Util.TOAST_TYPE_INFO);
			}
		}		
		
	}

	public  void showNotification(final Context context,final String id, final CardDownloadData cardDownloadData) {
		List<CardData> list =  new ArrayList<CardData>();
		CardData data = new CardData();
		data._id = id;
		list.add(data);		
		
		if(myplexapplication.getApplicationConfig().indexFilePath == null){
			File internalPath = context.getFilesDir();		
			//Replace internalPath with appDirectory to store in memory card.
			//Remember to add WRITE_EXTERNAL_STORAGE permission in Manifest file
			myplexapplication.getApplicationConfig().indexFilePath = ""+internalPath;
		}

		
		CacheManager cacheManager = new CacheManager();
		cacheManager.getCardDetails(list,IndexHandler.OperationType.IDSEARCH, new CacheManagerCallback() {			
			@Override
			public void OnOnlineResults(List<CardData> dataList) {
				
			}
			
			@Override
			public void OnOnlineError(VolleyError error) {
			}
			
			@Override
			public void OnCacheResults(HashMap<String, CardData> obj,boolean issuedRequest) 
			{
				if(obj!= null && obj.containsKey(id)){
					CardData cardDatas = (CardData) obj.get(id);
					String title   = cardDatas.generalInfo.title;
					Log.d(TAG,"title ="+title);
					acquireRights(cardDownloadData,context,id, cardDatas);
					Util.showNotification(context, title);		
				}
			}
		});
	}
	

	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.e(TAG, "response: " + response);

				} catch (Exception e) {
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
				if (error != null && error.networkResponse != null) {
					Log.e(TAG, "$$$  " + error.networkResponse.statusCode);
				}

			}
		};
	}
}
