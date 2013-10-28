package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;

import com.apalya.myplex.R;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.SettingsData;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsAdapter extends ArrayAdapter<SettingsData> implements PinnedSectionListAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<SettingsData> mSettingsList = new ArrayList<SettingsData>();

	public SettingsAdapter(Context context, int resource,
			int textViewResourceId, List<SettingsData> objects) {
		super(context, resource, textViewResourceId, objects);
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		this.mSettingsList = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		
		if (getItem(position).type == FilterMenudata.SECTION) {
			v = mInflater.inflate(R.layout.settinglistitem, null);
			LinearLayout layout = (LinearLayout) v.findViewById(R.id.settingsLayout);
			layout.setBackgroundResource(R.drawable.background_card);
			TextView txt = (TextView) v.findViewById(R.id.settingsgroup);
			txt.setText(mSettingsList.get(position).mSettingName);
			txt.setTypeface(FontUtil.Roboto_Bold);
			v.setTag(mSettingsList.get(position));
		}
		else
		{
			v = mInflater.inflate(R.layout.settinglistitem, null);
			TextView txt = (TextView) v.findViewById(R.id.settingsgroup);
			txt.setText(mSettingsList.get(position).mSettingName);
			txt.setTypeface(FontUtil.Roboto_Light);
			v.setTag(mSettingsList.get(position));
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
		// TODO Auto-generated method stub
		return viewType == SettingsData.SECTION;
	}
}
