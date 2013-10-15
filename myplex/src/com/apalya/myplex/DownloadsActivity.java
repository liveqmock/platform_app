package com.apalya.myplex;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.adapters.PicasaArrayAdapter;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataVideosItem;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.DownloadDetails;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.PicasaEntry;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;




import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class DownloadsActivity extends BaseFragment implements CacheManagerCallback{
	private ListView mLvDownloads;
	private View rootView;
	private boolean mHasData = false;
	private boolean mInError = false;
	private ArrayList<PicasaEntry> mListEntries = new ArrayList<PicasaEntry>();
	private PicasaArrayAdapter mAdapter;
	public  HashMap<String,CardData> mEntries = new HashMap<String,CardData>();
	public ArrayList<CardData> mMasterEntries = new ArrayList<CardData>();
	int startIndex = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		rootView = inflater.inflate(R.layout.listviewholder, container, false);

		mLvDownloads = (ListView) rootView.findViewById(R.id.lv_picasa);
		mAdapter = new PicasaArrayAdapter(getActivity(), 0, mListEntries, MyVolley.getImageLoader());
		mLvDownloads.setAdapter(mAdapter);
		mLvDownloads.setOnScrollListener(new EndlessScrollListener());
		
		

		return rootView;
	}



	@Override
	public void onResume() {
		super.onResume();

		/*if (!mHasData && !mInError) {
            loadPage();
           
        }*/
		loadLocalData();
	}
	private void loadLocalData(){

		//182,201,203
		ArrayList<CardData> downloadCardIds = new ArrayList<CardData>();
		CardData cardId1= new CardData();
		cardId1._id="193";
		CardData cardId2= new CardData();
		cardId2._id="201";
		CardData cardId3= new CardData();
		cardId3._id="203";
		downloadCardIds.add(cardId1);
		downloadCardIds.add(cardId2);
		downloadCardIds.add(cardId3);
		
		CacheManager mCacheManager=new CacheManager();
		mCacheManager.getCardDetails(downloadCardIds,IndexHandler.OperationType.IDSEARCH,DownloadsActivity.this);
		
		/*List<String> cardIds = SharedPrefUtils.readList(mMainActivity, "cardids");
		List<String> cardImgs = SharedPrefUtils.readList(mMainActivity, "cardimgs");


		for(int i=0;i<cardIds.size()&& i<cardImgs.size();i++)
		{
			mEntries.add(new PicasaEntry(cardIds.get(i), cardImgs.get(i)));

		}
		mAdapter.notifyDataSetChanged();
		List<String> downloads = SharedPrefUtils.readList(mMainActivity, "downloads");
		 for(int i=0;i<downloads.size();i++)
		{
			showProgress(i, Long.parseLong(downloads.get(i)));
		}
		prepareFilterData();
		
		if(cardIds.size()==0)
		{
			Util.showToast("No Downloads", mMainActivity);
		}*/
		
	}

	private void loadPage() {
		RequestQueue queue = MyVolley.getRequestQueue();



		String requestUrl = new String();
		requestUrl = ConsumerApi.getFavourites(ConsumerApi.LEVELDEVICEMAX,startIndex);
		StringRequest myReq = new StringRequest(requestUrl, createMyReqSuccessListener(), createMyReqErrorListener());


		myReq.setShouldCache(true);

		queue.add(myReq);
	}


	private Response.Listener<String> createMyReqSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					/*JSONObject feed = response.getJSONObject("feed");
                    JSONArray entries = feed.getJSONArray("entry");
                    JSONObject entry;
                    for (int i = 0; i < entries.length(); i++) {
                        entry = entries.getJSONObject(i);

                        String url = null;

                        JSONObject media = entry.getJSONObject("media$group");
                        if (media != null && media.has("media$thumbnail")) {
                            JSONArray thumbs = media.getJSONArray("media$thumbnail");
                            if (thumbs != null && thumbs.length() > 0) {
                                url = thumbs.getJSONObject(0).getString("url");
                            }
                        }

                        mEntries.add(new PicasaEntry(entry.getJSONObject("title").getString("$t"), url));
                    }*/

					CardResponseData minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
					if(minResultSet.code != 200){
						Toast.makeText(getContext(), minResultSet.message, Toast.LENGTH_SHORT).show();
					}

					if(minResultSet.results ==  null){Toast.makeText(getContext(), "No Data", Toast.LENGTH_SHORT).show();;return;}
					if(minResultSet.results.size() ==  0){Toast.makeText(getContext(), "No Data", Toast.LENGTH_SHORT).show();;return;}
					//mCacheManager.getCardDetails(minResultSet.results,IndexHandler.OperationType.IDSEARCH,ProfileActivity.this);

					for(CardData data:minResultSet.results){
						String title=data.generalInfo.title;
						String url="";
						for(CardDataImagesItem images:data.images.values){
							if(images.profile != null && images.profile.equalsIgnoreCase("ldpi") && images.link != null){
								url=images.link;
							}
						}
						//mEntries.add(new PicasaEntry(title, url));	
					}

					/*if(minResultSet.results != null){
						Toast.makeText(getContext(), "No data", Toast.LENGTH_SHORT).show();
						int count=minResultSet.results.size();
						for(int i=0;i<count;i++)
						{
							String title=minResultSet.results.get(i).generalInfo.title;
							String url="";
							int imagesCount =minResultSet.results.get(i).images.values.size();
							for(int j=0;j<imagesCount;j++)
							{
								url=minResultSet.results.get(i).images.values.get(j).link;
							}
							mEntries.add(new PicasaEntry(title, url));	
						}

					}*/


					mAdapter.notifyDataSetChanged();
				}  catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}


	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				showErrorDialog();
			}
		};
	}


	private void showErrorDialog() {
		mInError = true;

		AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
		b.setMessage("Please check your Internet Connection and try again!");
		b.show();
	}

	private void prepareFilterData()
	{
		List<FilterMenudata> profileFilter = new ArrayList<FilterMenudata>();
		profileFilter.add(new FilterMenudata(FilterMenudata.ITEM, "All", 0));
		profileFilter.add(new FilterMenudata(FilterMenudata.ITEM, "Completed", 1));
		profileFilter.add(new FilterMenudata(FilterMenudata.ITEM, "Inprogress", 2));
		mMainActivity.addFilterData(profileFilter,mFilterMenuClickListener);

	}

	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof FilterMenudata) {
				String label = ((FilterMenudata) v.getTag()).label;

			}
		}
	};

	/**
	 * Detects when user is close to the end of the current page and starts loading the next page
	 * so the user will not have to wait (that much) for the next entries.
	 * 
	 * @author Ognyan Bankov (ognyan.bankov@bulpros.com)
	 */
	public class EndlessScrollListener implements OnScrollListener {
		// how many entries earlier to start loading next page
		private int visibleThreshold = 5;
		private int currentPage = 0;
		private int previousTotal = 0;
		private boolean loading = true;

		public EndlessScrollListener() {
		}
		public EndlessScrollListener(int visibleThreshold) {
			this.visibleThreshold = visibleThreshold;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (loading) {
				if (totalItemCount > previousTotal) {
					loading = false;
					previousTotal = totalItemCount;
					currentPage++;
				}
			}
			if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
				// I load the next page of gigs using a background task,
				// but you can call any function here.
				startIndex+=mEntries.size();
				// loadPage();
				// loading = true;
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}


		public int getCurrentPage() {
			return currentPage;
		}
	}
	private void updateStatus(int index,final int Status){

		View v = mLvDownloads.getChildAt(index - mLvDownloads.getFirstVisiblePosition());
		if(v!=null)
		{
			// Update ProgressBar
			final ProgressBar progress = (ProgressBar)v.findViewById(R.id.progressBar1);
			progress.setProgress(Status);
			final TextView per= (TextView)v.findViewById(R.id.percentage);
			
			/*if(per!=null)
				per.setText(String.valueOf(Status));*/
			
			mMainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                	if(Status>=100)
        			{
        				
                		progress.setVisibility(View.GONE);
                		per.setText("Downloaded");
        			}
                	else
                	{
                		per.setText("Downloading... "+String.valueOf(Status));
                	}


                }
            });
			
			
			
		}
	}
	private void showProgress(final int index,final long dwnlId){
		final DownloadManager manager = (DownloadManager) mMainActivity.getSystemService(Context.DOWNLOAD_SERVICE);

		new Thread(new Runnable() {

			@Override
			public void run() {

				boolean downloading = true;

				while (downloading) {

					DownloadManager.Query q = new DownloadManager.Query();
					q.setFilterById(dwnlId);

					Cursor cursor = manager.query(q);
					cursor.moveToFirst();
					int bytes_downloaded = cursor.getInt(cursor
							.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
					int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

					if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
						downloading = false;
						updateStatus(index,100);
					}
					else{
						final int dl_progress = (bytes_downloaded * 100) / bytes_total;
						updateStatus(index,dl_progress);
					}
					//final double dl_progress = (bytes_downloaded / bytes_total) * 100;
					

					/*mProgressBar.setProgress((int) dl_progress);                    mMainActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            mProgressBar.setProgress((int) dl_progress);
                           percentage.setText(String.valueOf((int) dl_progress));


                        }
                    });*/

					//Log.d("DOWNLOADS", statusMessage(cursor));
					cursor.close();
				}

			}
		}).start();
	}
	@Override
	public void OnCacheResults(HashMap<String, CardData> object) {
		if(object == null){
			Util.showToast("No Downloads", mMainActivity);
			return;
		}
		
		Set<String> keySet = object.keySet();
			
		for(String key:keySet){
//			mData.mEntries.add(object.get(key));
			if(mEntries.get(key) == null){
				mEntries.put(key,object.get(key));
				mMasterEntries.add(object.get(key));
			}
		}
		
		if(mMasterEntries.size() == 0){
			return;
		}
		
		for (CardData data : mMasterEntries) 
		{
			String imgLink=null;
			for(CardDataImagesItem imageItem: data.images.values)
			{
				if(imageItem.type.equalsIgnoreCase("thumbnail"))
				imgLink=imageItem.link;//"http://myplexv2betaimages.s3.amazonaws.com/193/180x320_593621f9-9175-4d3d-95a6-1417249cee0b.jpg";
			}
			mListEntries.add(new PicasaEntry(data.generalInfo.title,imgLink));
		}
		mAdapter.notifyDataSetChanged();
			
		
	}
	@Override
	public void OnOnlineResults(List<CardData> dataList) {
		
		
		for (CardData data : dataList) 
		{
			String imgLink=null;
			for(CardDataImagesItem imageItem: data.images.values)
			{
				if(imageItem.type.equalsIgnoreCase("thumbnail"))
				imgLink=imageItem.link;//"http://myplexv2betaimages.s3.amazonaws.com/193/180x320_593621f9-9175-4d3d-95a6-1417249cee0b.jpg";
			}
			mListEntries.add(new PicasaEntry(data.generalInfo.title,imgLink));
		}
		mAdapter.notifyDataSetChanged();
		
	}
	@Override
	public void OnOnlineError(VolleyError error) {
		// TODO Auto-generated method stub
		
	}
}
