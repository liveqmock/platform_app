package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.ls.LSInput;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport.CrashInfo;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.slidemenudata;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.views.FliterMenu;
import com.apalya.myplex.views.NewCardView;
import com.apalya.myplex.views.NewCardView.OnLoadMoreListener;
import com.apalya.myplex.views.NewCardView.OnPlayListener;
import com.apalya.myplex.views.slidemenuadapter;

public class CardExplorer extends BaseActivity implements OnPlayListener{
	private NewCardView mCardView;
	private RelativeLayout mSlideNotificationLayout;
	private RelativeLayout mCardDetailsView;
	private static final int RESULTS_PAGE_SIZE = 20;
	private boolean mHasData = false;
	private boolean mInError = false;
	private int mStartIndex = 0;
	private ArrayList<CardData> mEntries = new ArrayList<CardData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.cardbrowsing);
		super.onCreate(savedInstanceState);
		
		
		prepareSlideNotifiation();
//		prepareFilterMenu();
		mCardView = (NewCardView) findViewById(R.id.framelayout);
//		mCardDetailsView = (RelativeLayout)findViewById(R.id.carddetailsview);
		// mCardView.setActionBarHeight(actionBar.getHeight());
		mCardView.setContext(this);
		mCardView.setOnPlayListener(this);
		mCardView.setOnLoadMoreListener(new  OnLoadMoreListener() {
			
			@Override
			public void loadmore(int value) {
//				mStartIndex = value;
//				loadPage();
			}
		});
		// prepareData();
		// this.setProgressBarIndeterminate(true);

//		Timer t = new Timer();
//		t.schedule(new TimerTask() {
//
//			@Override
//			public void run() {
//				prepareData(50);
//
//			}
//		}, 1000);
//		fillFilterMenuData();
//		enableDownFilter();
		// mCardView.show();
	}

	private void fillFilterMenuData(){
		List<FilterMenudata> mMenuList = new ArrayList<FilterMenudata>();
		for(int i = 0;i < 50;i++){
			FilterMenudata data;
			if(i % 8 == 0){
				data = new FilterMenudata(FilterMenudata.SECTION, "Parent "+i, i);
			}else{
				data = new FilterMenudata(FilterMenudata.ITEM, "child", i);
			}
			mMenuList.add(data);
		}
		setFilterData(mMenuList);
	}
	@Override
	public void onFilterMenuItemSelected(FilterMenudata data) {
		super.onFilterMenuItemSelected(data);
	}
	private int mSlideNotifcationHeight;
	private TextView mSlideNotificationText;
	private void prepareSlideNotifiation() {
		mSlideNotificationLayout = (RelativeLayout)findViewById(R.id.slidenotificationlayout);
		mSlideNotificationText = (TextView)findViewById(R.id.slidenotificationtextview);
		mSlideNotifcationHeight = (int) getResources().getDimension(R.dimen.slidenotification);
		mSlideNotificationLayout.setY(-mSlideNotifcationHeight);
		mSlideNotificationLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				prepareData(debugTotal);
				hideNotification();				
			}
		});
	}
	private void showNotification(){
		animate(-mSlideNotifcationHeight,0, mSlideNotificationLayout,false,2);
	}
	private void hideNotification(){
		animate(0,-mSlideNotifcationHeight, mSlideNotificationLayout,false,2);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if (!mHasData && !mInError) {
			 loadPage();
		}
	}
/*https://picasaweb.google.com/data/feed/api/all?q=movie&max-results="
+ RESULTS_PAGE_SIZE + "&thumbsize=160&alt=json"
+ "&start-index="*/ 
	private void loadPage() {
		RequestQueue queue = MyVolley.getRequestQueue();
		
		JsonObjectRequest myReq = new JsonObjectRequest(Method.GET,
				"http://106.186.115.151:8888/content/v2/search/?startIndex="+ mStartIndex, null,
				createMyReqSuccessListener(), createMyReqErrorListener());

		Log.d("pref","Request results for "+"http://106.186.115.151:8888/content/v2/search/?startIndex="+ mStartIndex);
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
					mEntries.clear();
					for (int i = 0; i < entries.length(); i++) {
						CardData data = new CardData("", "", 0);
						
						entry = entries.getJSONObject(i);
						String url = null;
						JSONObject content = entry.getJSONObject("content");
						data.title = content.getString("title");
						JSONObject images = entry.getJSONObject("images");
						JSONArray thumbnail  = images.getJSONArray("cover");
						if(thumbnail.length()> 0){
							data.imageUrl = thumbnail.getJSONObject(0).getString("link");	
						}
						
						mEntries.add(data);
					}
					mCardView.addData(mEntries);
					 mCardView.show();
						Log.d("pref","Request found "+mEntries.size());
				} catch (JSONException e) {
					e.printStackTrace();
//					showErrorDialog();
				}
			}
		};
	}

	private void showErrorDialog() {
		mInError = true;
		AlertDialog.Builder b = new AlertDialog.Builder(CardExplorer.this);
		b.setMessage("Error occured");
		b.show();
	}

	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
//				showErrorDialog();
			}
		};
	}
	
	
	private void sendNotification(){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			public void run() {
				mSlideNotificationText.setText(debugTotal+" result available");
				showNotification();
			}
		});
	}
	private void debugDelayResults(){
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				sendNotification();
			}
		}, 2000);
	}
	private int debugTotal;
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.slideinmenu:{
//			showMenu();
//			break;
//		}
//		case R.id.loadmore50: {
//			debugTotal = 50;
//			debugDelayResults();
//			break;
//		}
//		case R.id.loadmore100: {
//			debugTotal = 100;
//			debugDelayResults();
//			break;
//		}
//		case R.id.loadmore500: {
//			debugTotal = 500;
//			debugDelayResults();
//			break;
//		}
//		default:
//			break;
//		}
//		// TODO Auto-generated method stub
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_main, menu);

		ActionBar actionBar = getActionBar();
		mCardView.setActionBarHeight(actionBar.getHeight());
		return true;
	}

	private int count = 0;
	private Rect centerViewBounds = new Rect();
	private Rect leftViewBounds = new Rect();
	private Animator mCurrentAnimator;

	
	private void prepareData(final int total) {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < total; i++) {
					mCardView.addData(new com.apalya.myplex.data.CardData(
							"Movie " + count++, null,
							R.drawable.radiohead_in_rainbows));
				}
				mCardView.show();
				// setProgressBarIndeterminate(false);
			}
		});
	}
	private RelativeLayout mCardDetailPreviewLayout;
	private ImageView mCardDetailPreviewImage;
	private RelativeLayout mCardDetailPreviewTitle;
	private RelativeLayout mCardDetailPreviewBottom;
	private TextView mCardDetailPreviewTitleText;
	
	public interface AnimationComplete{
		public void OnAnimationComplete();
	}
	private void DelayedCallback(final AnimationComplete listener){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(listener != null){
					listener.OnAnimationComplete();
				}
			}
		});
	}
	@Override
	public void play(CardData data) {
		startActivity(new Intent(this,CardDetails.class));
		
//		mCardDetailPreviewLayout = (RelativeLayout)findViewById(R.id.dummy_card_preview);
//		mCardDetailPreviewImage = (ImageView)findViewById(R.id.dummy_card_preview_image);
//		mCardDetailPreviewTitle = (RelativeLayout)findViewById(R.id.dummy_card_title_layout);
//		mCardDetailPreviewTitleText = (TextView)findViewById(R.id.dummy_card_title_name);
//		mCardDetailPreviewBottom = (RelativeLayout)findViewById(R.id.dummy_card_preview_bottom_layout);
//
//		mCardDetailPreviewLayout.setVisibility(View.VISIBLE);
//		mCardDetailPreviewTitle.setVisibility(View.VISIBLE);
//		mCardDetailPreviewBottom.setVisibility(View.VISIBLE);
//		
//		mCardDetailPreviewImage.setImageResource(data.resId);
//		mCardDetailPreviewTitleText.setText(data.title);
//		
//		mCardDetailsView.setVisibility(View.VISIBLE);
//		fadeCardView(mCardView,new  AnimationComplete() {
//			
//			@Override
//			public void OnAnimationComplete() {
////				fadeCardView(mCardDetailPreviewTitle, null);
////				fadeCardView(mCardDetailPreviewBottom, null);
//			}
//		});
		
	}
//	@Override
//	public void onBackPressed() {
//		if(mCardDetailsView.getVisibility() == View.VISIBLE){
//			mCardDetailsView.setVisibility(View.GONE);
//			mCardView.setVisibility(View.VISIBLE);
//			AnimatorSet set = new AnimatorSet();
//			set.play(ObjectAnimator.ofFloat(mCardView, View.ALPHA, 0, 1));
//			set.setDuration(1000);
//			set.setInterpolator(new DecelerateInterpolator());
//			set.start();
//		}else{
//			super.onBackPressed();
//		}
//	}
	private void fadeCardView(final View v,final AnimationComplete listener){
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(mCardView, View.ALPHA, 1, 0));
		set.setDuration(1000);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				v.setVisibility(View.GONE);
				DelayedCallback(listener);
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
			}
		});
		set.start();
	}
}
