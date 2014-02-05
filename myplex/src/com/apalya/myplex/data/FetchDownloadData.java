package com.apalya.myplex.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.android.volley.VolleyError;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.utils.Util;

import android.content.Context;
import android.util.Log;

public class FetchDownloadData implements CacheManagerCallback{
	public static final String TAG = "FetchDownloadData";
	private Context mContext;
	private FetchDownloadDataListener mListener;
	private CacheManager mCacheManager = new CacheManager();
	private CardExplorerData mBrowseData;
	private int mSavedCount = 0 ;
	public interface FetchDownloadDataListener{
		public void completed();
	}
	public FetchDownloadData(Context cxt) {
		this.mContext = cxt;
	}
	public void setListener(FetchDownloadDataListener listener){
		this.mListener = listener;
	}
	public void fetchDownload(FetchDownloadDataListener listener){
		mListener = listener;
		mSavedCount = 0;
		mBrowseData = myplexapplication.getCardExplorerData();
		mBrowseData.reset();
		mBrowseData.requestType = CardExplorerData.REQUEST_DOWNLOADS;
		CardDownloadedDataList downloadlist =  null;
		try {
			downloadlist = (CardDownloadedDataList) Util.loadObject(myplexapplication.getApplicationConfig().downloadCardsPath);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(downloadlist == null){
			if(mListener != null){
				mListener.completed();
			}
			Log.d(TAG,"user has not requested for a download yet");
			return;
		}
		mSavedCount = downloadlist.mDownloadedList.size();
		Log.d(TAG,"user has requested for a "+mSavedCount+" downloads");
		List<CardData> downloadCards = new ArrayList<CardData>();
		Set<String> keySet = downloadlist.mDownloadedList.keySet();
		for(String key:keySet){
			CardData data = new CardData();
			data._id = key;
			downloadCards.add(data);
		}
		mCacheManager.getCardDetails(downloadCards,IndexHandler.OperationType.IDSEARCH,FetchDownloadData.this);
		
	}
	@Override
	public void OnCacheResults(HashMap<String, CardData> obj,boolean issuedRequest) {
		Log.d(TAG,"reading form cache ");
		Set<String> keySet = obj.keySet();
		for(String key:keySet){
			if(mBrowseData.mEntries.get(key) == null){
				mBrowseData.mEntries.put(key,obj.get(key));
				mBrowseData.mMasterEntries.add(obj.get(key));
			}
		}
		if(mBrowseData.mEntries.size() == mSavedCount){
			Log.d(TAG,"got the complete list form cache ");
			if(mListener != null){
				mListener.completed();
			}
		}
	}
	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		if(dataList == null){return;}
		
		for(CardData data:dataList){
			if(mBrowseData.mEntries.get(data._id) == null){
				mBrowseData.mEntries.put(data._id,data);
				mBrowseData.mMasterEntries.add(data);
			}
		}		
		Log.d(TAG,"got the complete list form online ");
		if(mListener != null){
			mListener.completed();
		}
	}
	@Override
	public void OnOnlineError(VolleyError error) {
		Log.d(TAG,"problem while reading form cache ");
		if(mListener != null){
			mListener.completed();
		}		
	}
	
}
