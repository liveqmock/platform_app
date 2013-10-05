package com.apalya.myplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apalya.myplex.adapters.SearchListAdapter;
import com.apalya.myplex.adapters.OpenListener.OpenCallBackListener;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.SearchData;
import com.apalya.myplex.data.SearchData.ButtonData;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.views.PinnedSectionListView;
import com.flurry.android.FlurryAgent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchActivity extends BaseFragment implements OpenCallBackListener {

	EditText mSearchInput;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.searchlayout, container, false);
		mSearchInput = (EditText) rootView.findViewById(R.id.inputSearch);
		mSearchInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					FillEditText(mSearchInput.getText().toString());
					Analytics.trackEvent("TEXT-TYPED-"+mSearchInput.getText().toString());
					mSearchInput.setText("");
				}
				return false;
			}

		});

		final ImageView clearButton = (ImageView)rootView.findViewById(R.id.clearSearchresults);
		clearButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Analytics.trackEvent("DELETE-ALL-SELECTED-SERCH-TAGS");
				final LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
				spannablelayout.removeAllViews();
				ClearSearchTags();
				clearButton.setVisibility(View.GONE);
				if(mListData !=null)
				{
					for(int count =0; count < mListData.size(); count++)
					{
						if(mListData.get(count) ==null || mListData.get(count).getSearchTags() ==null)
							continue;
						int tagcount = mListData.get(count).getSearchTags().size();
						for(int tags=0; tags < tagcount; tags++)
						{
							if(mListData.get(count).getSearchTags().get(tags) !=null)
								mListData.get(count).getSearchTags().get(tags).setCLicked(false);
						}
					}
					
					mAdapter.notifyDataSetChanged();
				}
			}
		});
		// ArrayList<SearchData> mSearchData = fillSearchData();
		try {
			mPinnedListView = (PinnedSectionListView) rootView.findViewById(R.id.list_view);
			mListData = new ArrayList<SearchData>();
			mSearchbleTags = new ArrayList<SearchData.ButtonData>();
			mAdapter = new SearchListAdapter(getContext(), mListData);
			mAdapter.setOpenListener(SearchActivity.this);
			mPinnedListView.setAdapter(mAdapter);

		} catch (Exception e) {
			e.printStackTrace();
		}
		mMainActivity.setTitle("Search");

		loadSearchTags();

		return rootView;
	}
	
	//Boolean true to add, false to remove
	private void UpdateSearchTags(ButtonData tagData, Boolean addorremove)
	{
		if(mSearchbleTags == null)
			return;
		if(addorremove)
		{
			mSearchbleTags.add(tagData);
			Analytics.endTimedEvent("SEARCH-TAG-ADDED-"+tagData.getButtonName());
		}
		else
		{
			mSearchbleTags.remove(tagData);
			Analytics.endTimedEvent("SEARCH-TAG-REMOVED-"+tagData.getButtonName());
		}
	}
	
	private void ClearSearchTags()
	{
		if(mSearchbleTags !=null)
			mSearchbleTags.clear();
	}
	
	public List<ButtonData> GetSearchTags()
	{
		return mSearchbleTags;
	}

	private void loadSearchTags() {
		mListData.clear();
		Analytics.trackEvent("SEARCH-REQUEST",true);
		mMainActivity.showActionBarProgressBar();
		showProgressBar();
		RequestQueue queue = MyVolley.getRequestQueue();
		JsonObjectRequest myReq = new JsonObjectRequest(Method.GET, "http://dev.myplex.in/content/v2/tags/", null,
				createMyReqSuccessListener(), createMyReqErrorListener());
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
				Analytics.endTimedEvent("SEARCH-REQUEST");
				Analytics.trackEvent("SEARCH-REQUEST-SUCCESS");
			}
		};
	}

	private void ParseJonsResponse(JSONObject response)
	{
		try {
			JSONObject tags = response.getJSONObject("tags");
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
					Iterator<?> it = innerObj.keys();
					while (it.hasNext()) {
						String key = (String) it.next();
						FillListData(key,innerObj);
					}
					break;
				case 1:
					innerObj = startletters;
					
					//Sorting Json response start
					SortedMap<String, Object> sortedObj = new TreeMap<String, Object>();
					Iterator<?> sortingIterator = innerObj.keys();
					while (sortingIterator.hasNext()) {
						String key = (String) sortingIterator.next();
						sortedObj.put(key, innerObj.getJSONObject(key));
					}
					//Sorting Json response end
					Iterator<String> sortedIterator = sortedObj.keySet().iterator();
					while (sortedIterator.hasNext()) {
						String key = (String) sortedIterator.next();
						FillListData(key,innerObj);
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
		}
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Analytics.trackEvent("SEARCH-SCREEN-VIEWED");
	}
	private void preapareFilterData()
	{
		if(mUniqueCategories !=null)
		{
			List<FilterMenudata> searchFilter = new ArrayList<FilterMenudata>();
			searchFilter.add(new FilterMenudata(FilterMenudata.ITEM, allCategories, 0));
			Iterator<String> mapIterator = mUniqueCategories.keySet().iterator();
			while (mapIterator.hasNext()) {
				String categoryName = (String) mapIterator.next();
				searchFilter.add(new FilterMenudata(FilterMenudata.ITEM, categoryName, 0));
			}
			mMainActivity.addFilterData(searchFilter,mFilterMenuClickListener);
		}
	}
	
	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof FilterMenudata) {
				String label = ((FilterMenudata) v.getTag()).label;
				sortTags(label);
				Analytics.trackEvent("SEARCH-FILTER-TAG-SELECTED-"+label);
			}
		}
	};
	
	private void sortTags(String sortCategory)
	{
		Log.d("searchSort", "based on:"+sortCategory);
		
		if(sortCategory.equalsIgnoreCase(allCategories))
		{
			mAdapter.setSearchDataList(mMasterListData);
		}
		else
		{
			//Creating a local copy from masterList for sorting
			List<SearchData> sortListdata = new ArrayList<SearchData>();
			for (SearchData searchData : mMasterListData) {
				sortListdata.add(new SearchData(searchData));
			} 
			
			//holds data to be removed from list whose Data is not matching the category
			List<SearchData> itemsToRemove = new ArrayList<SearchData>();
			for(int sortcount =0; sortcount< sortListdata.size(); sortcount++)
			{
				SearchData tempSearchData = sortListdata.get(sortcount);
				if(tempSearchData ==null || tempSearchData.getSearchTags() ==null)
				{
					continue;
				}
				List<ButtonData> matchingButtonData = new ArrayList<SearchData.ButtonData>();
				for(int tagscount =0; tagscount< tempSearchData.getSearchTags().size(); tagscount++)
				{
					if(tempSearchData.getSearchTags().get(tagscount).getTagCategory() !=null && tempSearchData.getSearchTags().get(tagscount).getTagCategory().equalsIgnoreCase(sortCategory))
						matchingButtonData.add(tempSearchData.getSearchTags().get(tagscount));
				}
				tempSearchData.setSearchTags(matchingButtonData);
				
				if(matchingButtonData.size() <= 0)
				{
					//Remove Header and items from list which doesn't have matching data
					itemsToRemove.add(sortListdata.get(sortListdata.indexOf(tempSearchData)-1));
					itemsToRemove.add(tempSearchData);
				}
			}
			sortListdata.removeAll(itemsToRemove);
			
			mAdapter.setSearchDataList(sortListdata);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	private void FillListData(String key, JSONObject object)
	{
		try{
			
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
			if(category!=null && category.length() >0)
				mUniqueCategories.put(category, (mUniqueCategories.size()+1));// Filling Unique Categories
			tagsInfo.add(new ButtonData(id, name, category, qualifier, false));
		}
		tagsObj.setSearchTags(tagsInfo);

		mListData.add(searchableObj);
		mListData.add(tagsObj);
		}catch(Exception e)
		{
			e.printStackTrace();
			Log.e("FillListData Exception", e.getMessage());
		}
	}
	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				mMainActivity.hideActionBarProgressBar();
				dismissProgressBar();
				Analytics.endTimedEvent("SEARCH-REQUEST");
				Analytics.trackEvent("SEARCH-REQUEST-ERROR");
			}
		};
	}

	private void FillEditText(String buttonName) {
		final LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
		final HorizontalScrollView scroll = (HorizontalScrollView) rootView.findViewById(R.id.selectionscroll);

		ImageView clearButton = (ImageView)rootView.findViewById(R.id.clearSearchresults);
		clearButton.setVisibility(View.VISIBLE);
		
		final ButtonData userButton = new ButtonData(null, buttonName, "", "", false);
		Button button = CreateButton(userButton);
		MarginLayoutParams marginParams = new MarginLayoutParams(spannablelayout.getLayoutParams());
		marginParams.setMargins(0, 0, 10, 0);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginParams);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
				fadeAnim.setDuration(800);

				fadeAnim.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						spannablelayout.removeView(v);
						UpdateSearchTags(userButton, false);
					}
				});
				fadeAnim.start();
			}
		});
		ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
		fadeinAnimation.setDuration(800);
		fadeinAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				scroll.fullScroll(View.FOCUS_RIGHT);
			}
		});
		fadeinAnimation.start();
		spannablelayout.addView(button, layoutParams);
		UpdateSearchTags(new ButtonData(null, button.getText().toString(), null, null, false), true);
		UpdateRecentList(button.getText().toString());
	}

	private void FillEditText(View v) {

		ImageView clearButton = (ImageView)rootView.findViewById(R.id.clearSearchresults);
		clearButton.setVisibility(View.VISIBLE);
		
		final Button btn = (Button) v;
		final SearchData mSearchData = (SearchData) v.getTag();
		
		if(mSearchData.getSearchTags().get(btn.getId()).isCLicked() == true)
			return; // return if button is already clicked
		mSearchData.getSearchTags().get(btn.getId()).setCLicked(true);
		mAdapter.notifyDataSetChanged();
		
		LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
		final HorizontalScrollView scroll = (HorizontalScrollView) rootView.findViewById(R.id.selectionscroll);

		Button button = CreateButton(mSearchData.getSearchTags().get(btn.getId()));
		btn.setAlpha(mButtonApha);
		button.setId(btn.getId());
		button.setTag(R.string.tag1, mSearchData);
		button.setTag(R.string.tag2,(Button)v);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				
				// FadeOut Animation of Clicked button Start
				ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
				fadeAnim.setDuration(800);
				fadeAnim.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
						spannablelayout.removeView(v);
						UpdateSearchTags(mSearchData.getSearchTags().get(btn.getId()),false);
					}
				});
				fadeAnim.start();

				// FadeOut Animation of Clicked button End
				
				//TODO:: fadein Animation not working. need to fix this
				//FadeIn Animation in list
				final Button ownerButton =(Button)v.getTag(R.string.tag2);
				ownerButton.setAlpha(1f);
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(ownerButton, "alpha", mButtonApha, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						SearchData mSearchData = (SearchData) v.getTag(R.string.tag1);
						mSearchData.getSearchTags().get(ownerButton.getId()).setCLicked(false);
						mAdapter.notifyDataSetChanged();
					}
				});
				fadeAnim2.start();
				//FadeIn Animation in list
			}
		});

		MarginLayoutParams marginParams = new MarginLayoutParams(spannablelayout.getLayoutParams());
		marginParams.setMargins(0, 0, 10, 0);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginParams);

		ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
		fadeinAnimation.setDuration(800);
		fadeinAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				scroll.fullScroll(View.FOCUS_RIGHT);
			}
		});
		fadeinAnimation.start();
		spannablelayout.addView(button, layoutParams);
		UpdateSearchTags(mSearchData.getSearchTags().get(btn.getId()),true);
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
		Button btn = new Button(getContext());
		btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		btn.setBackgroundResource(R.drawable.roundedbutton);
		btn.setTextColor(Color.parseColor("#FFFFFF"));
		btn.setText(tagData.getButtonName());
		btn.setTextSize(14f);
		btn.setTypeface(FontUtil.Roboto_Regular);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				v.setAlpha(1.0f);
				Button btn1 = (Button) v;
				Button OwnerButton = (Button) btn1.getTag();
				ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(OwnerButton, "alpha", 0.5f, 1f);
				fadeAnim2.setDuration(800);
				fadeAnim2.start();
				
				ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
				fadeAnim.setDuration(800);

				fadeAnim.addListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator animation) {
						LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
						spannablelayout.removeView(v);
						UpdateSearchTags(tagData, false);
					}
				});
				fadeAnim.start();

			}
		});
		return btn;
	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FlurryAgent.onStartSession(this.getActivity(), "X6WWX57TJQM54CVZRB3K");
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(this.getActivity());
	}
	public void showProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(getContext(),"", "Fetching Data...", true,false);
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

}
