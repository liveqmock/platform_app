package com.apalya.myplex;

import java.util.ArrayList;



import com.apalya.myplex.adapters.SearchListAdapter;
import com.apalya.myplex.adapters.OpenListener.OpenCallBackListener;
import com.apalya.myplex.data.SearchData;
import com.apalya.myplex.views.PinnedSectionListView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SearchActivity extends BaseActivity implements OpenCallBackListener {

	EditText mSearchInput;
	private PinnedSectionListView mPinnedListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchlayout);
		
		mSearchInput = (EditText) findViewById(R.id.inputSearch);
        
        
        ArrayList<SearchData>  mSearchData=fillSearchData();
		try {
			mPinnedListView=(PinnedSectionListView) findViewById(R.id.list_view);
			ArrayList<SearchData> mNewSearchData=prepareData(mSearchData);
			SearchListAdapter adapter = new SearchListAdapter(this, mNewSearchData);
			adapter.setOpenListener(SearchActivity.this);
			mPinnedListView.setAdapter(adapter);
			
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	  private ArrayList<SearchData> fillSearchData(){
	    	
	    	 SearchData recent=new SearchData();
	 		String []name={"New","Comedy","Horror","Popular","TV Shows","Top Rated","Kung Fu Comedy"};
	 		recent.setCategoryName("Recent");
	 		recent.setNames(name);
	 		
	 		//toprating
	 		SearchData topRatingData=new SearchData();
	 		String []topRatingname={"Action","Adventure","Fiction","Romance","Mystery"};
	 		topRatingData.setCategoryName("Trending Now");
	 		topRatingData.setNames(topRatingname);
	 		
	 		
	 		
	 		SearchData sd3=new SearchData();
	 		String []array3={"Katrina Kaif","Priyanka chopra","Sunny Leone",};
	 		sd3.setCategoryName("Mostly Searched");
	 		sd3.setNames(array3);
	 		
	 		
	 		SearchData sd4=new SearchData();
	 		String []array4={"NewArrival4:Ranbir kapoor","NewArrival4:Katrina","Slaman khan","Pavan kalyan","Mahesh babu","Africa","Algeria","Oceania","American",
	 				"Greenland","Grenada"};
	 		sd4.setCategoryName("A");
	 		sd4.setNames(array4);
	 		
	 		
	 		SearchData sd5=new SearchData();
	 		String []array5={"MostPopular5:Ranbir kapoor5","MostPopular5:Katrina5","MostPopular5:Slaman khan5","MostPopular5:Pavan kalyan5","Mahesh babu5","Africa5","Algeria5","Oceania5","American5",
	 				"Greenland5","Grenada5"};
	 		sd5.setCategoryName("B");
	 		sd5.setNames(array5);
	 		
	 		
	 		SearchData sd6=new SearchData();
	 		String []array6={"Movies6:Ranbir kapoor6","Movies6:Katrina6","Movies6:Slaman khan6","Pavan kalyan6","Mahesh babu6","Africa6","Algeria6","Oceania6","American6",
	 				"Greenland6","Grenada6"};
	 		sd6.setCategoryName("C");
	 		sd6.setNames(array6);
	 		
	 		SearchData sd7=new SearchData();
	 		String []array7={"Live7:Ranbir kapoor7","Live7:Katrina7","Live7:Slaman khan7","Live7:Pavan kalyan7","Live7:Mahesh babu7","Africa7","Algeria7","Oceania7","American7",
	 				"Greenland7","Grenada7"};
	 		sd7.setCategoryName("D");
	 		sd7.setNames(array7);
	 		
	 		SearchData A=new SearchData();
	 		String []A1={"Live7:Ranbir kapoor7","Live7:Katrina7","Live7:Slaman khan7","Live7:Pavan kalyan7","Live7:Mahesh babu7","Africa7","Algeria7","Oceania7","American7",
	 				"Greenland7","Grenada7"};
	 		A.setCategoryName("E");
	 		A.setNames(array7);
	 		
	 		SearchData B=new SearchData();
	 		String []B1={"Live7:Ranbir kapoor7","Live7:Katrina7","Live7:Slaman khan7","Live7:Pavan kalyan7","Live7:Mahesh babu7","Africa7","Algeria7","Oceania7","American7",
	 				"Greenland7","Grenada7"};
	 		B.setCategoryName("F");
	 		B.setNames(B1);
	 		
	 		
	 		SearchData C=new SearchData();
	 		String []C1={"Live7:Ranbir kapoor7","Live7:Katrina7","Live7:Slaman khan7","Live7:Pavan kalyan7","Live7:Mahesh babu7","Africa7","Algeria7","Oceania7","American7",
	 				"Greenland7","Grenada7"};
	 		C.setCategoryName("G");
	 		C.setNames(C1);
	 		
	 		ArrayList<SearchData>	mSearchData=new ArrayList<SearchData>();
	 		mSearchData.add(recent);
	 		mSearchData.add(topRatingData);
	 		mSearchData.add(sd3);
	 		mSearchData.add(sd4);
	 		
	 		mSearchData.add(sd5);
	 		mSearchData.add(sd6);
	 		mSearchData.add(sd7);
	 		mSearchData.add(A);
	 		mSearchData.add(B);
	 		mSearchData.add(C);
			return mSearchData;
	    	
	    }
	    
		private  ArrayList<SearchData> prepareData(
				ArrayList<SearchData> searchData) {
			ArrayList<SearchData> mNewSearchData = new ArrayList<SearchData>();
			if (searchData == null || (!(searchData.size() > 0))) {
				return mNewSearchData;
			}

			SearchData newSectionData;
			SearchData newItemData;

			for (int i = 0; i < searchData.size(); i++) {
				/*
				 *  adding section data
				 *   (setsection is true to indicate it is section data)
				 */
				newSectionData = new SearchData();
				newSectionData.setCategoryName(searchData.get(i).getCategoryName());
				newSectionData.setNames(searchData.get(i).getNames());
				newSectionData.setSection(true);
				mNewSearchData.add(newSectionData);
				String[] names = searchData.get(i).getNames();
				if (names != null && names.length > 0) {
					newItemData = new SearchData();
					newItemData.setSection(false);
					newItemData.setCategoryName(searchData.get(i).getCategoryName());
					newItemData.setNames(searchData.get(i).getNames());
					mNewSearchData.add(newItemData);
				}
			}

			return mNewSearchData;

		}
	    
		@Override
		public void OnOpenAction(View v) {
			
			FillEditText(v);
			
		}
		
		private void FillEditText(View v)
		{
			LinearLayout spannablelayout = (LinearLayout)findViewById(R.id.spannable);
			Button btn = (Button) v;
			if(btn.getAlpha() <= 0.5f)
				return;
			SearchData mSearchData = (SearchData) v.getTag();
			
			Button button = CreateButton(btn.getText().toString());
			btn.setAlpha(0.5f);
			
			button.setId(btn.getId());
			button.setTag(v);
			
			MarginLayoutParams marginParams = new MarginLayoutParams(spannablelayout.getLayoutParams());
			marginParams.setMargins(0,0, 10, 0);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginParams);
			
			ValueAnimator fadeinAnimation = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
			fadeinAnimation.setDuration(800);
			fadeinAnimation.start();
			spannablelayout.addView(button,layoutParams);
		}
		
		private Button CreateButton(String text)
		{
			Button btn = new Button(this);
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
							LinearLayout spannablelayout = (LinearLayout)findViewById(R.id.spannable);
							spannablelayout.removeView(v);
						}
					});
					fadeAnim.start();
					
				}
			});
			return btn;
		}
		
}
