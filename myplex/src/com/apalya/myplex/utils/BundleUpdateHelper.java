package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.cache.InsertionResult;
import com.apalya.myplex.cache.SearchResult;
import com.apalya.myplex.data.Bundle;
import com.apalya.myplex.data.BundleContent;
import com.apalya.myplex.data.BundleData;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;

public class BundleUpdateHelper {

	private String requestUrl;
	private static String TAG = BundleUpdateHelper.class.getName();
	private List<String> contentIds = new ArrayList<String>();
	private List<CardData> dataToSave = new ArrayList<CardData>();
	private InsertionResult insertionResult;
	private CardData cardData;

	public BundleUpdateHelper(String url, List<CardData> dataToSave) {
		requestUrl = url;
		this.dataToSave = dataToSave;
		cardData = dataToSave.get(0);
	}

	public void getConnectIds(InsertionResult insertionResult) {
		this.insertionResult = insertionResult;
		StringRequest mStringRequest = new StringRequest(requestUrl,
				new CurrentUserDataSuccessListener(),
				new CurrentUserDataErrorListener());
		RequestQueue queue = MyVolley.getRequestQueue();
		mStringRequest.setShouldCache(false);
		queue.add(mStringRequest);

	}

	private class CurrentUserDataSuccessListener implements Listener<String> {
		@Override
		public void onResponse(String response) {
			Log.d(TAG, "got response ->" + response);
			if (response == null) {
				updatecurrentUserData();
				return;
			}
			try {
				Bundle bundle = (Bundle) Util.fromJson(response, Bundle.class);
				if ((bundle == null))
					return;
				if ((bundle.code != 200)
						|| (!bundle.status.equalsIgnoreCase("SUCCESS"))) {
					return;
				}
				List<BundleData> datas = bundle.results.values;
				if (datas == null && datas.size() == 0) {
					return;
				}
				for (BundleData data : datas) {
					List<BundleContent> contents = data.contents;
					if (contents != null && contents.size() > 0) {
						for (BundleContent content : contents) {
							if (!content.contentId
									.equalsIgnoreCase(cardData._id)) {
								String contentId = content.contentId;
								contentIds.add(contentId);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				updatecurrentUserData();
			}
		}
	}

	public void updatecurrentUserData() {

		List<CardData> searchList = new ArrayList<CardData>();

		for (String contentId : contentIds) {
			Log.d(TAG, "content id " + contentId);
			CardData newContent = new CardData();
			newContent._id = contentId;
			searchList.add(newContent);
		}

		if (searchList.isEmpty()) {
			myplexapplication.getCacheHolder().UpdataDataAsync(dataToSave,
					insertionResult);
			return;
		}

		myplexapplication.getCacheHolder().GetData(searchList,
				IndexHandler.OperationType.IDSEARCH, new SearchResult() {
					@Override
					public void searchComplete(
							HashMap<String, CardData> resultMap) {
						if (resultMap == null) {
							return;
						}

						for (String _id : contentIds) {
							if (resultMap.containsKey(_id)) {
								CardData card = resultMap.get(_id);
								card.currentUserData = cardData.currentUserData;
								dataToSave.add(card);
							}
						}
						Set<String> keySet = resultMap.keySet();
						Log.d(TAG,
								"Number of result found in cache :"
										+ keySet.size());
						if (dataToSave != null && dataToSave.size() > 0)
							myplexapplication.getCacheHolder().UpdataDataAsync(
									dataToSave, insertionResult);

					}
				});

	}

	private class CurrentUserDataErrorListener implements ErrorListener {

		@Override
		public void onErrorResponse(VolleyError error) {

		}

	}
}
