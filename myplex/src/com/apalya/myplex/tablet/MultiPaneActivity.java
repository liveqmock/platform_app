package com.apalya.myplex.tablet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.R;
import com.apalya.myplex.SearchActivity;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.adapters.SearchListAdapter;
import com.apalya.myplex.adapters.OpenListener.OpenCallBackListener;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.NavigationOptionsMenu;
import com.apalya.myplex.data.SearchData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.data.SearchData.ButtonData;
import com.apalya.myplex.receivers.ConnectivityReceiver;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.VersionUpdateUtil;
import com.apalya.myplex.utils.VersionUpdateUtil.VersionUpdateCallbackListener;
import com.apalya.myplex.views.FlowLayout;
import com.apalya.myplex.views.PinnedSectionListView;

public class MultiPaneActivity extends BaseActivity implements OpenCallBackListener, CacheManagerCallback{

	private ListView mLeftNavigationListView;
	
	private TextView mSearchBox;
	private RelativeLayout mSearchToggler;
	private RelativeLayout mDrawerlayout;
	
	//Code from SearchActivity
	private EditText mSearchInput;
	private List<ButtonData> mSearchbleTags = null;
	private List<SearchData> mListData = null;
	private PinnedSectionListView mPinnedListView;
	private SearchListAdapter mAdapter;
	private float mButtonApha = 0.25f;
	private CacheManager mCacheManager = new CacheManager();
	private String mSearchQuery = new String();
	//Code end from SearchActiivty
	
	public static final String TAG = "MultiPaneActivity";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.cardexplorer_tablet);
		setParentView(findViewById(R.id.parentlayout));
		super.onCreate(savedInstanceState);
		
		if(getResources().getBoolean(R.bool.isTablet))
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		else
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		mLeftNavigationListView = (ListView) findViewById(R.id.left_drawer);
		prepareNavigationMenuList(mLeftNavigationListView);
		mContentLayout = (FrameLayout) findViewById(R.id.content_frame);
		prepareNavipane();
		prepareCustomActionBar();
		enableFilterAction(true);
		onHandleExternalIntent(getIntent());
		updateClientCheck();
		createCardExplorer();
	}
	
	private void updateClientCheck() {
		
		String url = getResources().getString(R.string.config_url_versionupdate);
		VersionUpdateUtil versionUpdateUtil = new VersionUpdateUtil(this,new VersionUpdateCallbackListener() {
			
			@Override
			public boolean showUpgradeDialog() {
			
				if(myplexapplication.getCardExplorerData().cardDataToSubscribe != null || isFinishing()){
					return false;
				}
				
				return true;
			}
		});
		
		versionUpdateUtil.checkIfUpgradeAvailable(url,getResources().getString(R.string.old_date_for_upgrade));
	}

	@Override
	protected void onResume() {
		
/*
		
		if(myplexapplication.mSelectedOption_Tablet != NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION){
			createCardExplorer();
		}else{
			OnSelectedOption(myplexapplication.mSelectedOption_Tablet, "live tv");
		}*/
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		
		Log.d(TAG,"onStop");
		if(mCacheManager != null){
			mCacheManager.deRegistration();
		}
		super.onStop();
	}
	
	@Override
	public void fillMenuItem() {
		super.fillMenuItem();
	}
	
	//Search related code
	
	@Override
	public void searchButtonClicked() {

		if (mSearchbleTags == null || mSearchbleTags.size() <= 0)
		{
//			Toast.makeText(mContext,  "Select search tags or enter text for search ",  Toast.LENGTH_LONG).show();
			Util.showToast(mContext,"Select search tags or enter text for search.",Util.TOAST_TYPE_INFO);
			return;
		}
//		mMainActivity.showActionBarProgressBar();
		Analytics.SEARCH_TYPE = "actionbar";
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
		mSearchQuery = searchQuery;
		
		IndexHandler.OperationType searchType = IndexHandler.OperationType.DONTSEARCHDB;
		if(!Util.isNetworkAvailable(mContext))
			searchType = IndexHandler.OperationType.FTSEARCH;
		mCacheManager.getCardDetails(searchString, searchType, MultiPaneActivity.this);


	}
	
	private void prepareNavipane()
	{
		//Close and open animations for search screen
		mSearchBox = (TextView) findViewById(R.id.searchBox);
		mSearchToggler = (RelativeLayout) findViewById(R.id.tablayoutchanger);
		mDrawerlayout = (RelativeLayout) findViewById(R.id.drawerlayout);
		
		mSearchBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation slideDown = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
				Analytics.mixPanelDiscoveryOptionSelected();
				slideDown.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					@Override
					public void onAnimationEnd(Animation animation) {
						mDrawerlayout.setVisibility(View.GONE);
						mSearchToggler.setVisibility(View.VISIBLE);
					}
				});
				mDrawerlayout.startAnimation(slideDown);
				findViewById(R.id.searchlayoutinclude).setVisibility(View.VISIBLE);
				PinnedSectionListView listview = (PinnedSectionListView)findViewById(R.id.list_view);
				ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) listview .getLayoutParams();
				mlp.setMargins(0, 0, 0, 40);
				listview.setLayoutParams(mlp);
				
			}
		});
		
		mSearchToggler.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final View layout = findViewById(R.id.searchlayoutinclude);
				Animation slideup = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
				slideup.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationRepeat(Animation animation) {}
					@Override
					public void onAnimationEnd(Animation animation) {
						layout.setVisibility(View.GONE);
						}
				});
				mSearchToggler.setVisibility(View.GONE);
				mDrawerlayout.setVisibility(View.VISIBLE);
				mDrawerlayout.startAnimation(slideup);
			}
		});
		
		loadSearchData();	
	}
	
	private void loadSearchData()
	{
		createSearchView();
		loadSearchTags();
	}
	
	private void createSearchView()
	{
		mSearchInput = (EditText) findViewById(R.id.inputSearch);
		mSearchInput.setTypeface(FontUtil.Roboto_Regular);
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
		
		final ImageView clearButton = (ImageView)findViewById(R.id.clearSearchresults);
		clearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final FlowLayout spannablelayout = (FlowLayout)findViewById(R.id.spannable);
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
			}
		});
		
		try {
			mPinnedListView = (PinnedSectionListView)findViewById(R.id.list_view);
			mListData = new ArrayList<SearchData>();
			mSearchbleTags = new ArrayList<SearchData.ButtonData>();
			mAdapter = new SearchListAdapter(mContext, mListData);
			mAdapter.setOpenListener(MultiPaneActivity.this);
			mPinnedListView.setAdapter(mAdapter);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadSearchTags() {
		mListData.clear();
		//showProgressBar();
		TextView empty = (TextView) findViewById(R.id.empty);
		empty.setVisibility(View.GONE);
		
		RequestQueue queue = MyVolley.getRequestQueue();
		JsonObjectRequest myReq = new JsonObjectRequest(Method.GET,
				ConsumerApi.getSearchTags("all", ConsumerApi.LEVELDEVICEMAX),
				null, createMyReqSuccessListener(), createMyReqErrorListener());
		myReq.setShouldCache(true);

		Log.d("tagresponse", myReq.getUrl());
		queue.add(myReq);
	}

	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				TextView empty = (TextView) findViewById(R.id.empty);
				empty.setTypeface(FontUtil.Roboto_Light);
				empty.setVisibility(View.VISIBLE);
//				dismissProgressBar();
			}
		};
	}

	private Response.Listener<JSONObject> createMyReqSuccessListener() {
		return new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				ParseJonsResponse(response);
//				dismissProgressBar();
			}
		};
	}
	
	private void ParseJonsResponse(JSONObject response) {
		try {
			JSONObject tags = response.getJSONObject("tags");
			if(tags ==null || tags.length()==0)
			{
				Util.showToast(mContext,"No data for tags.",Util.TOAST_TYPE_ERROR);
//				Toast.makeText(mContext,  "No data for tags",  Toast.LENGTH_LONG).show();
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
//			mMasterListData = mListData;
			mAdapter.notifyDataSetChanged();
//			preapareFilterData();
		} catch (JSONException e) {
			if(e == null){return;}
			Log.e("response Exception", e.getMessage());
			Util.showToast(mContext,e.getMessage(),Util.TOAST_TYPE_ERROR);
//				dismissProgressBar();
		}
	}
	
	private void FillEditText(View v) {

		ImageView clearButton = (ImageView)findViewById(R.id.clearSearchresults);
		clearButton.setVisibility(View.VISIBLE);

		final Button btn = (Button) v;
		final SearchData mSearchData = (SearchData) v.getTag();

		if (mSearchData.getSearchTags().get(btn.getId()).isCLicked() == true)
			return; // return if button is already clicked
		mSearchData.getSearchTags().get(btn.getId()).setCLicked(true);
		mAdapter.notifyDataSetChanged();

		FlowLayout spannablelayout = (FlowLayout)findViewById(R.id.spannable);
//		final HorizontalScrollView scroll = (HorizontalScrollView) findViewById(R.id.selectionscroll);

		Button button = CreateButton(mSearchData.getSearchTags().get(
				btn.getId()));
		btn.setAlpha(mButtonApha);
		button.setId(btn.getId());
		button.setTag(R.string.tag1, mSearchData);
		button.setTag(R.string.tag2, (Button) v);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				FlowLayout spannablelayout = (FlowLayout) findViewById(R.id.spannable);
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
//						LinearLayout spannablelayout = (LinearLayout) findViewById(R.id.spannable);
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
		marginParams.setMargins(0, 0, 10, 0);
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
//		UpdateRecentList(button.getText().toString());

	}
	
	private void FillEditText(String buttonName) {
		final FlowLayout spannablelayout = (FlowLayout) findViewById(R.id.spannable);
//		final HorizontalScrollView scroll = (HorizontalScrollView) findViewById(R.id.selectionscroll);

		ImageView clearButton = (ImageView) findViewById(R.id.clearSearchresults);
		clearButton.setVisibility(View.VISIBLE);

		ButtonData userButton = new ButtonData(null, buttonName, "", "", false);
		Button button = CreateButton(userButton);
		button.setTag(userButton);
		MarginLayoutParams marginParams = new MarginLayoutParams(
				spannablelayout.getLayoutParams());
		marginParams.setMargins(0, 0, 10, 0);
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
		/*UpdateRecentList(button.getText().toString());*/
	}
	
	private Button CreateButton(final ButtonData tagData) {
		final Button btn = new Button(mContext);
		btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		btn.setMinimumHeight(0);
		btn.setMinHeight(0);
		btn.setTextColor(Color.parseColor("#FFFFFF"));
		btn.setText(tagData.getButtonName()+" ");
		btn.setTextSize(14f);
		btn.setTypeface(FontUtil.Roboto_Light);
		btn.setBackgroundResource(R.drawable.roundedbutton);
		Drawable drawableRight = getResources().getDrawable(R.drawable.tagclose);
		drawableRight.setBounds(0, 0, (int) (drawableRight.getIntrinsicWidth()), (int) (drawableRight.getIntrinsicHeight()));
		btn.setCompoundDrawables(null, null, drawableRight, null);
//		btn.setCompoundDrawablePadding(8);
		btn.setBackgroundResource(R.drawable.roundedbuttonwithclose);

		return btn;
	}
	
	private void UpdateSearchTags(ButtonData tagData, Boolean addorremove) {
		if (mSearchbleTags == null)
			return;
		if (addorremove)
			mSearchbleTags.add(tagData);
		else
			mSearchbleTags.remove(tagData);
		
		if (mSearchbleTags.size() == 0)
			setSearchBarVisibilty(View.INVISIBLE);
		else
		{
			searchButtonClicked();
			setSearchBarVisibilty(View.INVISIBLE);
		}
	}
	
	private void ClearSearchTags() {
		if (mSearchbleTags != null)
			mSearchbleTags.clear();
	}
	
	@Override
	public void OnOpenAction(View v) {
		FillEditText(v);
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
/*					if (mUniqueCategories.containsKey(category))
						count = mUniqueCategories.get(category);
					mUniqueCategories.put(category, (count + 1));// Filling
																	// Unique
																	// Categories
*/				}
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

	@Override
	public void OnCacheResults(HashMap<String, CardData> obj,boolean issuedRequest) {

		if (obj == null) {
			return;
		}

		CardExplorerData dataBundle = myplexapplication.getCardExplorerData();

		dataBundle.reset();
		dataBundle.searchQuery = mSearchQuery;
		dataBundle.requestType = CardExplorerData.REQUEST_SEARCH;

		addFilterData(new ArrayList<FilterMenudata>(), null);

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
//		mMainActivity.hideActionBarProgressBar();
		BaseFragment fragment = createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
		bringFragment(fragment);
	}
	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnOnlineError(VolleyError error) {
		// TODO Auto-generated method stub
		
	}
	
	//Search related code end
	@Override
	public void setUpShareButton(String toBeShared) 
	{
		super.setUpShareButton(toBeShared);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  
	  /*
	  savedInstanceState.putBoolean("MyBoolean", true);
	  savedInstanceState.putDouble("myDouble", 1.9);
	  savedInstanceState.putInt("MyInt", 1);
	  savedInstanceState.putString("MyString", "Welcome back to Android");	  
	  */	  
	  // etc.
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
	  
	  /*
	  boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
	  double myDouble = savedInstanceState.getDouble("myDouble");
	  int myInt = savedInstanceState.getInt("MyInt");
	  
	  
	  String myString = savedInstanceState.getString("MyString");
	
	
*/	
	}
	
	private String _id;
	
	private boolean onHandleExternalIntent(Intent intent) {
		
		if(intent==null)
			return false;
		
		if(!ConnectivityReceiver.isConnected){			
			return false;								
		} 
		
		boolean intentHandled = false;
		
		Analytics.mixPanelNotificationReceived(mContext, getIntent());
		
		if(getIntent().hasExtra(mContext.getString(R.string._id))){
			showActionBarProgressBar();
			_id = getIntent().getExtras().getString(mContext.getString(R.string._id));
			List<CardData> cards  =  new ArrayList<CardData>();
			CardData cardData  = new CardData();
			cardData._id = _id;
			cards.add(cardData);
			if(_id!=null && _id.length() >0){
				mCacheManager.getCardDetails(cards, IndexHandler.OperationType.FTSEARCH, new CacheManagerCallback() {					
					@Override
					public void OnOnlineResults(List<CardData> dataList) {
						for (CardData cardData : dataList) {
							if(cardData._id.equalsIgnoreCase(_id)){
								mCacheManager.unRegisterCallback();
								hideActionBarProgressBar();
								if(mCurrentFragment != null || isFinishing()) {return;}
								myplexapplication.mSelectedCard = cardData;
								startActivity(new Intent(getApplicationContext(),TabletCardDetails.class));
								finish();
								break;
							}
						}
					}					
					@Override
					public void OnOnlineError(VolleyError error) {
					}					
					@Override
					public void OnCacheResults(HashMap<String, CardData> obj,
							boolean issuedRequest) {
						CardData data = null;
						
						 Iterator<Entry<String, CardData>> it = obj.entrySet().iterator();
						    while (it.hasNext()) {
						        Entry<String, CardData> pair = it.next();
						        if(pair.getValue()._id.equalsIgnoreCase(_id)){
						        	data = pair.getValue();
						        	break;
						        }
						    }
						
						if(data == null){							
							return;
						}
						    
						hideActionBarProgressBar();
						mCacheManager.unRegisterCallback();
						if(mCurrentFragment != null || isFinishing()) {return;}
						myplexapplication.mSelectedCard = data;
						startActivity(new Intent(getApplicationContext(),TabletCardDetails.class));
						finish();
					}
				});
			}
			intentHandled=true;
		}
		String action  = "";
		if(intent.hasExtra(mContext.getString(R.string.page))){
			action = intent.getStringExtra(mContext.getString(R.string.page));
		}
		if(action.length()>0){
			if(action.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE)){
				OnSelectedOption(1,NavigationOptionsMenuAdapter.LIVETV);
				intentHandled=true;
			}else if(action.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_MOVIE)){
				OnSelectedOption(1,NavigationOptionsMenuAdapter.MOVIES);
				intentHandled=true;
			}else if(action.equalsIgnoreCase(NavigationOptionsMenuAdapter.RECOMMENDED)){
				OnSelectedOption(1,NavigationOptionsMenuAdapter.RECOMMENDED);
				intentHandled=true;
			}else if(action.equalsIgnoreCase(NavigationOptionsMenuAdapter.TVSHOWS)){
				OnSelectedOption(1,NavigationOptionsMenuAdapter.TVSHOWS);
				intentHandled=true;
			}
			return intentHandled;
		}
		return intentHandled;
	}
}
