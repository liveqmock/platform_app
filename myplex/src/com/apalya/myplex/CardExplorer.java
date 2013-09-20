package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apalya.myplex.adapters.CardActionListener;
import com.apalya.myplex.adapters.CardGoogleLayoutAdapater;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.listboxanimation.AlphaInAnimationAdapter;
import com.apalya.myplex.listboxanimation.OnDismissCallback;
import com.apalya.myplex.listboxanimation.ScaleInAnimationAdapter;
import com.apalya.myplex.listboxanimation.SwingBottomInAnimationAdapter;
import com.apalya.myplex.listboxanimation.SwingLeftInAnimationAdapter;
import com.apalya.myplex.listboxanimation.SwingRightInAnimationAdapter;
import com.apalya.myplex.listboxanimation.SwipeDismissAdapter;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.views.CardView;

public class CardExplorer extends BaseFragment implements CardActionListener,
		OnDismissCallback {
	private CardView mCardView;
	private ListView mGoogleCardListView;
	private CardGoogleLayoutAdapater mGoogleCardListViewAdapter;
	private static final int RESULTS_PAGE_SIZE = 20;
	private boolean mHasData = false;
	private boolean mInError = false;
	private int mStartIndex = 0;
	private ArrayList<CardData> mEntries = new ArrayList<CardData>();
	private ArrayList<CardData> mMasterEntries = new ArrayList<CardData>();
	private int displayMode = STACKVIEW;
	static final int STACKVIEW = 1;
	static final int GOOGLECARDVIEW = 2;
	private View mRootView;
	private ProgressDialog mProgressDialog = null;
	
	public void setDisplayMode(int mode) {
		this.displayMode = mode;
	}

	@Override
	public void play(CardData object) {
		BaseFragment fragment = mMainActivity.createFragment(MainActivity.CARDDETAILS);
		fragment.setDataObject(object);
		mMainActivity.bringFragment(fragment);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	public void showProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(getContext(),"", "Loading...", true,false);
	}
	public void dismissProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
	}
	public void updateText(String str){
		if(mProgressDialog != null){
			mProgressDialog.setTitle(str);
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(mRootView != null){
			show();
			return mRootView;
		}
		mRootView = inflater.inflate(R.layout.cardbrowsing, container,false);
		mCardView = (CardView) mRootView.findViewById(R.id.framelayout);
		mGoogleCardListView = (ListView) mRootView
				.findViewById(R.id.cardlistview);
		mGoogleCardListViewAdapter = new CardGoogleLayoutAdapater(getContext());
		// mGoogleCardListView.setAdapter(mGoogleCardListViewAdapter);
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
				new SwipeDismissAdapter(mGoogleCardListViewAdapter, this));
		swingBottomInAnimationAdapter.setAbsListView(mGoogleCardListView);

		mGoogleCardListView.setAdapter(swingBottomInAnimationAdapter);
		mCardView.setVisibility(View.VISIBLE);
		mCardView.setVerticalScrollBarEnabled(false);
		mCardView.setHorizontalScrollBarEnabled(false);
		mCardView.setContext(getContext());
		mCardView.setActionBarHeight(getActionBar().getHeight());
		mCardView.setCardActionListener(this);
		mGoogleCardListViewAdapter.setCardActionListener(this);
		setDisplayMode(displayMode);
		mMainActivity.setTitle("Home");
		showProgressBar();
		loadPage();
		return mRootView;
	}

	public void setSelectedAnimation(int index){
		switch (index) {
		case 0:
			SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
					new SwipeDismissAdapter(mGoogleCardListViewAdapter, this));
			swingBottomInAnimationAdapter.setAbsListView(mGoogleCardListView);

			mGoogleCardListView.setAdapter(swingBottomInAnimationAdapter);
			break;
		case 1:
			SwingRightInAnimationAdapter swingRightinAnimationAdapter = new SwingRightInAnimationAdapter(
					new SwipeDismissAdapter(mGoogleCardListViewAdapter, this));
			swingRightinAnimationAdapter.setAbsListView(mGoogleCardListView);

			mGoogleCardListView.setAdapter(swingRightinAnimationAdapter);
			break;
		case 2:
			SwingLeftInAnimationAdapter swingLeftinAnimationAdapter = new SwingLeftInAnimationAdapter(
					new SwipeDismissAdapter(mGoogleCardListViewAdapter, this));
			swingLeftinAnimationAdapter.setAbsListView(mGoogleCardListView);

			mGoogleCardListView.setAdapter(swingLeftinAnimationAdapter);
			break;
		case 3:
			ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(
					new SwipeDismissAdapter(mGoogleCardListViewAdapter, this));
			scaleInAnimationAdapter.setAbsListView(mGoogleCardListView);

			mGoogleCardListView.setAdapter(scaleInAnimationAdapter);
			break;
		case 4:
			AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(
					new SwipeDismissAdapter(mGoogleCardListViewAdapter, this));
			alphaInAnimationAdapter.setAbsListView(mGoogleCardListView);

			mGoogleCardListView.setAdapter(alphaInAnimationAdapter);
			break;
		}
	}
	@Override
	public void setActionBarHeight(int height) {
		super.setActionBarHeight(height);
		mCardView.setActionBarHeight(height);
	}
	private void show(){
		if (displayMode == STACKVIEW) {
			mGoogleCardListView.setVisibility(View.GONE);
			mCardView.setVisibility(View.VISIBLE);
			mMainActivity.setTitle("Home");
		} else {
			mGoogleCardListView.setVisibility(View.VISIBLE);
			mCardView.setVisibility(View.GONE);
			mMainActivity.setTitle("Home Tablet");
		}
		applyData();
	}
	@Override
	public void onResume() {
		super.onResume();
		show();
	}

	@Override
	public void loadmore(int value) {
		mStartIndex = value;
		loadPage();
	}

	/*
	 * https://picasaweb.google.com/data/feed/api/all?q=movie&max-results=" +
	 * RESULTS_PAGE_SIZE + "&thumbsize=160&alt=json" + "&start-index="
	 */
	private void loadPage() {
		showActionBarProgress();
		RequestQueue queue = MyVolley.getRequestQueue();

		JsonObjectRequest myReq = new JsonObjectRequest(Method.GET,
				"http://dev.myplex.in/content/v2/search/?startIndex="
						+ mStartIndex, null, createMyReqSuccessListener(),
				createMyReqErrorListener());

		myReq.setShouldCache(true);
		Log.d("pref", "Request results for "
				+ "http://dev.myplex.in/content/v2/search/?startIndex="
				+ mStartIndex);
		queue.add(myReq);
	}

	private Response.Listener<JSONObject> createMyReqSuccessListener() {
		return new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject feed = response.getJSONObject("results");
					JSONArray entries = feed.getJSONArray("values");
					JSONObject entry;
//					mEntries.clear();
					mEntries = new ArrayList<CardData>();
					for (int i = 0; i < entries.length(); i++) {
						CardData data = new CardData("", "", 0);

						entry = entries.getJSONObject(i);
						JSONObject content = entry.getJSONObject("content");
						data.title = content.getString("title");
						data.filterName = content.getString("language");		
						JSONObject images = entry.getJSONObject("images");
						JSONArray thumbnail = images.getJSONArray("cover");
						if (thumbnail.length() > 0) {
							data.imageUrl = thumbnail.getJSONObject(0)
									.getString("link");
						}
						mEntries.add(data);
						mMasterEntries.add(data);
					}
					applyData();
					Log.d("pref", "Request found " + mEntries.size());
				} catch (JSONException e) {
					e.printStackTrace();
					// showErrorDialog();
				}
			}
		};
	}
	private void prepareFilterData(){
		List<FilterMenudata> filteroptions = new ArrayList<FilterMenudata>();
		List<String> tempList = new ArrayList<String>();
		filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, "ALL", 1));
		for(CardData data:mMasterEntries){
			if(data.filterName != null && !tempList.contains(data.filterName)){
				filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, data.filterName, 1));
				tempList.add(data.filterName);
			}
		}
		mMainActivity.addFilterData(filteroptions,mFilterMenuClickListener);
	}
	private OnClickListener mFilterMenuClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v.getTag() instanceof FilterMenudata){
				String label = ((FilterMenudata)v.getTag()).label;
				if(label != null && label.equalsIgnoreCase("ALL")){
					sort(mMasterEntries);
					return;
				}
				ArrayList<CardData> localData = new ArrayList<CardData>();
				for(CardData data:mEntries){
					if(data.filterName != null && data.filterName.equalsIgnoreCase(label)){
						localData.add(data);
					}
				}
				sort(localData);
			}
		}
	};
	private void sort(ArrayList<CardData> localData){
		if (displayMode == STACKVIEW) {
			mCardView.forceUpdateData(localData);
		} else {
			mGoogleCardListViewAdapter.forceUpdateData(localData);
		}
	}
	private void applyData() {
		if (displayMode == STACKVIEW) {
			mCardView.addData(mMasterEntries);
			mCardView.show();
		} else {
			mGoogleCardListViewAdapter.setData(mMasterEntries);
		}
		prepareFilterData();
		dismissProgressBar();
		hideActionBarProgress();
	}

	private void showErrorDialog() {
		mInError = true;
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setMessage("No Response from server");
		b.show();
	}

	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				 showErrorDialog();
				hideActionBarProgress();
				dismissProgressBar();
			}
		};
	}
	
	private CardData mCardData;

	@Override
	public void addFavourite(CardData data) {
		mCardData = data;
		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				mCardData.applyFavoriteInProgress = false;
				mCardData.isFavorite = true;
				Handler h = new Handler(Looper.getMainLooper());
				h.post(new Runnable() {

					@Override
					public void run() {
						mCardView.updateData(mCardData);
					}
				});
			}
		}, 1000);
	}

	@Override
	public void removeFavourite(CardData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectedCard(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deletedCard(CardData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moreInfo(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void purchase(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		// TODO Auto-generated method stub

	}
}
