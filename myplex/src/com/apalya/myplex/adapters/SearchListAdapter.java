package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;

import com.apalya.myplex.R;
import com.apalya.myplex.adapters.OpenListener.OpenCallBackListener;
import com.apalya.myplex.data.SearchData;
import com.apalya.myplex.views.FlowLayout;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;


public class SearchListAdapter extends BaseAdapter implements
		SectionIndexer, PinnedSectionListAdapter {

	private int mListOffset = 150;
	private static final int[] COLORS = new int[] { android.R.color.black,
			android.R.color.black, android.R.color.black, android.R.color.black };

	private LayoutInflater inflater = null;
	private OpenCallBackListener mButtonClickListener = null;
	Context mlocalContext;
	private SectionIndexer sectionIndexer;
	List<SearchData> mSearchDataList = new ArrayList<SearchData>();

	static class ViewHolder {
		TextView categoryText;
		Button arrowButton;
		// FlowLayout mFlowLayout;
		LinearLayout mLinearLlayout;
	}

	public SearchListAdapter(Context context,
			ArrayList<SearchData> mLocalSearchData) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mlocalContext = context;
		mSearchDataList = mLocalSearchData;
	}


	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		if (mSearchDataList == null)
			return null;
		if (mSearchDataList.size() > 0) {
			System.out.println("POS:: " + position);

			SearchData mLocalSearchData = mSearchDataList.get(position);
			FlowLayout mFlowLayout;
			LayoutInflater inflater = (LayoutInflater) mlocalContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (mLocalSearchData.isSection()) {
				
					convertView = inflater.inflate(R.layout.list, null);
				
				TextView view = (TextView) convertView.findViewById(R.id.searchgroup);
//				Button arrowButton = (Button) convertView.findViewById(R.id.Collapsiblebutton);
				view.setText(mLocalSearchData.getCategoryName());
				

				/*if(arrowButton!=null)
					{
					arrowButton.setTag(mLocalSearchData);
					arrowButton.setId(position);
					arrowButton.setOnClickListener(new OnClickListener() {
	
						@Override
						public void onClick(View v) {
							Button btn = (Button) v;
							
							SearchData searchData = (SearchData) btn.getTag();
							int position = parent.indexOfChild((ViewGroup)btn.getParent().getParent());
							LinearLayout view = (LinearLayout) parent.getChildAt(position + 1);
							
							Toast.makeText(mlocalContext, searchData.getCategoryName(),
									Toast.LENGTH_SHORT).show();
							if (!searchData.isVisible()) {
								searchData.setVisible(true);
	
								SearchData temp = mSearchDataList.get(position + 1);
								temp.setVisible(true);
								mSearchDataList.set(position + 1, temp);
	
								if (view != null) {
									int index = view.getChildCount();
									View mFlowLayoutView = view
											.getChildAt(index - 1);
									ViewGroup.LayoutParams params = mFlowLayoutView
											.getLayoutParams();
									params.height = LayoutParams.WRAP_CONTENT;
									
									Animation anim = new ExpandCollapseAnimation(mFlowLayoutView,ExpandCollapseAnimation.EXPAND
											);
											anim.setDuration(1000);
											
											mFlowLayoutView.startAnimation(anim);
											btn.setBackgroundResource(R.drawable.hide);
									mFlowLayoutView.requestLayout();
								}
							} else {
								if (view != null) {
									int index = view.getChildCount();
									View mFlowLayoutView = view
											.getChildAt(index - 1);
									ViewGroup.LayoutParams params = mFlowLayoutView
											.getLayoutParams();
									params.height = 150;
									mFlowLayoutView.requestLayout();
								}
								searchData.setVisible(false);
	
								SearchData temp = mSearchDataList.get(position + 1);
								temp.setVisible(false);
								mSearchDataList.set(position + 1, temp);
								btn.setBackgroundResource(R.drawable.expose);
							}
						}
					});
				}*/

			} else if (!mLocalSearchData.isSection()) {
				Boolean addnow = false;
				//if(convertView == null)
				{
					addnow = true;
					convertView = inflater.inflate(R.layout.flowlayout, null);
				}

				LinearLayout mainlay = (LinearLayout) convertView.findViewById(R.id.mainLinearLayout);
				
				
				mFlowLayout = new FlowLayout(mlocalContext);
				
				if (mLocalSearchData.getNames() != null
						&& mLocalSearchData.getNames().length > 0) {
					for (int i = 0; i < mLocalSearchData.getNames().length; i++) {
						Button btn = new Button(mlocalContext);
						btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
						btn.setBackgroundResource(R.drawable.roundedbutton_stroke);
						btn.setTextColor(Color.parseColor("#F57242"));
						btn.setText(mLocalSearchData.getNames()[i]);
						btn.setTextSize(14.667f);
						btn.setId(position);
						if(mButtonClickListener !=null)
							btn.setOnClickListener(new OpenListener(mButtonClickListener));
						//btn.getBackground().setColorFilter(Color.RED, Mode.MULTIPLY);
						//btn.setBackgroundResource(R.drawable.roundedborder);
						/*btn.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								Button btn = (Button) v;
								SearchData mSearchData = (SearchData) v
										.getTag();
								Log.d("SearchActitvity",
										"btnClick.OnClickListener");
								Toast.makeText(mlocalContext, btn.getText(),
										Toast.LENGTH_SHORT).show();
								btn.setAlpha(0.5f);
								ObjectAnimator animation2 = ObjectAnimator.ofFloat(btn,
								          "y", -60);
								      animation2.setDuration(2000);
								      animation2.start();
								FillEditText(v);
							}
						});*/
						// btn.setBackgroundDrawable(R.drawable.)
						mFlowLayout.addView(btn);
					}

				}
				
				
				//<---- Code to hold expandable state ---->
				/*if (mLocalSearchData.isVisible()) {
					LinearLayout ll = (LinearLayout) parent
							.getChildAt(position);
					if (ll != null) {
						ViewGroup.LayoutParams llparams = mFlowLayout
								.getLayoutParams();
						llparams.height = LayoutParams.WRAP_CONTENT;
						mFlowLayout.requestLayout();
					}
				} else {
					LinearLayout ll = (LinearLayout) parent
							.getChildAt(position);
					if (ll != null) {
						ViewGroup.LayoutParams llparams = mFlowLayout
								.getLayoutParams();
						llparams.height = mListOffset;
						mFlowLayout.requestLayout();
					}
				}*/
				//<---- Code to hold expandable state ---->
				if(addnow)
				{
					mainlay.addView(mFlowLayout);
				}
			}
		}
		return convertView;
	}
	
	public void setOpenListener(OpenCallBackListener openListener) {
		this.mButtonClickListener = openListener;
	}
	
	

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (mSearchDataList.get(position).isSection()) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == SearchData.SECTION; // return 1 if it is a section
	}

	@Override
	public int getPositionForSection(int section) {
		return getSectionIndexer().getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return getSectionIndexer().getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return getSectionIndexer().getSections();
	}

	private SectionIndexer getSectionIndexer() {
		if (sectionIndexer == null) {
			sectionIndexer = createSectionIndexer(mSearchDataList);
		}
		return sectionIndexer;
	}

	private SectionIndexer createSectionIndexer(
			List<SearchData> mlocalSearchData) {

		return createSectionIndexer(mlocalSearchData,
				new Function<SearchData, String>() {

					@Override
					public String apply(SearchData input) {
						return input.getCategoryName();
					}
				});
	}

	/**
	 * Create a SectionIndexer given an arbitrary function mapping countries to
	 * their section name.
	 * 
	 * @param countries
	 * @param sectionFunction
	 * @return
	 */
	private SectionIndexer createSectionIndexer(
			List<SearchData> mlocalSearchDataList,
			Function<SearchData, String> sectionFunction) {

		List<String> sections = new ArrayList<String>();
		final List<Integer> sectionsToPositions = new ArrayList<Integer>();
		final List<Integer> positionsToSections = new ArrayList<Integer>();

		// assume the countries are properly sorted
		for (int i = 0; i < mlocalSearchDataList.size(); i++) {
			SearchData mlocalSearchData = mlocalSearchDataList.get(i);
			String section = sectionFunction.apply(mlocalSearchData);
			if (sections.isEmpty()
					|| !sections.get(sections.size() - 1).equals(section)) {
				// add a new section
				sections.add(section);
				// map section to position
				sectionsToPositions.add(i);
			}

			// map position to section
			positionsToSections.add(sections.size() - 1);
		}

		final String[] sectionsArray = sections.toArray(new String[sections
				.size()]);

		return new SectionIndexer() {

			@Override
			public Object[] getSections() {
				return sectionsArray;
			}

			@Override
			public int getSectionForPosition(int position) {
				return positionsToSections.get(position);
			}

			@Override
			public int getPositionForSection(int section) {
				return sectionsToPositions.get(section);
			}
		};
	}

	public void refreshSections() {
		sectionIndexer = null;
		getSectionIndexer();
	}

	@Override
	public int getCount() {
		return mSearchDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}