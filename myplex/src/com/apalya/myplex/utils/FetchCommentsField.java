package com.apalya.myplex.utils;

import java.io.IOException;

import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataComments;
import com.apalya.myplex.data.CardDataCommentsItem;
import com.apalya.myplex.data.CardDataUserReviews;
import com.apalya.myplex.data.CardDataUserReviewsItem;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.ValuesResponse;
import com.apalya.myplex.utils.FetchCardField.FetchComplete;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;

public class FetchCommentsField {

	private static final String TAG = "FetchCommentsField";
	private FetchComplete mListener;
	private String field;

	public void FetchCommentReview(CardData data, String field,int startIndex,
			FetchComplete listener) {
		if (data == null || field == null || listener == null) {
			return;
		}
		mListener = listener;
		this.field = field;
		String requestURl = new String();
		if (field.contains(ConsumerApi.FIELD_COMMENTS)) {
			requestURl = ConsumerApi.getComments(data._id, startIndex);
		} else if (field.contains(ConsumerApi.FIELD_USERREVIEWS)) {
			requestURl = ConsumerApi.getReviews(data._id, startIndex);
		} else {
			return;
		}
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(requestURl, successListener(),
				errorListener());
		myReg.setShouldCache(false);
		Log.d(TAG, "Min Request:" + requestURl);
		queue.add(myReg);
	}

	private Response.Listener<String> successListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {

				try {
					Log.d(TAG, "server response " + response);

					if (field.contains(ConsumerApi.FIELD_COMMENTS)) {
						ValuesResponse<CardDataCommentsItem> valuesResponse = (ValuesResponse<CardDataCommentsItem>) Util
								.fromJson(
										response,
										new TypeReference<ValuesResponse<CardDataCommentsItem>>() {
										});

						if (valuesResponse.code != 200) {
							if (mListener != null) {
								mListener.response(null);
							}
							return;
						}

						if (mListener != null) {
							CardResponseData cardResponseData = new CardResponseData();
							CardData cardData = new CardData();
							cardData.comments = new CardDataComments();
							cardData.comments.values = valuesResponse.results.values;
							cardResponseData.results.add(cardData);
							mListener.response(cardResponseData);

							return;
						}
					} else if (field.contains(ConsumerApi.FIELD_USERREVIEWS)) {
						ValuesResponse<CardDataUserReviewsItem> valuesResponse = (ValuesResponse<CardDataUserReviewsItem>) Util
								.fromJson(
										response,
										new TypeReference<ValuesResponse<CardDataUserReviewsItem>>() {
										});

						if (valuesResponse.code != 200) {
							if (mListener != null) {
								mListener.response(null);
							}
							return;
						}

						if (mListener != null) {
							CardResponseData cardResponseData = new CardResponseData();
							CardData cardData = new CardData();
							cardData.userReviews = new CardDataUserReviews();
							cardData.userReviews.values = valuesResponse.results.values;
							cardResponseData.results.add(cardData);
							mListener.response(cardResponseData);
							return;
						}
					}

					if (mListener != null) {
						mListener.response(null);
					}

				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
	}

	private Response.ErrorListener errorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.d(TAG, "Error from server " + error.networkResponse);
				if (mListener != null) {
					mListener.response(null);
				}
			}
		};
	}
}
