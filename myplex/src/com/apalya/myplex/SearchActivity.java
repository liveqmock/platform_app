package com.apalya.myplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.adapters.OpenListener.OpenCallBackListener;
import com.apalya.myplex.adapters.SearchListAdapter;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.SearchData;
import com.apalya.myplex.data.SearchData.ButtonData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.FlowLayout;
import com.apalya.myplex.views.PinnedSectionListView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class SearchActivity extends BaseFragment implements
		OpenCallBackListener, CacheManagerCallback {

	EditText mSearchInput;
	ImageView mSearchLens; 
	private PinnedSectionListView mPinnedListView;
	private float mButtonApha = 0.25f;
	private SearchListAdapter mAdapter;
	private View rootView;
	List<SearchData> mListData = null;
	List<SearchData> mMasterListData = null;
	List<ButtonData> mSearchbleTags = null;
	HashMap<String, Integer> mUniqueCategories = new HashMap<String, Integer>();
	private String allCategories = "ALL";
	private ProgressDialog mProgressDialog = null;
	private CacheManager mCacheManager = new CacheManager();
	public static final String TAG = "SearchActivity";
	//private EasyTracker easyTracker = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.mixPanelDiscoveryOptionSelected();
		Analytics.createScreenGA(Analytics.SCREEN_DISCOVER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(isVisible()){
			mMainActivity.addFilterData(new ArrayList<FilterMenudata>(), mFilterMenuClickListener);
		}
		rootView = inflater.inflate(R.layout.searchlayout, container, false);
		mSearchInput = (EditText) rootView.findViewById(R.id.inputSearch);
		mSearchLens = (ImageView)rootView.findViewById(R.id.searchlens);
		mSearchLens.setOnClickListener(mSearchClickListener);
		Util.showFeedback(mSearchLens);
		mSearchInput.setTypeface(FontUtil.Roboto_Regular);
		mMainActivity.setSearchBarVisibilty(View.INVISIBLE);
		mMainActivity.setSearchViewVisibilty(View.INVISIBLE);
		mSearchInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String userSearchText = mSearchInput.getText().toString();
					if (userSearchText.length() <= 0)
						return true;

					String searchText = mSearchInput.getText().toString();
					FillEditText(searchText);
					mSearchInput.setText("");
					searchText = "";
				}
				return false;
			}

		});
		final ImageView clearButton = (ImageView) rootView
				.findViewById(R.id.clearSearchresults);
		Util.showFeedback(clearButton);
		clearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final FlowLayout spannablelayout = (FlowLayout) rootView.findViewById(R.id.spannable);
				spannablelayout.removeAllViews();
				ClearSearchTags();
				clearButton.setVisibility(View.GONE);
				if (mListData != null) {
					for (int count = 0; count < mListData.size(); count++) {
						if (mListData.get(count) == null
								|| mListData.get(count).getSearchTags() == null)
							continue;
						int tagcount = mListData.get(count).getSearchTags()
								.size();
						for (int tags = 0; tags < tagcount; tags++) {
							if (mListData.get(count).getSearchTags().get(tags) != null)
								mListData.get(count).getSearchTags().get(tags)
										.setCLicked(false);
						}
					}

					mAdapter.notifyDataSetChanged();
				}
				mMainActivity.setSearchBarVisibilty(View.INVISIBLE);
			}
		});
		// ArrayList<SearchData> mSearchData = fillSearchData();
		try {
			mPinnedListView = (PinnedSectionListView) rootView
					.findViewById(R.id.list_view);
			mListData = new ArrayList<SearchData>();
			mSearchbleTags = new ArrayList<SearchData.ButtonData>();
			mAdapter = new SearchListAdapter(getContext(), mListData);
			mAdapter.setOpenListener(SearchActivity.this);
			mPinnedListView.setAdapter(mAdapter);

		} catch (Exception e) {
			e.printStackTrace();
		}
//		mMainActivity.setTitle("Search");

		loadSearchTags();
		
		return rootView;
	}

	private String mSearchQuery = new String();

	private OnClickListener mSearchClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			searchButtonClicked();
		}
	};
	@Override
	public void searchButtonClicked() {

		if (mSearchbleTags == null || mSearchbleTags.size() <= 0)
		{
			Util.showToast(mContext, "Select search tags or enter text for search ",Util.TOAST_TYPE_INFO);
//			Toast.makeText(mContext,  "Select search tags or enter text for search ",  Toast.LENGTH_LONG).show();
			return;
		}
		/////////AddAnalytics
		mMainActivity.showActionBarProgressBar();

		String searchQuery = new String();
		final List<CardData> searchString = new ArrayList<CardData>();
		for (ButtonData data : mSearchbleTags) {
			CardData temp = new CardData();
			// temp._id = data.getButtonId() != null ? data.getButtonId() :
			// data.getButtonName();
			temp._id = data.getButtonName();
			if (searchQuery.length() > 0) {
				searchQuery += ",";
			}
			searchQuery += data.getButtonName();
			searchString.add(temp);
			
		}
		
		//mixPanelSearchButtonClicked(searchQuery);
		Analytics.mixPanelDiscoverySearchButtonClicked(searchQuery,mSearchbleTags);
		mSearchQuery = searchQuery;
		mMainActivity.setActionBarTitle(searchQuery);
		IndexHandler.OperationType searchType = IndexHandler.OperationType.DONTSEARCHDB;
		if(!Util.isNetworkAvailable(mContext))
			searchType = IndexHandler.OperationType.FTSEARCH;
		mCacheManager.getCardDetails(searchString, searchType, SearchActivity.this);
	}
	
	// Boolean true to add, false to remove
	private void UpdateSearchTags(ButtonData tagData, Boolean addorremove) {
		if (mSearchbleTags == null)
			return;
		Map<String,String> params=new HashMap<String, String>();
		if (addorremove)
		{
			mSearchbleTags.add(tagData);
			
			params.put(Analytics.DISCOVER_KEYWORD, tagData.getButtonName());
			
		}
		else
		{
			//params.put("tagRemoved", tagData.getButtonName()); 
			mSearchbleTags.remove(tagData);
		}
		
		if (mSearchbleTags.size() == 0)
			mMainActivity.setSearchBarVisibilty(View.INVISIBLE);
		else
			mMainActivity.setSearchBarVisibilty(View.VISIBLE);
	}

	private void ClearSearchTags() {
		if (mSearchbleTags != null)
			mSearchbleTags.clear();
		
	}

	public List<ButtonData> GetSearchTags() {
		return mSearchbleTags;
	}

	private void loadSearchTags() {
		mListData.clear(); 
		mMainActivity.showActionBarProgressBar();
		showProgressBar();
		TextView empty = (TextView) rootView.findViewById(R.id.empty);
		empty.setVisibility(View.GONE);
		
		RequestQueue queue = MyVolley.getRequestQueue();
		JsonObjectRequest myReq = new JsonObjectRequest(Method.GET,
				ConsumerApi.getSearchTags("all", ConsumerApi.LEVELDEVICEMAX),
				null, createMyReqSuccessListener(), createMyReqErrorListener());
		myReq.setShouldCache(true);

		Log.d("tagresponse", myReq.getUrl());
		queue.add(myReq);
		
	}

	private Response.Listener<JSONObject> createMyReqSuccessListener() {
		return new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				ParseJonsResponse(response);
				mMainActivity.hideActionBarProgressBar();
				dismissProgressBar();
			}
		};
	}

	private void ParseJonsResponse(JSONObject response) {
		try {
			JSONObject tags = response.getJSONObject("tags");
			if(tags ==null || tags.length() ==0)
			{
				dismissProgressBar();
				return;
			}
			JSONObject qualifiers = null;
			JSONObject startletters = null;
			JSONObject innerObj = null;
			Iterator<?> responseIterator = tags.keys();
			while (responseIterator.hasNext()) {
				String tagKey = (String) responseIterator.next();
				if (tagKey.equalsIgnoreCase("qualifiers"))
					qualifiers = tags.getJSONObject("qualifiers");
				else if (tagKey.equalsIgnoreCase("startLetters"))
					startletters = tags.getJSONObject("startLetters");
				else {

				}
			}
			mListData.clear();

			for (int i = 0; i < 2; i++) {
				switch (i) {
				case 0:
					innerObj = qualifiers;
					if (innerObj != null){
						Iterator<?> it = innerObj.keys();
						while (it.hasNext()) {
							String key = (String) it.next();
							FillListData(key, innerObj);
						}
					}
					break;
				case 1:
					innerObj = startletters;

					if (innerObj != null) {
						// Sorting Json response start
						SortedMap<String, Object> sortedObj = new TreeMap<String, Object>();
						Iterator<?> sortingIterator = innerObj.keys();
						while (sortingIterator.hasNext()) {
							String key = (String) sortingIterator.next();
							sortedObj.put(key, innerObj.getJSONObject(key));
						}
						// Sorting Json response end
						Iterator<String> sortedIterator = sortedObj.keySet()
								.iterator();
						while (sortedIterator.hasNext()) {
							String key = (String) sortedIterator.next();
							FillListData(key, innerObj);
						}
					}
					break;
				default:
					break;
				}
			}
			mMasterListData = mListData;
			mAdapter.notifyDataSetChanged();
			preapareFilterData();
		} catch (JSONException e) {
			Log.e("response Exception", e.getMessage());
			dismissProgressBar();
		}
	}

	private void preapareFilterData() {
		if (mUniqueCategories != null) {
			List<FilterMenudata> searchFilter = new ArrayList<FilterMenudata>();
			searchFilter.add(new FilterMenudata(FilterMenudata.ITEM,
					allCategories, 0));
			Iterator<String> mapIterator = mUniqueCategories.keySet()
					.iterator();
			while (mapIterator.hasNext()) {
				String categoryName = (String) mapIterator.next();
				searchFilter.add(new FilterMenudata(FilterMenudata.ITEM,
						categoryName, 0));
			}
			if (isVisible() && searchFilter.size() >1) {
				mMainActivity.addFilterData(searchFilter,
						mFilterMenuClickListener);
			}
		}
	}

	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof FilterMenudata) {
				String label = ((FilterMenudata) v.getTag()).label;
				sortTags(label);
			}
		}
	};

	private void sortTags(String sortCategory) {
		Log.d("searchSort", "based on:" + sortCategory);

		if (sortCategory.equalsIgnoreCase(allCategories)) {
			mAdapter.setSearchDataList(mMasterListData);
		} else {
			// Creating a local copy from masterList for sorting
			List<SearchData> sortListdata = new ArrayList<SearchData>();
			for (SearchData searchData : mMasterListData) {
				sortListdata.add(new SearchData(searchData));
			}

			// holds data to be removed from list whose Data is not matching the
			// category
			List<SearchData> itemsToRemove = new ArrayList<SearchData>();
			for (int sortcount = 0; sortcount < sortListdata.size(); sortcount++) {
				SearchData tempSearchData = sortListdata.get(sortcount);
				if (tempSearchData == null
						|| tempSearchData.getSearchTags() == null) {
					continue;
				}
				List<ButtonData> matchingButtonData = new ArrayList<SearchData.ButtonData>();
				for (int tagscount = 0; tagscount < tempSearchData
						.getSearchTags().size(); tagscount++) {
					if (tempSearchData.getSearchTags().get(tagscount)
							.getTagCategory() != null
							&& tempSearchData.getSearchTags().get(tagscount)
									.getTagCategory()
									.equalsIgnoreCase(sortCategory))
						matchingButtonData.add(tempSearchData.getSearchTags()
								.get(tagscount));
				}
				tempSearchData.setSearchTags(matchingButtonData);

				if (matchingButtonData.size() <= 0) {
					// Remove Header and items from list which doesn't have
					// matching data
					itemsToRemove.add(sortListdata.get(sortListdata
							.indexOf(tempSearchData) - 1));
					itemsToRemove.add(tempSearchData);
				}
			}
			sortListdata.removeAll(itemsToRemove);

			mAdapter.setSearchDataList(sortListdata);
		}
		mAdapter.notifyDataSetChanged();
	}

	private void FillListData(String key, JSONObject object) {
		try {

			SearchData searchableObj = new SearchData();
			searchableObj.setCategoryName(key);

			SearchData tagsObj = new SearchData();
			List<ButtonData> tagsInfo = new ArrayList<SearchData.ButtonData>();

			JSONObject letters = object.getJSONObject(key);
			JSONArray tagArray = letters.getJSONArray("values");

			for (int tagcount = 0; tagcount < tagArray.length(); tagcount++) {
				JSONObject tag = tagArray.getJSONObject(tagcount);
				String id = tag.getString("_id");
				String name = tag.getString("name");
				String qualifier = tag.getString("qualifier");
				String category = tag.getString("category");
				if (category != null && category.length() > 0) {
					int count = 0;
					if (mUniqueCategories.containsKey(category))
						count = mUniqueCategories.get(category);
					mUniqueCategories.put(category, (count + 1));// Filling
																	// Unique
																	// Categories
				}
				tagsInfo.add(new ButtonData(id, name, category, qualifier,
						false));
			}
			tagsObj.setSearchTags(tagsInfo);

			mListData.add(searchableObj);
			mListData.add(tagsObj);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("FillListData Exception", e.getMessage());
		}
	}

	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				//Analytics.endTimedEvent(Analytics.SearchScreenShown);
				TextView empty = (TextView) rootView.findViewById(R.id.empty);
				empty.setTypeface(FontUtil.Roboto_Light);
				empty.setVisibility(View.VISIBLE);
				mMainActivity.hideActionBarProgressBar();
				dismissProgressBar();
			}
		};
	}

	private void FillEditText(String buttonName) {
/////////AddAnalytics
		final FlowLayout spannablelayout = (FlowLayout) rootView
				.findViewById(R.id.spannable);
//		final HorizontalScrollView scroll = (HorizontalScrollView) rootView
//				.findViewById(R.id.selectionscroll);

		ImageView clearButton = (ImageView) rootView
				.findViewById(R.id.clearSearchresults);
		clearButton.setVisibility(View.VISIBLE);

		ButtonData userButton = new ButtonData(null, buttonName, "", "", false);
		Button button = CreateButton(userButton);
		button.setTag(userButton);
		MarginLayoutParams marginParams = new MarginLayoutParams(
				spannablelayout.getLayoutParams());
		marginParams.setMargins(0, 0, 0, 0);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				marginParams);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				spannablelayout.removeView(v);
				Button btn = (Button) v;
				UpdateSearchTags((ButtonData) btn.getTag(), false);
//				ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f,
//						0f);
//				fadeAnim.setDuration(800);
//
//				fadeAnim.addListener(new AnimatorListenerAdapter() {
//					public void onAnimationEnd(Animator animation) {
//						spannablelayout.removeView(v);
//						Button btn = (Button) v;
//						UpdateSearchTags((ButtonData) btn.getTag(), false);
//					}
//				});
//				fadeAnim.start();
			}
		});
//		ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(button, "alpha",
//				0f, 1f);
//		fadeinAnimation.setDuration(800);
//		fadeinAnimation.addListener(new AnimatorListenerAdapter() {
//			public void onAnimationEnd(Animator animation) {
////				scroll.fullScroll(View.FOCUS_RIGHT);
//			}
//		});
//		fadeinAnimation.start();
		spannablelayout.addView(button, layoutParams);
		UpdateSearchTags(userButton, true);
		UpdateRecentList(button.getText().toString());
	}

	private void FillEditText(View v) {
		/////////AddAnalytics
		ImageView clearButton = (ImageView) rootView
				.findViewById(R.id.clearSearchresults);
		clearButton.setVisibility(View.VISIBLE);

		final Button btn = (Button) v;
		final SearchData mSearchData = (SearchData) v.getTag();

		if (mSearchData.getSearchTags().get(btn.getId()).isCLicked() == true)
			return; // return if button is already clicked
		mSearchData.getSearchTags().get(btn.getId()).setCLicked(true);
		mAdapter.notifyDataSetChanged();

		FlowLayout spannablelayout = (FlowLayout) rootView
				.findViewById(R.id.spannable);
//		final HorizontalScrollView scroll = (HorizontalScrollView) rootView
//				.findViewById(R.id.selectionscroll);

		Button button = CreateButton(mSearchData.getSearchTags().get(
				btn.getId()));
		btn.setAlpha(mButtonApha);
		button.setId(btn.getId());
		button.setTag(R.string.tag1, mSearchData);
		button.setTag(R.string.tag2, (Button) v);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {

				FlowLayout spannablelayout = (FlowLayout) rootView
						.findViewById(R.id.spannable);
				spannablelayout.removeView(v);
				UpdateSearchTags(
						mSearchData.getSearchTags().get(btn.getId()),
						false);
				// FadeOut Animation of Clicked button Start
//				ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f,
//						0f);
//				fadeAnim.setDuration(800);
//				fadeAnim.addListener(new AnimatorListenerAdapter() {
//					public void onAnimationEnd(Animator animation) {
//						FlowLayout spannablelayout = (FlowLayout) rootView
//								.findViewById(R.id.spannable);
//						spannablelayout.removeView(v);
//						UpdateSearchTags(
//								mSearchData.getSearchTags().get(btn.getId()),
//								false);
//					}
//				});
//				fadeAnim.start();

				// FadeOut Animation of Clicked button End

				// TODO:: fadein Animation not working. need to fix this 
				// FadeIn Animation in list
				
				final Button ownerButton = (Button) v.getTag(R.string.tag2);
				ownerButton.setAlpha(1f);
				SearchData mSearchData = (SearchData) v
						.getTag(R.string.tag1);
				mSearchData.getSearchTags().get(ownerButton.getId())
						.setCLicked(false);
				mAdapter.notifyDataSetChanged();
//				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(ownerButton,
//						"alpha", mButtonApha, 1f);
//				fadeAnim2.setDuration(800);
//				fadeAnim2.addListener(new AnimatorListenerAdapter() {
//					public void onAnimationEnd(Animator animation) {
//						SearchData mSearchData = (SearchData) v
//								.getTag(R.string.tag1);
//						mSearchData.getSearchTags().get(ownerButton.getId())
//								.setCLicked(false);
//						mAdapter.notifyDataSetChanged();
//					}
//				});
//				fadeAnim2.start();
				// FadeIn Animation in list
			}
		});

		MarginLayoutParams marginParams = new MarginLayoutParams(
				spannablelayout.getLayoutParams());
		marginParams.setMargins(0, 0, 0, 0);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				marginParams);

//		ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(button, "alpha",
//				0f, 1f);
//		fadeinAnimation.setDuration(800);
//		fadeinAnimation.addListener(new AnimatorListenerAdapter() {
//			public void onAnimationEnd(Animator animation) {
////				scroll.fullScroll(View.FOCUS_RIGHT);
//			}
//		});
//		fadeinAnimation.start();
		spannablelayout.addView(button, layoutParams);
		UpdateSearchTags(mSearchData.getSearchTags().get(btn.getId()), true);
		UpdateRecentList(button.getText().toString());

	}

	private void UpdateRecentList(String tagname) {
		/*
		 * ArrayList<String> stringArrayList = new ArrayList<String>();
		 * stringArrayList
		 * .toArray(adapter.getmSearchDataList().get(0).getNames());
		 * stringArrayList.add(tagname);
		 * 
		 * Object[] ObjectList = stringArrayList.toArray(); String[] StringArray
		 * = Arrays.copyOf(ObjectList,ObjectList.length,String[].class);
		 * 
		 * adapter.getmSearchDataList().get(0).setNames(StringArray);
		 * adapter.notifyDataSetChanged(); adapter.notifyDataSetInvalidated();
		 */
	}

	@Override
	public void OnOpenAction(View v) {
		FillEditText(v);
	}

	private Button CreateButton(final ButtonData tagData) {
		final Button btn = new Button(getContext());
		btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		btn.setMinimumHeight(0);
		btn.setMinHeight(0);
		btn.setTextColor(Color.parseColor("#FFFFFF"));
		btn.setText(tagData.getButtonName()+" ");
		btn.setTextSize(14f);
		btn.setTypeface(FontUtil.Roboto_Medium);
//		btn.setBackgroundResource(R.drawable.roundedbutton);
		Drawable drawableRight = getResources().getDrawable(R.drawable.tagclose);
		drawableRight.setBounds(0, 0, (int) (drawableRight.getIntrinsicWidth()), (int) (drawableRight.getIntrinsicHeight()));
		btn.setCompoundDrawables(null, null, drawableRight, null);
//		btn.setCompoundDrawablePadding(8);
		btn.setBackgroundResource(R.drawable.roundedbuttonwithclose);

		return btn;
	}

	public void showProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(getContext(), "",
				"Fetching Data...", true, false);
	}

	public void dismissProgressBar() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	public void updateText(String str) {
		if (mProgressDialog != null) {
			mProgressDialog.setTitle(str);
		}
	}

	@Override
	public void OnCacheResults(HashMap<String, CardData> obj,boolean issuedRequest) {
		if (obj == null) {
			return;
		}

		CardExplorerData dataBundle = myplexapplication.getCardExplorerData();

		dataBundle.reset();
		dataBundle.searchQuery = mSearchQuery;
		dataBundle.requestType = CardExplorerData.REQUEST_SEARCH;

		mMainActivity.addFilterData(new ArrayList<FilterMenudata>(), null);

		Set<String> keySet = obj.keySet();
		for (String key : keySet) {
			CardData data = obj.get(key);
			// dataBundle.mEntries.add(data);
			// if(dataBundle.mEntries.get(key) == null){
			dataBundle.mEntries.put(key, data);
			dataBundle.mMasterEntries.add(data);
			// }
			if (data.generalInfo != null)
				Log.i(TAG, "adding " + data._id + ":" + data.generalInfo.title
						+ " from Cache");
		}
		mCacheManager.unRegisterCallback();
		mMainActivity.hideActionBarProgressBar();
		BaseFragment fragment = mMainActivity
				.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		mMainActivity.bringFragment(fragment);
	}

	@Override
	public void OnOnlineResults(List<CardData> dataList) {

	}

	@Override
	public void OnOnlineError(VolleyError error) {
		mMainActivity.hideActionBarProgressBar();
	}
}
