package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;



import com.apalya.myplex.adapters.SearchListAdapter;
import com.apalya.myplex.adapters.OpenListener.OpenCallBackListener;
import com.apalya.myplex.data.SearchData;
import com.apalya.myplex.data.SearchData.ButtonData;
import com.apalya.myplex.views.PinnedSectionListView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchActivity extends BaseFragment implements OpenCallBackListener {

	EditText mSearchInput;
	private PinnedSectionListView mPinnedListView;
	private float mButtonApha = 0.25f;
	private SearchListAdapter mAdapter;
	private View rootView; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.searchlayout, container,
					false);
			mSearchInput = (EditText) rootView.findViewById(R.id.inputSearch);
			mSearchInput.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {

						FillEditText(mSearchInput.getText().toString());
						mSearchInput.setText("");
					}
					return false;
				}

				
			});
	        
	        ArrayList<SearchData>  mSearchData=fillSearchData();
			try {
				mPinnedListView=(PinnedSectionListView)rootView. findViewById(R.id.list_view);
				mAdapter = new SearchListAdapter(getContext(), mSearchData);
				mAdapter.setOpenListener(SearchActivity.this);
				mPinnedListView.setAdapter(mAdapter);
				
			} catch (Exception e) { 
				e.printStackTrace();
			}
			mMainActivity.setTitle("Search");
			return rootView;
		}
		private void FillEditText(String buttonName) {
			final LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
			final HorizontalScrollView scroll = (HorizontalScrollView) rootView.findViewById(R.id.selectionscroll);

			Button button = CreateButton(buttonName);
			MarginLayoutParams marginParams = new MarginLayoutParams(
					spannablelayout.getLayoutParams());
			marginParams.setMargins(0, 0, 10, 0);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					marginParams);

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f,
							0f);
					fadeAnim.setDuration(800);

					fadeAnim.addListener(new AnimatorListenerAdapter() {
						public void onAnimationEnd(Animator animation) {
							spannablelayout.removeView(v);
						}
					});
					fadeAnim.start();
				}
			});
			ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(button, "alpha",
					0f, 1f);
			fadeinAnimation.setDuration(800);
			fadeinAnimation.addListener(new AnimatorListenerAdapter() {
				public void onAnimationEnd(Animator animation) {
					scroll.fullScroll(View.FOCUS_RIGHT);
				}
			});
			fadeinAnimation.start();
			spannablelayout.addView(button, layoutParams);

			UpdateRecentList(button.getText().toString());
		}
		private void FillEditText(View v) {
			LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
			final HorizontalScrollView scroll = (HorizontalScrollView) rootView.findViewById(R.id.selectionscroll);

			Button btn = (Button) v;
			if (btn.getAlpha() <= mButtonApha)
				return;
			SearchData mSearchData = (SearchData) v.getTag();
			mSearchData.getSearchTags().get(btn.getId()).setCLicked(true);
			mAdapter.notifyDataSetChanged();

			Button button = CreateButton(btn.getText().toString());
			btn.setAlpha(mButtonApha);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					v.setAlpha(1.0f);
					Button btn1 = (Button) v;
					
					Button OwnerButton = (Button) btn1.getTag(R.string.tag2);
					ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(OwnerButton, "alpha", mButtonApha, 1f);
					fadeAnim2.setDuration(800);
					fadeAnim2.addListener(new AnimatorListenerAdapter() {
						public void onAnimationEnd(Animator animation) {
								mAdapter.notifyDataSetChanged();
						}
					});
					
					fadeAnim2.start();
					
					SearchData mSearchData = (SearchData) v.getTag(R.string.tag1);
					mSearchData.getSearchTags().get(OwnerButton.getId()).setCLicked(false);
					
					
					
					//FadeOut Animation of Clicked button Start
					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
					fadeAnim.setDuration(800);
					fadeAnim.addListener(new AnimatorListenerAdapter() {
						public void onAnimationEnd(Animator animation) {
							LinearLayout spannablelayout = (LinearLayout) rootView.findViewById(R.id.spannable);
							spannablelayout.removeView(v);
						}
					});
					fadeAnim.start();
					
					//FadeOut Animation of Clicked button End
				}
			});

			button.setId(btn.getId());
			button.setTag(R.string.tag1,mSearchData);
			button.setTag(R.string.tag2,v);
			

			MarginLayoutParams marginParams = new MarginLayoutParams(
					spannablelayout.getLayoutParams());
			marginParams.setMargins(0, 0, 10, 0);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					marginParams);

			ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(button, "alpha",
					0f, 1f);
			fadeinAnimation.setDuration(800);
			fadeinAnimation.addListener(new AnimatorListenerAdapter() {
				public void onAnimationEnd(Animator animation) {
					scroll.fullScroll(View.FOCUS_RIGHT);
				}
			});
			fadeinAnimation.start();
			spannablelayout.addView(button, layoutParams);

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

	  private ArrayList<SearchData> fillSearchData(){

			SearchData obj1 = new SearchData();
			obj1.setCategoryName("Recent");
			obj1.setSection(true);
			
			SearchData obj2 = new SearchData();
			List<ButtonData> tagsobj1 = new ArrayList<ButtonData>(); 
			tagsobj1.add(new ButtonData("New", false));
			tagsobj1.add(new ButtonData("Comedy", false));
			tagsobj1.add(new ButtonData("Horror", false));
			tagsobj1.add(new ButtonData("Popular", false));
			tagsobj1.add(new ButtonData("TV Shows", false));
			tagsobj1.add(new ButtonData("Top Rated", false));
			tagsobj1.add(new ButtonData("Kung Fu Comedy", false));

			obj2.setSearchTags(tagsobj1);
			obj2.setSection(false);
			
			SearchData obj3 = new SearchData();
			obj3.setCategoryName("Trending Now");
			obj3.setSection(true);
			
			SearchData obj4 = new SearchData();
			List<ButtonData> tagsobj2 = new ArrayList<ButtonData>(); 
			tagsobj2.add(new ButtonData("Riddick", false));
			tagsobj2.add(new ButtonData("Big Boss", false));
			tagsobj2.add(new ButtonData("Millers", false));
			tagsobj2.add(new ButtonData("Insidious", false));
			tagsobj2.add(new ButtonData("Comedy Nights with Kapil", false));
			tagsobj2.add(new ButtonData("Jabardasth", false));
			tagsobj2.add(new ButtonData("News Hour", false));

			obj4.setSearchTags(tagsobj2);
			obj4.setSection(false);
			
			SearchData obj5 = new SearchData();
			obj5.setCategoryName("Mostly Searced");
			obj5.setSection(true);
			
			SearchData obj6 = new SearchData();
			List<ButtonData> tagsobj3 = new ArrayList<ButtonData>(); 
			tagsobj3.add(new ButtonData("Katrina Kaif", false));
			tagsobj3.add(new ButtonData("Ranbir Kapoor", false));
			tagsobj3.add(new ButtonData("Sunny Leone", false));
			tagsobj3.add(new ButtonData("Mahesh Baabu", false));
			tagsobj3.add(new ButtonData("Attarintiki Daaredhi", false));
			tagsobj3.add(new ButtonData("Anushka", false));
			tagsobj3.add(new ButtonData("Deepika Padukone", false));

			obj6.setSearchTags(tagsobj3);
			obj6.setSection(false);
			
			SearchData obj7 = new SearchData();
			obj7.setCategoryName("A");
			obj7.setSection(true);
			
			SearchData obj8 = new SearchData();
			List<ButtonData> tagsobj4 = new ArrayList<ButtonData>(); 
			tagsobj4.add(new ButtonData("Amitabh", false));
			tagsobj4.add(new ButtonData("Aamir Khan", false));
			tagsobj4.add(new ButtonData("Aashiqi 2", false));
			tagsobj4.add(new ButtonData("Anushka", false));
			tagsobj4.add(new ButtonData("Arnab", false));

			obj8.setSearchTags(tagsobj4);
			obj8.setSection(false);
			
			SearchData obj9 = new SearchData();
			obj9.setCategoryName("B");
			obj9.setSection(true);
			
			SearchData obj10 = new SearchData();
			List<ButtonData> tagsobj5 = new ArrayList<ButtonData>(); 
			tagsobj5.add(new ButtonData("Riddick", false));
			tagsobj5.add(new ButtonData("Big Boss", false));
			tagsobj5.add(new ButtonData("Millers", false));
			tagsobj5.add(new ButtonData("Insidious", false));
			tagsobj5.add(new ButtonData("Comedy Nights with Kapil", false));
			tagsobj5.add(new ButtonData("Jabardasth", false));
			tagsobj5.add(new ButtonData("News Hour with Arnab", false));

			obj10.setSearchTags(tagsobj5);
			obj10.setSection(false);
			
			SearchData obj11 = new SearchData();
			obj11.setCategoryName("C");
			obj11.setSection(true);
			
			SearchData obj12 = new SearchData();
			List<ButtonData> tagsobj6 = new ArrayList<ButtonData>(); 
			tagsobj6.add(new ButtonData("Riddick", false));
			tagsobj6.add(new ButtonData("Big Boss", false));
			tagsobj6.add(new ButtonData("Insidious", false));
			tagsobj6.add(new ButtonData("Millers", false));
			tagsobj6.add(new ButtonData("Comedy Nights with Kapil", false));
			tagsobj6.add(new ButtonData("Jabardasth", false));
			tagsobj6.add(new ButtonData("News Hour with Arnab", false));

			obj12.setSearchTags(tagsobj6);
			obj12.setSection(false);
			
			SearchData obj13 = new SearchData();
			obj13.setCategoryName("Z");
			obj13.setSection(true);
			
			SearchData obj14 = new SearchData();
			List<ButtonData> tagsobj7 = new ArrayList<ButtonData>();
			tagsobj7.add(new ButtonData("Zanjeer", false));
			tagsobj7.add(new ButtonData("Catherine Zeta-Jones", false));
			tagsobj7.add(new ButtonData("Hans Zimmer", false));
			tagsobj7.add(new ButtonData("Zindagi", false));
			tagsobj7.add(new ButtonData("Zinda", false));
			tagsobj7.add(new ButtonData("Zero", false));


			obj14.setSearchTags(tagsobj7);
			obj14.setSection(false);

			ArrayList<SearchData> mSearchData = new ArrayList<SearchData>();
			mSearchData.add(obj1);
			mSearchData.add(obj2);
			
			mSearchData.add(obj3);
			mSearchData.add(obj4);

			mSearchData.add(obj5);
			mSearchData.add(obj6);
			
			mSearchData.add(obj7);
			mSearchData.add(obj8);
			
			mSearchData.add(obj9);
			mSearchData.add(obj10);
			
			mSearchData.add(obj11);
			mSearchData.add(obj12);
			

			
			SearchData obj15 = new SearchData();
			obj15.setCategoryName("E");
			obj15.setSection(true);
			mSearchData.add(obj15);
			
			SearchData obj16 = new SearchData();
			obj16 = obj10;
			mSearchData.add(obj16);
			
			SearchData obj17 = new SearchData();
			obj17.setCategoryName("F");
			obj17.setSection(true);
			mSearchData.add(obj17);
			
			SearchData obj18 = new SearchData();
			obj18 = obj10;
			mSearchData.add(obj18);
			
			SearchData obj19 = new SearchData();
			obj19.setCategoryName("G");
			obj19.setSection(true);
			mSearchData.add(obj19);
			
			SearchData obj20 = new SearchData();
			obj20 = obj10;
			mSearchData.add(obj20);
			
			SearchData k = new SearchData();
			k.setCategoryName("K");
			k.setSection(true);
			mSearchData.add(k);
			
			SearchData ktags = new SearchData();
			ktags = obj10;
			mSearchData.add(ktags);
			
			SearchData s = new SearchData();
			s.setCategoryName("S");
			s.setSection(true);
			mSearchData.add(s);
			
			SearchData stags = new SearchData();
			stags = obj10;
			mSearchData.add(stags);
			
			SearchData y = new SearchData();
			y.setCategoryName("Y");
			y.setSection(true);
			mSearchData.add(y);
			
			SearchData ytags = new SearchData();
			ytags = obj10;
			mSearchData.add(ytags);
			
			mSearchData.add(obj13);
			mSearchData.add(obj14);
			
			return mSearchData;
	  }
	    
	    
		@Override
		public void OnOpenAction(View v) {
			FillEditText(v);
		}
		
		
		private Button CreateButton(String text)
		{
			Button btn = new Button(getContext());
			btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			btn.setBackgroundResource(R.drawable.roundedbutton);
			btn.setTextColor(Color.parseColor("#FFFFFF"));
			btn.setText(text);
			btn.setTextSize(14.667f);
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(final View v) {
					v.setAlpha(1.0f);
					Button btn1 = (Button) v;
					Button OwnerButton = (Button)btn1.getTag();
					ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(OwnerButton, "alpha", 0.5f, 1f);
					fadeAnim2.setDuration(800);
					
					fadeAnim2.start();
					//OwnerButton.setAlpha(1.0f);
					// TODO Auto-generated method stub
					
//					LinearLayout spannablelayout = (LinearLayout)findViewById(R.id.spannable);
//					spannablelayout.removeView(v);
					
					ValueAnimator fadeAnim = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
					fadeAnim.setDuration(800);
					
					fadeAnim.addListener(new AnimatorListenerAdapter() {
						public void onAnimationEnd(Animator animation) {
							LinearLayout spannablelayout = (LinearLayout)rootView.findViewById(R.id.spannable);
							spannablelayout.removeView(v);
						}
					});
					fadeAnim.start();
					
				}
			});
			return btn;
		}
		
}
