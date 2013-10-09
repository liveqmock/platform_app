package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;

import com.apalya.myplex.R;
import com.apalya.myplex.adapters.OpenListener.OpenCallBackListener;
import com.apalya.myplex.data.SearchData;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.views.FlowLayout;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SectionIndexer;
import android.widget.TextView;


public class SearchListAdapter extends BaseAdapter implements
		 PinnedSectionListAdapter {

	private LayoutInflater inflater = null;
	private OpenCallBackListener mButtonClickListener = null;
	Context mlocalContext;
	private SectionIndexer sectionIndexer;
	List<SearchData> mSearchDataList = new ArrayList<SearchData>();

	public List<SearchData> getSearchDataList() {
		return mSearchDataList;
	}


	public void setSearchDataList(List<SearchData> mSearchDataList) {
		this.mSearchDataList = mSearchDataList;
	}

	class HeaderHolder {
		TextView categoryText;
	}
	
	class ButtonData{
		
		int buttonid;
		String buttonText;
		boolean isClicked;
		public ButtonData(int buttonid, String ButtonText, boolean isClicked) {
			this.buttonid = buttonid;
			this.isClicked = isClicked;
			this.buttonText = ButtonText;
		}
	}
	
	class TagsHolder{
//		List<ButtonData> searchTags;
		FlowLayout tagslayout;
		List<Button> searchButtons;
	}
	


	public SearchListAdapter(Context context, List<SearchData> mLocalSearchData) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			
			if (mLocalSearchData.isSection()) {
				
				HeaderHolder headerHolder = null;
				if(convertView == null)
				{
					headerHolder = new HeaderHolder();
					convertView = inflater.inflate(R.layout.list, null);
					headerHolder.categoryText = (TextView) convertView.findViewById(R.id.searchgroup);
					headerHolder.categoryText.setTypeface(FontUtil.Roboto_Regular);
					convertView.setTag(headerHolder);
				}
				else
					headerHolder = (HeaderHolder) convertView.getTag();
				
//				Button arrowButton = (Button) convertView.findViewById(R.id.Collapsiblebutton);
				headerHolder.categoryText.setText(mLocalSearchData.getCategoryName());

			} else{
				
				TagsHolder tagsHolder = null;
				if(convertView == null)
				{
					tagsHolder = new TagsHolder();
					convertView = inflater.inflate(R.layout.flowlayout, null);
					tagsHolder.tagslayout = (FlowLayout) convertView.findViewById(R.id.buttongroup);
					tagsHolder.searchButtons = new ArrayList<Button>();
					convertView.setTag(tagsHolder);
				}
				else
				{
					tagsHolder = (TagsHolder)convertView.getTag();
				}
				
				tagsHolder.tagslayout.removeAllViews();
				if (mLocalSearchData.getSearchTags() != null) {
					
					for(int i=0; i<mLocalSearchData.getSearchTags().size(); i++)
					{
						if(mLocalSearchData.getSearchTags().get(i) == null)
							continue;
						Button btn = (Button) CreateButton(i,mLocalSearchData.getSearchTags().get(i).getButtonName(),mLocalSearchData.getSearchTags().get(i).isCLicked());
								if(mButtonClickListener !=null)
									btn.setOnClickListener(new OpenListener(mButtonClickListener));
								
						btn.setTag(mLocalSearchData);	
						tagsHolder.tagslayout.addView(btn);
					}
				}
			}
		}
		return convertView;
	}
	
	private View CreateButton(int buttonid, String searchTag,Boolean clicked)
	{
		Button searchButton = new Button(mlocalContext);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,  LayoutParams.WRAP_CONTENT);
		searchButton.setLayoutParams(params);
		searchButton.setMinimumHeight(0);
		searchButton.setMinHeight(0);
		searchButton.setBackgroundResource(R.drawable.roundedbutton_stroke);
		int text_color = mlocalContext.getResources().getColor(R.color.searchtags_color);
		searchButton.setTextColor(text_color);
		searchButton.setTextSize(14f);
		searchButton.setTypeface(FontUtil.Roboto_Light);
		searchButton.setId(buttonid);
		searchButton.setText(searchTag);
		if(clicked)
			searchButton.setAlpha(0.25f);
		
		return searchButton;
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

/*	@Override
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
	}*/

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