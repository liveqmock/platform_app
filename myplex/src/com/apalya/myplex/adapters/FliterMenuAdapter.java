package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

public class FliterMenuAdapter extends ArrayAdapter<FilterMenudata>
		implements PinnedSectionListAdapter {
	private Context mContext;
	public LayoutInflater mInflater;
	private List<FilterMenudata> mMenuDataList = new ArrayList<FilterMenudata>();
	public void setDataList(List<FilterMenudata> menuDataList){
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
			v.setTag(mMenuDataList.get(position));
		} else if (getItem(position).type == FilterMenudata.ITEM) {
			v = mInflater.inflate(R.layout.filtersubmenuitem, null);
			TextView txt = (TextView) v.findViewById(R.id.filtersubmenutext);
			txt.setText(mMenuDataList.get(position).label);
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
}
