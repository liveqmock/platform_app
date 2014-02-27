package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.RelatedResults;

import com.apalya.myplex.R;
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.SettingsData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.SetttingsFragment;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
		SettingsData data = getItem(position);
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
			if(data.viewtype == SettingsData.VIEWTYPE_TOGGLEBUTTON){
				v = mInflater.inflate(R.layout.setting_item_togglebutton, null);
				Switch txt = (Switch) v.findViewById(R.id.settingitem_togglebutton);
				txt.setText(mSettingsList.get(position).mSettingName);
				txt.setTypeface(FontUtil.Roboto_Light);
				if(data.mSettingName.equalsIgnoreCase("Download only on Wifi")){
					txt.setChecked(myplexapplication.getApplicationSettings().downloadOnlyOnWifi);	
				}else if(data.mSettingName.equalsIgnoreCase("Show player logs")){
					txt.setChecked(myplexapplication.getApplicationSettings().showPlayerLogs);	
				}else if (data.mSettingName.equalsIgnoreCase(SetttingsFragment.SENSOR_SCROLL)){
					txt.setChecked(ApplicationSettings.ENABLE_SENSOR_SCROLL);
				}
				txt.setOnCheckedChangeListener(mActionListener);
				txt.setTag(data);
				v.setTag(data);
			}else{
				v = mInflater.inflate(R.layout.settinglistitem, null);
				TextView txt = (TextView) v.findViewById(R.id.settingsgroup);
				txt.setText(mSettingsList.get(position).mSettingName);
				txt.setTypeface(FontUtil.Roboto_Light);
				v.setTag(mSettingsList.get(position));	
			}
		}
		return v;
	}
	public OnCheckedChangeListener mActionListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			try {
				Analytics.mixPanelWifiOnly(isChecked);
				SettingsData obj = (SettingsData)buttonView.getTag();
				if(obj != null){
					if(obj.mSettingName.equalsIgnoreCase("Download only on Wifi")){
						myplexapplication.getApplicationSettings().downloadOnlyOnWifi = isChecked;
					}else if(obj.mSettingName.equalsIgnoreCase("Show player logs")){
						myplexapplication.getApplicationSettings().showPlayerLogs = isChecked;	
					}else if (obj.mSettingName.equalsIgnoreCase(SetttingsFragment.SENSOR_SCROLL)){
						SharedPrefUtils.writeToSharedPref(mContext, mContext.getString(R.string.isSensorScrollEnabled),isChecked);
						ApplicationSettings.ENABLE_SENSOR_SCROLL=isChecked;
					}
				}
			} catch (Exception e) {
				Log.e("SettingsAdapter",e.toString());
			}
			
		}
	};
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
