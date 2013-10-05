package com.apalya.myplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Space;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.adapters.CardActionListener;
import com.apalya.myplex.adapters.CardGoogleLayoutAdapater;
import com.apalya.myplex.cache.CacheHolder;
import com.apalya.myplex.cache.InsertionResult;
import com.apalya.myplex.cache.SearchResult;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.listboxanimation.OnDismissCallback;
import com.apalya.myplex.listboxanimation.SwingBottomInAnimationAdapter;
import com.apalya.myplex.listboxanimation.SwipeDismissAdapter;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.CardView;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class CardExplorer extends BaseFragment implements CardActionListener,
		OnDismissCallback {
	public static final String TAG = "CardExplorer";
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
	public void open(CardData object) {
		BaseFragment fragment = mMainActivity.createFragment(MainActivity.CARDDETAILS);
		fragment.setDataObject(object);
		mMainActivity.bringFragment(fragment);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	public void showProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(getContext(), "", "Loading...",true, false);
	}

	public void dismissProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	public void updateText(final String str) {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null) {
					mProgressDialog.setTitle(str);
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mRootView != null) {
			show();
			return mRootView;
		}
		mRootView = inflater.inflate(R.layout.cardbrowsing, container, false);
		mCardView = (CardView) mRootView.findViewById(R.id.framelayout);
		mGoogleCardListView = (ListView) mRootView.findViewById(R.id.cardlistview);
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
		mMainActivity.setPotrait();
		delayedAction();
		return mRootView;
	}

	public void delayedAction() {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				showProgressBar();
//				prepareDummyData();
				loadPage();
			}
		});
	}

	@Override
	public void setActionBarHeight(int height) {
		super.setActionBarHeight(height);
		mCardView.setActionBarHeight(height);
	}

	private void show() {
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

	private void loadPage() {
		mMainActivity.showActionBarProgressBar();
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(ConsumerApi.getSearch("*",ConsumerApi.LEVELMIN,mStartIndex), deviceMinSuccessListener(), responseErrorListener());
		myReg.setShouldCache(true);
		Log.d(TAG,"Min Request:"+ConsumerApi.getSearch("*",ConsumerApi.LEVELMIN,mStartIndex));
		queue.add(myReg);
	}
	private void cacheValidation(final CardResponseData minResultSet) {
		//
		cache.GetData(minResultSet.results, new SearchResult() {
			
			@Override
			public void searchComplete(HashMap<String, Object> resultMap) {
				if(resultMap == null){}
				Set<String> keySet = resultMap.keySet(); 
				for(String key: keySet){
					CardData data = (CardData) resultMap.get(key);
					mEntries.add(data);
					mMasterEntries.add(data);
				}
				String missingCardId = new String();
				for(CardData data: minResultSet.results){
					if(!keySet.contains(data._id)){
						if(missingCardId.length() > 0){
							missingCardId += ",";
						}
						missingCardId += data._id;
					}
				}
				if(missingCardId.length() >0){
					loadMissingData(missingCardId);
				}else{
					applyData();
				}
			}
		});
	}
	
	
	private Response.Listener<String> deviceMinSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					updateText("parsing results");
					CardResponseData minResultSet  =(CardResponseData) Util.fromJson(response, CardResponseData.class);
					cacheValidation(minResultSet);
//					applyData();
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	private CacheHolder cache = new CacheHolder();
	private void addToCache(final CardResponseData minResultSet){
		
		cache.UpdataDataAsync(minResultSet.results, new InsertionResult() {
			
			@Override
			public void updateComplete(Boolean updateStatus) {
				// TODO Auto-generated method stub
				
			}
		});
		for(CardData data:minResultSet.results){
			mEntries.add(data);
			mMasterEntries.add(data);
		}
		applyData();
	}
	private Response.Listener<String> deviceMaxSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					updateText("parsing results");
					CardResponseData maxResultSet  = (CardResponseData) Util.fromJson(response, CardResponseData.class);
					addToCache(maxResultSet);
//					applyData();
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	private void loadMissingData(String missingCardIds) {
		mMainActivity.showActionBarProgressBar();
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(ConsumerApi.getContentDetail(missingCardIds,ConsumerApi.LEVELDEVICEMAX), deviceMaxSuccessListener(), responseErrorListener());
		myReg.setShouldCache(true);
		Log.d(TAG,"MAX Request:"+ConsumerApi.getContentDetail(missingCardIds,ConsumerApi.LEVELDEVICEMAX));
		queue.add(myReg);
	}
	
	public void prepareDummyData(){
//		for(int i = 0; i <15;i++){
//			CardData data = new CardData();
//			data.title = "Movie "+i;
//			data.imageUrl = URLS[i];
//			mEntries.add(data);
//			mMasterEntries.add(data);
//		}
//		applyData();
	}
	
	private void prepareFilterData() {
//		List<FilterMenudata> filteroptions = new ArrayList<FilterMenudata>();
//		List<String> tempList = new ArrayList<String>();
//		filteroptions.add(new FilterMenudata(FilterMenudata.SECTION, "ALL", 1));
//		for (CardData data : mMasterEntries) {
//			if (data.filterName != null && !tempList.contains(data.filterName)) {
//				filteroptions.add(new FilterMenudata(FilterMenudata.SECTION,
//						data.filterName, 1));
//				tempList.add(data.filterName);
//			}
//		}
//		mMainActivity.addFilterData(filteroptions, mFilterMenuClickListener);
	}

	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
//			if (v.getTag() instanceof FilterMenudata) {
//				String label = ((FilterMenudata) v.getTag()).label;
//				if (label != null && label.equalsIgnoreCase("ALL")) {
//					sort(mMasterEntries);
//					return;
//				}
//				ArrayList<CardData> localData = new ArrayList<CardData>();
//				for (CardData data : mEntries) {
//					if (data.filterName != null
//							&& data.filterName.equalsIgnoreCase(label)) {
//						localData.add(data);
//					}
//				}
//				sort(localData);
//			}
		}
	};

	private void sort(ArrayList<CardData> localData) {
		if (displayMode == STACKVIEW) {
			mCardView.forceUpdateData(localData);
		} else {
			mGoogleCardListViewAdapter.forceUpdateData(localData);
		}
	}

	private void applyData() {
//		if (mMasterEntries.size() == 0) {
//			showProgressBar();
//			loadPage();
//		}
		updateText("preparing ui");
		if (displayMode == STACKVIEW) {
			mCardView.addData(mMasterEntries);
			mCardView.show();
		} else {
			mGoogleCardListViewAdapter.setData(mMasterEntries);
		}
		prepareFilterData();
		dismissProgressBar();
		mMainActivity.hideActionBarProgressBar();
	}

	private void showErrorDialog() {
		mInError = true;
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setMessage("No Response from server");
		b.show();
	}

	private Response.ErrorListener responseErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				showErrorDialog();
				mMainActivity.hideActionBarProgressBar();
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
//				mCardData.applyFavoriteInProgress = false;
				// mCardData.isFavorite = true;
				Handler h = new Handler(Looper.getMainLooper());
				h.post(new Runnable() {

					@Override
					public void run() {
//						mCardView.updateData(mCardData);
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
	public void purchase(CardData data) {
		showPackDialog(data);
	}
	private PopupWindow mFilterMenuPopupWindow = null;
	private List<PopupWindow> mFilterMenuPopupWindowList = new ArrayList<PopupWindow>();
	
	private void dismissFilterMenuPopupWindow() {
		if (mFilterMenuPopupWindow != null) {
			mFilterMenuPopupWindowList.remove(mFilterMenuPopupWindow);
			mFilterMenuPopupWindow.dismiss();
			mFilterMenuPopupWindow = null;
		}
	}

	private void showPopup(View v) {
		dismissFilterMenuPopupWindow();
		mFilterMenuPopupWindow = new PopupWindow(v,LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		mFilterMenuPopupWindowList.add(mFilterMenuPopupWindow);
		mFilterMenuPopupWindow.setOutsideTouchable(true);
		mFilterMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mFilterMenuPopupWindow.showAsDropDown(getActionBar().getCustomView());
	}
	public void showPackDialog(CardData data) {
//		CustomDialog dialog = new CustomDialog(getContext());
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View v = inflater.inflate(R.layout.purchasepopup,null);
		LinearLayout susblayout = (LinearLayout) v.findViewById(R.id.purchasepopup_packslayout);
		addPack(data,susblayout);
		showPopup(v);
//		dialog.show();
	}

	private void addSpace(ViewGroup v,int space){
		Space gap = new Space(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,space);
		gap.setLayoutParams(params);
		v.addView(gap);
	}
	private void addPack(CardData data,LinearLayout parentlayout) {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		for (int i = 0; i < 4; i++) {
			View v = inflater.inflate(R.layout.purchasepackitem, null);
			addSpace(parentlayout, 15);
			parentlayout.addView(v);
		}
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		// TODO Auto-generated method stub

	}

	private static final String[] URLS = {
			"https://lh6.googleusercontent.com/-HEeoO3k3bPg/S0VKWAJUlbI/AAAAAAAAAik/k1x42L8UIvw/Movie-GhostRider-001.jpg",
			"https://lh4.googleusercontent.com/-16Op5dZqK4s/STQf00CgLaI/AAAAAAAAAS4/y94XF3tvI2o/Blog1000-Which-way-india-stn.jpg",
			"https://lh3.googleusercontent.com/-yqLKT4RAfBM/S32v0NNVTbI/AAAAAAAAKyw/2ggyry4KiCE/Nature%252520Wallpapers%252520%25252880%252529.jpg",
			"https://lh5.googleusercontent.com/-d-qS8knzDP4/SePRfjfPYhI/AAAAAAAACVA/jxox5vRCphw/IMG_0084.jpg",
			"https://lh5.googleusercontent.com/-eurfd_3DDJM/SYpR7j0o8CI/AAAAAAAAJ8k/XRRlN8bdQlA/DSCF3739r.jpg",
			"https://lh5.googleusercontent.com/-K0Weq3ovQ2Y/SPrz1dedUWI/AAAAAAAAARc/1-fuKwsJPHs/IMG_4450.JPG",
			"https://lh3.googleusercontent.com/-RgQYezPeGck/SMC0KWFderI/AAAAAAAAE-w/Tm0JFwb-1Yc/100_5136.jpg",
			"https://lh3.googleusercontent.com/-sjtaMlX_2Qo/Sen1maYoUeI/AAAAAAAABds/8ABF3laHiqg/CA-wp6.jpg",
			"https://lh6.googleusercontent.com/-V4-QM6drP5c/SA66lIlP2cI/AAAAAAAAAIA/SjF8lVpf5hI/Denali-Dance-Framed.jpg",
			"http://lh5.ggpht.com/_mrb7w4gF8Ds/TCpetKSqM1I/AAAAAAAAD2c/Qef6Gsqf12Y/s144-c/_DSC4374%20copy.jpg",
			"http://lh5.ggpht.com/_Z6tbBnE-swM/TB0CryLkiLI/AAAAAAAAVSo/n6B78hsDUz4/s144-c/_DSC3454.jpg",
			"http://lh3.ggpht.com/_GEnSvSHk4iE/TDSfmyCfn0I/AAAAAAAAF8Y/cqmhEoxbwys/s144-c/_MG_3675.jpg",
			"http://lh6.ggpht.com/_Nsxc889y6hY/TBp7jfx-cgI/AAAAAAAAHAg/Rr7jX44r2Gc/s144-c/IMGP9775a.jpg",
			"http://lh3.ggpht.com/_lLj6go_T1CQ/TCD8PW09KBI/AAAAAAAAQdc/AqmOJ7eg5ig/s144-c/Juvenile%20Gannet%20despute.jpg",
			"http://lh6.ggpht.com/_ZN5zQnkI67I/TCFFZaJHDnI/AAAAAAAABVk/YoUbDQHJRdo/s144-c/P9250508.JPG",
			"http://lh4.ggpht.com/_XjNwVI0kmW8/TCOwNtzGheI/AAAAAAAAC84/SxFJhG7Scgo/s144-c/0014.jpg",
			"http://lh6.ggpht.com/_lnDTHoDrJ_Y/TBvKsJ9qHtI/AAAAAAAAG6g/Zll2zGvrm9c/s144-c/000007.JPG",
			"http://lh6.ggpht.com/_qvCl2efjxy0/TCIVI-TkuGI/AAAAAAAAOUY/vbk9MURsv48/s144-c/DSC_0844.JPG",
			"http://lh4.ggpht.com/_TPlturzdSE8/TBv4ugH60PI/AAAAAAAAMsI/p2pqG85Ghhs/s144-c/_MG_3963.jpg",
			"http://lh4.ggpht.com/_4f1e_yo-zMQ/TCe5h9yN-TI/AAAAAAAAXqs/8X2fIjtKjmw/s144-c/IMG_1786.JPG",
			"http://lh6.ggpht.com/_iFt5VZDjxkY/TB9rQyWnJ4I/AAAAAAAADpU/lP2iStizJz0/s144-c/DSCF1014.JPG", };
}
