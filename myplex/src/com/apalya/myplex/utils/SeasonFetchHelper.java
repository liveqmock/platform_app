package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GZipRequest;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;

public class SeasonFetchHelper implements CacheManagerCallback {

	private CardData rootCard;
	private static String TAG = SeasonFetchHelper.class.getSimpleName();
	private List<CardData> cardDatas = new ArrayList<CardData>();
	private ShowFetchListener showFetchListener;

	public SeasonFetchHelper(CardData cardData, ShowFetchListener listener) {
		rootCard = cardData;
		showFetchListener = listener;
	}

	public void fetchSeason() {
		
		String seasonRequestUrl = "";
		seasonRequestUrl = ConsumerApi.getTVShowSeasonListUrl(rootCard._id);
		Log.d(TAG, "seasonRequestUrl=" + seasonRequestUrl);
		if (seasonRequestUrl == null)
			return;
		GZipRequest seasonRequest = new GZipRequest(seasonRequestUrl,
				new SeasonFetched(), new SeasonFetchFailed());
//		seasonRequest.setShouldCache(false);
		RequestQueue queue = MyVolley.getRequestQueue();
		queue.add(seasonRequest);
		
	}

	private class SeasonFetched implements Listener<CardResponseData> {
		private CacheManager mCacheManager;

		@Override
		public void onResponse(CardResponseData responseData) {
			if (responseData == null) {
				return;
			}
			if ((responseData.code != 200)
					|| (responseData.message.equalsIgnoreCase("SUCCESS"))) {
				return;
			}
			List<CardData> datas = responseData.results;
			if (datas == null || datas.size() == 0) {
				return;
			}
			mCacheManager = new CacheManager();
			mCacheManager
					.getCardDetails(datas, IndexHandler.OperationType.IDSEARCH,
							SeasonFetchHelper.this);

		}
	}

	private class SeasonFetchFailed implements Response.ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.d(TAG, "error occured" + error.getMessage());
			if (showFetchListener != null)
				showFetchListener.onFailed(error);
		}

	}

	@Override
	public void OnCacheResults(HashMap<String, CardData> cardsMap,
			boolean issuedRequest) {

		if (cardsMap == null) {
			return;
		}
		// Iterator<Entry<String, CardData>> it =
		// cardsMap.entrySet().iterator();
		for (CardData data : cardsMap.values()) {
			cardDatas.add(data);
		}
		if (cardDatas == null || cardDatas.size() == 0)
			return;
		if (showFetchListener != null)
			showFetchListener.onSeasonDataFetched(cardDatas);

		fetchEpisodes(cardDatas.get(0));
	}

	@Override
	public void OnOnlineResults(List<CardData> dataList) {

		if (showFetchListener != null)
			showFetchListener.onSeasonDataFetched(dataList);

		for (CardData data : dataList) {
			cardDatas.add(data);
			Log.d(TAG, data._id);
		}
		fetchEpisodes(dataList.get(0));
	}

	@Override
	public void OnOnlineError(VolleyError error) {
		if (showFetchListener != null)
			showFetchListener.onFailed(error);
	}

	/**
	 * @param season
	 */
	public void fetchEpisodes(CardData season) {
		String _id = season._id;
		String episodeRequestUrl = ConsumerApi.getEpisodesUrl(_id);
		Log.d(TAG, "episodeRequestUrl=" + episodeRequestUrl);
		if (episodeRequestUrl == null)
			return;
		GZipRequest seasonRequest = new GZipRequest(episodeRequestUrl,
				new EpisodeFetched(season), new EpisodeFetchFailed());
		RequestQueue queue = MyVolley.getRequestQueue();
		queue.add(seasonRequest);

	}

	private class EpisodeFetched implements Listener<CardResponseData> {
		private CardData season;
		public EpisodeFetched(CardData season) {
			this.season = season;
		}
		
		@Override
		public void onResponse(CardResponseData responseData) {
			if (responseData == null) {
				return;
			}
			if ((responseData.code != 200)
					|| (responseData.message.equalsIgnoreCase("SUCCESS"))) {
				return;
			}
			List<CardData> datas = responseData.results;
			if (datas == null || datas.size() == 0) {
				return;
			}

			if (showFetchListener != null)
				showFetchListener.onEpisodeFetched(season, datas);
			Log.d(TAG, "got episodes" + responseData.results.size());
		}
	}

	private class EpisodeFetchFailed implements Response.ErrorListener {
		@Override
		public void onErrorResponse(VolleyError error) {
			Log.d(TAG, "error occured" + error.getMessage());
			if (showFetchListener != null)
				showFetchListener.onFailed(error);
		}

	}

	public interface ShowFetchListener {
		public void onSeasonDataFetched(List<CardData> seasons);

		public void onEpisodeFetched(CardData season, List<CardData> episodes);

		public void onFailed(VolleyError error);
	}

	public void cancelAllRequests() {
		
	}
}
