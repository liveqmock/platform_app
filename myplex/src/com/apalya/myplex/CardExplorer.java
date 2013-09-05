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
import com.apalya.myplex.data.slidemenudata;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.views.CardView;
import com.apalya.myplex.views.FliterMenu;
import com.apalya.myplex.views.NewCardView;
import com.apalya.myplex.views.NewCardView.OnPlayListener;
import com.apalya.myplex.views.slidemenuadapter;

public class CardExplorer extends BaseActivity implements OnPlayListener{
	private NewCardView mCardView;
	private RelativeLayout mSlideNotificationLayout;
	private RelativeLayout mCardDetailsView;
	private static final int RESULTS_PAGE_SIZE = 20;
	private boolean mHasData = false;
	private boolean mInError = false;
	private ArrayList<CardData> mEntries = new ArrayList<CardData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.cardbrowsing);
		super.onCreate(savedInstanceState);
		
		
		prepareSlideNotifiation();
//		prepareFilterMenu();
		mCardView = (NewCardView) findViewById(R.id.framelayout);
		mCardDetailsView = (RelativeLayout)findViewById(R.id.carddetailsview);
		// mCardView.setActionBarHeight(actionBar.getHeight());
		mCardView.setContext(this);
		mCardView.setOnPlayListener(this);
		// prepareData();
		// this.setProgressBarIndeterminate(true);

		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				prepareData(50);

			}
		}, 1000);
//		enableDownFilter();
		// mCardView.show();
	}
	
//	private void prepareFilterMenu(){
//		
//		mFilterMenuToggleButton = (ImageView)getActionBar().getCustomView().findViewById(R.id.fliterinmenu);
//		mFilterMenuToggleButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if(!mFilterMenuToggle){
//					mFilterMenuToggleButton.setImageResource(R.drawable.navigation_collapse);
//					mFliterMenu.show();
//				}else{
//					mFilterMenuToggleButton.setImageResource(R.drawable.navigation_expand);
//					mFliterMenu.hide();
//				}
//				mFilterMenuToggle = !mFilterMenuToggle;
//			}
//		});
//		mFilterMenuCloseButton = (ImageView)findViewById(R.id.filtermenuclose);
//		mFilterMenuCloseButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				mFliterMenu.hide();
//				mFilterMenuToggle = false;
//			}
//		});
//		;
//		mFliterMenu = (FliterMenu)findViewById(R.id.flitermenulayout);
//		mFliterMenu.init(this);
//		
//		mFliterListView = (ListView)findViewById(R.id.fliterlistbox);
//		mFliterMenuAdapter = new slidemenuadapter(this);
//		mFliterListView.setAdapter(mFliterMenuAdapter);
//		fillFilterMenuData();
//	}
//	private void fillFilterMenuData(){
//		List<slidemenudata> data = new ArrayList<slidemenudata>();
//		data.add(new slidemenudata("Recommended", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Movies", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Tv Shows", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Special Programming", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Popular", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Trending", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Recent", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("New", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Sports", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Spotlight", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Featured", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Beauty & Fashion", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Science & Education", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Cooking & Health", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("News & Politics", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Lifestyle", "", R.drawable.social_send_now));
//		data.add(new slidemenudata("Purchase", "", R.drawable.social_send_now));
//		mFliterMenuAdapter.setData(data);
//	}
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
	

	
	
	private ImageView mFilterMenuToggleButton;
	private ImageView mFilterMenuCloseButton;
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if (!mHasData && !mInError) {
//			 loadPage();
		}
	}

	private void loadPage() {
		RequestQueue queue = MyVolley.getRequestQueue();

		int startIndex = 1 + mEntries.size();
		JsonObjectRequest myReq = new JsonObjectRequest(Method.GET,
				"https://picasaweb.google.com/data/feed/api/all?q=movie&max-results="
						+ RESULTS_PAGE_SIZE + "&thumbsize=160&alt=json"
						+ "&start-index=" + startIndex, null,
				createMyReqSuccessListener(), createMyReqErrorListener());

		queue.add(myReq);
	}

	private Response.Listener<JSONObject> createMyReqSuccessListener() {
		return new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject feed = response.getJSONObject("feed");
					JSONArray entries = feed.getJSONArray("entry");
					JSONObject entry;
					for (int i = 0; i < entries.length(); i++) {
						entry = entries.getJSONObject(i);

						String url = null;

						JSONObject media = entry.getJSONObject("media$group");
						if (media != null && media.has("media$thumbnail")) {
							JSONArray thumbs = media
									.getJSONArray("media$thumbnail");
							if (thumbs != null && thumbs.length() > 0) {
								url = thumbs.getJSONObject(0).getString("url");
							}
						}

						mEntries.add(new CardData(entry.getJSONObject("title")
								.getString("$t"), url, 0));
					}
					mCardView.addData(mEntries);
					 mCardView.show();
				} catch (JSONException e) {
					showErrorDialog();
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
				showErrorDialog();
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
	private RelativeLayout mCardDetailPreviewLayoutLarge;
	private RelativeLayout mCardDetailPreviewLayoutSmall;
	private ImageView mCardDetailPreviewImageLarge;
	private ImageView mCardDetailPreviewImageSmall;
	@Override
	public void play(CardData data) {
		
		mCardDetailPreviewLayoutLarge = (RelativeLayout)findViewById(R.id.card_preview_large_layout);
		mCardDetailPreviewLayoutSmall = (RelativeLayout)findViewById(R.id.card_preview_small_layout);
		mCardDetailPreviewImageLarge = (ImageView)findViewById(R.id.card_preview_large_image);
		mCardDetailPreviewImageSmall = (ImageView)findViewById(R.id.card_preview_small_image);
		
//		Toast.makeText(this, data.title, Toast.LENGTH_SHORT).show();
		mCardDetailPreviewLayoutLarge.setVisibility(View.INVISIBLE);
		mCardDetailPreviewImageLarge.setVisibility(View.INVISIBLE);
		
		mCardDetailPreviewLayoutSmall.setVisibility(View.VISIBLE);
		mCardDetailPreviewImageSmall.setVisibility(View.VISIBLE);
		
		mCardDetailPreviewImageSmall.setImageResource(data.resId);
		mCardDetailPreviewImageLarge.setImageResource(data.resId);
		
		mCardDetailsView.setVisibility(View.VISIBLE);
		fadeCardView();
		zoomImageFromThumb(mCardDetailPreviewImageSmall,data.resId);
//		rotate();
//		startActivity(new Intent(CardExplorer.this,CardDetails.class));
	}
	@Override
	public void onBackPressed() {
		if(mCardDetailsView.getVisibility() == View.VISIBLE){
			mCardDetailsView.setVisibility(View.GONE);
			mCardView.setVisibility(View.VISIBLE);
			AnimatorSet set = new AnimatorSet();
			set.play(ObjectAnimator.ofFloat(mCardView, View.ALPHA, 0, 1));
			set.setDuration(1000);
			set.setInterpolator(new DecelerateInterpolator());
			set.start();
		}else{
			super.onBackPressed();
		}
	}
	private void zoomImageFromThumb(final View thumbView, int imageResId) {
	    // If there's an animation in progress, cancel it
	    // immediately and proceed with this one.
	    if (mCurrentAnimator != null) {
	        mCurrentAnimator.cancel();
	    }

	    // Load the high-resolution "zoomed-in" image.
	    mCardDetailPreviewImageLarge.setImageResource(imageResId);
	    Rect rect = new Rect();
	    thumbView.getGlobalVisibleRect(rect);
	    mCardDetailPreviewLayoutSmall.getGlobalVisibleRect(rect);
	    thumbView.setAlpha(0f);
	    mCardDetailPreviewImageLarge.setVisibility(View.VISIBLE);

	    // Set the pivot point for SCALE_X and SCALE_Y transformations
	    // to the top-left corner of the zoomed-in view (the default
	    // is the center of the view).
//	    mCardDetailPreviewImageLarge.setPivotX(0f);
//	    mCardDetailPreviewImageLarge.setPivotY(0f);

	    // Construct and run the parallel animation of the four translation and
	    // scale properties (X, Y, SCALE_X, and SCALE_Y).
//	    AnimatorSet set = new AnimatorSet();
//	    set
//	            .play(ObjectAnimator.ofFloat(mCardDetailPreviewImageLarge, View.X,
//	                    startBounds.left, finalBounds.left))
//	            .with(ObjectAnimator.ofFloat(mCardDetailPreviewImageLarge, View.Y,
//	                    startBounds.top, finalBounds.top))
//	            .with(ObjectAnimator.ofFloat(mCardDetailPreviewImageLarge, View.SCALE_X,
//	            startScale, 1f)).with(ObjectAnimator.ofFloat(mCardDetailPreviewImageLarge,
//	                    View.SCALE_Y, startScale, 1f));
//	    set.setDuration(2000);
//	    set.setInterpolator(new DecelerateInterpolator());
//	    set.addListener(new AnimatorListenerAdapter() {
//	        @Override
//	        public void onAnimationEnd(Animator animation) {
//	            mCurrentAnimator = null;
//	        }
//
//	        @Override
//	        public void onAnimationCancel(Animator animation) {
//	            mCurrentAnimator = null;
//	        }
//	    });
//	    set.start();
//	    mCurrentAnimator = set;

	}
	private void fadeCardView(){
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
				mCardView.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
			}
		});
		set.start();
	}
}
