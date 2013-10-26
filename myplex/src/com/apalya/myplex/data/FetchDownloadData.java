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

public class FetchDownloadData implements CacheManagerCallback{
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
		CardDownloadedDataList downloadlist =  null;
		try {
			downloadlist = (CardDownloadedDataList) Util.loadObject(myplexapplication.getApplicationConfig().downloadCardsPath);	
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(downloadlist == null){
			if(mListener != null){
				mListener.completed();
			}
			return;
		}
		mSavedCount = downloadlist.mDownloadedList.size();
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
		Set<String> keySet = obj.keySet();
		for(String key:keySet){
			if(mBrowseData.mEntries.get(key) == null){
				mBrowseData.mEntries.put(key,obj.get(key));
				mBrowseData.mMasterEntries.add(obj.get(key));
			}
		}
		if(mBrowseData.mEntries.size() == mSavedCount){
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
		if(mListener != null){
			mListener.completed();
		}
	}
	@Override
	public void OnOnlineError(VolleyError error) {
		if(mListener != null){
			mListener.completed();
		}		
	}
	
}
