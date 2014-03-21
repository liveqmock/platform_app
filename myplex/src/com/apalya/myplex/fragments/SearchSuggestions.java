package com.apalya.myplex.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.NavigationOptionsMenuAdapter;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataHolder;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.CardImageView;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.CardImageLoader;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class SearchSuggestions extends BaseFragment {

	private View rootView;
	private static String TAG = "SearchSuggestions";
	private SearchListAdapter mSearchlistAdapter;
	private String mSearchQuery;
	private ListView mSearchSuggestionList;
	private CacheManager mCacheManager = new CacheManager();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.createScreenGA(Analytics.SCREEN_SEARCH_SUGGESTIONS);	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(mContext ==null)
			return null;
		rootView = inflater.inflate(R.layout.searchsuggestions, container, false);
		RelativeLayout layout = (RelativeLayout)rootView.findViewById(R.id.searchsuggestionslayout);
		layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "onclick");
			}
		});
		
		mSearchSuggestionList = (ListView)rootView.findViewById(R.id.suggestionlist);
		mSearchlistAdapter = new SearchListAdapter(mContext, android.R.layout.simple_list_item_1, android.R.id.text1,new ArrayList<CardData>());
		mSearchSuggestionList.setAdapter(mSearchlistAdapter);
		return rootView;
	}
	
	public void setQuery(String searchString,String type)
	{
		mSearchQuery = searchString;
		doInlineSearch(searchString,type);
	}

	
	private void doInlineSearch(String searchQuery,String type) {
		// TODO Auto-generated method stub
		RequestQueue queue = MyVolley.getRequestQueue();

		String requestUrl = new String();
		requestUrl = ConsumerApi.getInlineSearch(searchQuery,ConsumerApi.LEVELDYNAMIC);
		if(type!=null)
			requestUrl += ConsumerApi.AMPERSAND+ConsumerApi.BROWSETYPE+type;
		StringRequest myReq = new StringRequest(requestUrl, searchResults(), searchResultsError());
		myReq.setShouldCache(true);
		queue.add(myReq);
		Log.d(TAG, requestUrl);
	}
	
	private Response.Listener<String> searchResults() {
		return new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.d(TAG, response);
				CardResponseData minResultSet = null;
				try {
					minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(minResultSet.results == null){
					return;
				}
				if(minResultSet.results.size() > 3)
					minResultSet.results = minResultSet.results.subList(0, 3);
				mSearchlistAdapter = new SearchListAdapter(mContext, android.R.layout.simple_list_item_1, android.R.id.text1,minResultSet.results);
				mSearchSuggestionList.setAdapter(mSearchlistAdapter);
				mSearchSuggestionList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						List<CardData> searchResults = mSearchlistAdapter.getSearchResults();
						
						if(position < searchResults.size())
						{
							String localSearchScope = null;
							CardExplorerData dataBundle = myplexapplication.getCardExplorerData();
							Analytics.SEARCH_TYPE = "inline"; //added for analytics

//							dataBundle.reset();
							if(dataBundle != null){			
								if((dataBundle.searchScope!=null) && dataBundle.searchScope.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE)){
									localSearchScope = ConsumerApi.VIDEO_TYPE_LIVE;
								}
								dataBundle.reset();
								if(localSearchScope != null){
									dataBundle.searchScope = localSearchScope;
								}
							}

							dataBundle.searchQuery = searchResults.get(position)._id;
							dataBundle.requestType = CardExplorerData.REQUEST_INLINESEARCH;
							BaseFragment fragment = mMainActivity.createFragment(NavigationOptionsMenuAdapter.CARDEXPLORER_ACTION);
							mMainActivity.bringFragment(fragment);
							mMainActivity.setActionBarTitle(searchResults.get(position).generalInfo.title.toLowerCase());
							Analytics.SELECTED_INLINE_WORD = searchResults.get(position).generalInfo.title.toLowerCase();
						}
						
					}

				});
				mSearchlistAdapter.notifyDataSetChanged();
			}
		};
	}
	
	private Response.ErrorListener searchResultsError() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "onErrorResponse");
			}
		};
	}
	
}

class SearchListAdapter extends ArrayAdapter<CardData>{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<CardData> mDataList;
	
	public SearchListAdapter(Context context, int resource,
			int textViewResourceId, List<CardData> objects) {
		super(context, resource, textViewResourceId, objects);
		this.mContext = context;
		mInflater = LayoutInflater.from(mContext);
		setData(objects);
	}
	
	public void setData(List<CardData> datalist){
		mDataList = new ArrayList<CardData>();
		if (datalist != null) {
			for (CardData data : datalist) {
				this.mDataList.add(data);
			}
		}
	}
	
	public List<CardData> getSearchResults()
	{
		return mDataList;
	}
	
	@Override
	public View getView(int position, View v, ViewGroup parent) {
		if(v ==null)
			v = mInflater.inflate(R.layout.searchresults, null);
		CardData data = mDataList.get(position);
		v.setId(position);
		CardDataHolder dataHolder = (CardDataHolder)v.getTag();
		if(dataHolder == null){
			dataHolder = new CardDataHolder();
			dataHolder.mTitle = (TextView)v.findViewById(R.id.title);
			dataHolder.mTitle.setTypeface(FontUtil.Roboto_Medium);
			
			dataHolder.mPreview = (CardImageView)v.findViewById(R.id.thumbnailimage);
		}
		dataHolder.mDataObject = data;
		dataHolder.mPreview.mCardId = position;
		dataHolder.mPreview.mImageUrl = null;
		
		if(data.generalInfo != null && data.generalInfo.title != null){
			dataHolder.mTitle.setText(data.generalInfo.title.toLowerCase());
		}

        dataHolder.mPreview.setImageBitmap(null);
		if(data.images != null){
			for(CardDataImagesItem imageItem:data.images.values){
				if(imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null && imageItem.profile.equalsIgnoreCase(myplexapplication.getApplicationConfig().type)){
					if (imageItem.link != null && !(imageItem.link.compareTo("Images/NoImage.jpg") == 0)) {
						dataHolder.mPreview.mImageUrl = imageItem.link;
						CardImageLoader ImageLoader = new CardImageLoader(position,mContext);
						ImageLoader.loadImage(dataHolder.mPreview);
//						dataHolder.mPreview.setImageUrl(imageItem.link,MyVolley.getImageLoader());
					}
					break;
				}
			}
		}
		return v;
	}
}
