package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;

import com.apalya.myplex.data.slidemenudata;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.views.FliterMenu;
import com.apalya.myplex.views.slidemenuadapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class BaseActivity extends Activity {
	private boolean mSlideInMenuToggle = false;
	private boolean mFilterMenuToggle,mSearchToggle = false;
	private slidemenuadapter mSlidemenuAdapter;
	private int mSlideInMenuWidth;
	private RelativeLayout mSlideMenuLayout;
	private ListView mSlideMenuList;
	private View mContentView;
	private RelativeLayout mRightFilterLayout,mDownFilterLayout;
	private TextView mRightFilterTitle,mDownFilterTitle;
	private ImageView mRightFilterButton,mDownFilterButton;
	
	private FliterMenu mFliterMenu;
	private ListView mFliterListView;
	private slidemenuadapter mFliterMenuAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!FontUtil.isFontsLoaded) {
			FontUtil.loadFonts(getAssets());
		}
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.applicationtitle);
		ImageView button = (ImageView) getActionBar().getCustomView().findViewById(R.id.menu_settings);
		mRightFilterLayout = (RelativeLayout)getActionBar().getCustomView().findViewById(R.id.title_withfilterright);
		mDownFilterLayout = (RelativeLayout)getActionBar().getCustomView().findViewById(R.id.title_withfilterdown);
		mRightFilterTitle = (TextView)getActionBar().getCustomView().findViewById(R.id.applicationtitle_right);
		mDownFilterTitle = (TextView)getActionBar().getCustomView().findViewById(R.id.applicationtitle_down);
		mRightFilterButton = (ImageView)getActionBar().getCustomView().findViewById(R.id.menu_fliter_right);
		mDownFilterButton = (ImageView)getActionBar().getCustomView().findViewById(R.id.menu_fliter_down);
		
		ImageView searchButton = (ImageView)getActionBar().getCustomView().findViewById(R.id.menu_search);
		
		mRightFilterTitle.setTypeface(FontUtil.Roboto_Medium);
		mDownFilterTitle.setTypeface(FontUtil.Roboto_Medium);
		
		mDownFilterButton.setOnClickListener(mFliterClickListener);
		mRightFilterButton.setOnClickListener(mFliterClickListener);
		
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!mSlideInMenuToggle) {
					showMenu();
				} else {
					hideMenu();
				}
				mSlideInMenuToggle = !mSlideInMenuToggle;

			}
		});
		
		searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(!mSearchToggle)
				{
					mSearchToggle = true;
					Intent searchIntent = new Intent(BaseActivity.this, SearchActivity.class);
					startActivity(searchIntent);
				}
			}
		});
		
		prepareSlideMenu();
		enableSideFilter();
	}
	public OnClickListener mFliterClickListener =  new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(!mFilterMenuToggle){
				mRightFilterButton.setImageResource(R.drawable.navigation_collapse);
				mDownFilterButton.setImageResource(R.drawable.navigation_collapse);
				mFliterMenu.show();
			}else{
				mRightFilterButton.setImageResource(R.drawable.navigation_expand);
				mDownFilterButton.setImageResource(R.drawable.navigation_expand);
				mFliterMenu.hide();
			}
			mFilterMenuToggle = !mFilterMenuToggle;		
		}
	};
	public void enableSideFilter(){
		mRightFilterLayout.setVisibility(View.VISIBLE);
		mDownFilterLayout.setVisibility(View.GONE);
	}
	public void enableDownFilter(){
		mRightFilterLayout.setVisibility(View.GONE);
		mDownFilterLayout.setVisibility(View.VISIBLE);
	}

	private void prepareSlideMenu() {
		mSlideInMenuWidth = (int) getResources().getDimension(R.dimen.slidemenugap);
		mSlideMenuLayout = (RelativeLayout) findViewById(R.id.slideinmenulayout);
		if(mSlideMenuLayout != null){
			mSlideMenuList = (ListView) findViewById(R.id.slideinmenulistbox);
			mSlidemenuAdapter = new slidemenuadapter(this);
			mSlideMenuList.setAdapter(mSlidemenuAdapter);
			mSlideMenuList.setOnItemClickListener(new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					mSlideInMenuToggle = false;
					hideMenu();
				}
	
			});
			mSlideMenuLayout.setX(-mSlideInMenuWidth);
		}
		// fillSlideMenuData();
	}
	private void fillSlideMenuData(){
		List<slidemenudata> data = new ArrayList<slidemenudata>();
		data.add(new slidemenudata("Recommended", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Movies", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Tv Shows", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Special Programming", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Popular", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Trending", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Recent", "", R.drawable.social_send_now));
		data.add(new slidemenudata("New", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Sports", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Spotlight", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Featured", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Beauty & Fashion", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Science & Education", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Cooking & Health", "", R.drawable.social_send_now));
		data.add(new slidemenudata("News & Politics", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Lifestyle", "", R.drawable.social_send_now));
		data.add(new slidemenudata("Purchase", "", R.drawable.social_send_now));
		mSlidemenuAdapter.setData(data);
	}
	protected void animate(float fromX, float toX, final View v,final boolean showlist,int animationType) {
		if (v == null) {
			return;
		}
		AnimatorSet set = new AnimatorSet();
		if(animationType == 1){
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_X, fromX, toX));
		}else{
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromX, toX));
		}
		set.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
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
				if(showlist){
					fillSlideMenuData();
				}
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		set.start();
	}
	public void setContentView(View v){
		mContentView = v;
	}
	private void hideMenu() {
		// Toast.makeText(this, "slide out", Toast.LENGTH_SHORT).show();
		animate(0, -mSlideInMenuWidth, mSlideMenuLayout, false, 1);
		animate(mSlideInMenuWidth, 0, mContentView, false, 1);
		List<slidemenudata> data = new ArrayList<slidemenudata>();
		mSlidemenuAdapter.setData(data);
	}

	private void showMenu() {
		// Toast.makeText(this, "slide in", Toast.LENGTH_SHORT).show();
		animate(-mSlideInMenuWidth, 0, mSlideMenuLayout, true, 1);
		animate(0, mSlideInMenuWidth, mContentView, false, 1);

	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(mSearchToggle)
			mSearchToggle = false;
	}

}
