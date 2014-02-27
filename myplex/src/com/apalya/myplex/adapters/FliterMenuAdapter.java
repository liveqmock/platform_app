package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.StateLanguageUtils;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

public class FliterMenuAdapter extends ArrayAdapter<FilterMenudata>
		implements PinnedSectionListAdapter {
	private Context mContext;
	public LayoutInflater mInflater;
	private List<FilterMenudata> mMenuDataList = new ArrayList<FilterMenudata>();
	private String state,language;

	public void setDataList(List<FilterMenudata> menuDataList){
		state = SharedPrefUtils.getFromSharedPreference(mContext, mContext.getString(R.string.pref_state));
		if(state != null && state.length() >0){
			StateLanguageUtils languageUtils = new StateLanguageUtils();
			language = languageUtils.getLanguage(state);
			if(language != null && language.length() >0){
				Collections.sort(menuDataList,new CoutComparator());
				Collections.sort(menuDataList,new LanguageComparetaor());
			}
		}
		this.mMenuDataList = menuDataList;
	}

	public FliterMenuAdapter(Context context, int resource,
			int textViewResourceId, List<FilterMenudata> objects) {
		super(context, resource, textViewResourceId, objects);
		this.mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TextView view = (TextView) super.getView(position, convertView,
		// parent);
		// view.setTextColor(Color.DKGRAY);
		View v = null;
		if (getItem(position).type == FilterMenudata.SECTION) {
			v = mInflater.inflate(R.layout.filtermenuitem, null);
			TextView txt = (TextView) v.findViewById(R.id.filtermenutext);
			txt.setText(mMenuDataList.get(position).label);
			txt.setTypeface(FontUtil.Roboto_Light);
			v.setTag(mMenuDataList.get(position));
		} else if (getItem(position).type == FilterMenudata.ITEM) {
			v = mInflater.inflate(R.layout.filtersubmenuitem, null);
			TextView txt = (TextView) v.findViewById(R.id.filtersubmenutext);
			txt.setText(mMenuDataList.get(position).label);
			txt.setTypeface(FontUtil.Roboto_Light);
			v.setTag(mMenuDataList.get(position));
		}
		return v;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).type;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == FilterMenudata.SECTION;
	}
	private class LanguageComparetaor implements Comparator<FilterMenudata>{
		@Override
		public int compare(FilterMenudata lhs, FilterMenudata rhs) {
			String lhsString = lhs.label.replaceAll("\\(.*?\\)","").trim();
			String rhsString = rhs.label.replaceAll("\\(.*?\\)","").trim();
			if(lhsString.contains("All")){
				return -1;
			}else if(rhsString.contains("All")){
				return 1;
			}else if(lhsString.equalsIgnoreCase( language )){
				return -1;
			}else if(rhsString.equalsIgnoreCase( language )){
				return 1;
			}else if(lhsString.equalsIgnoreCase( "Hindi" )){
				return -1;
			}else if(rhsString.equalsIgnoreCase( "Hindi" )){
				return 1;
			}else if(lhsString.equalsIgnoreCase( "English" )){
				return -1;
			}else if(rhsString.equalsIgnoreCase( "English" )){
				return 1;
			}else {
				return 0;
			}
		}
	}
	private class CoutComparator implements Comparator<FilterMenudata>{
		@Override
		public int compare(FilterMenudata lhs, FilterMenudata rhs) {
			int lhsCount = 0,rhsCount = 0;
			Matcher lhsMatcher = Pattern.compile("\\(([^)]+)\\)").matcher(lhs.label);
		     while(lhsMatcher.find()) {
		       System.out.println(lhsMatcher.group(1));    
		       lhsCount = Integer.parseInt(lhsMatcher.group(1));
		     }
		     Matcher rhsMatcher = Pattern.compile("\\(([^)]+)\\)").matcher(rhs.label);
		     while(rhsMatcher.find()) {
		       System.out.println(rhsMatcher.group(1));    
		       rhsCount = Integer.parseInt(rhsMatcher.group(1));
		       }
		     if(lhsCount > rhsCount){
		    	 return -1;
		     }else{
		    	 return 1;
		     }
		}
	}


}
