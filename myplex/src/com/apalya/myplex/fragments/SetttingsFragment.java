package com.apalya.myplex.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.R;
import com.apalya.myplex.SubscriptionView;
import com.apalya.myplex.TwitterWebView;
import com.apalya.myplex.adapters.SettingsAdapter;
import com.apalya.myplex.data.SettingsData;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.PinnedSectionListView;

public class SetttingsFragment extends BaseFragment {

	private View mRootView;
	private PinnedSectionListView mSettingsListView;
	private SettingsAdapter mListAdapter;
	private List<SettingsData> mSettingsList;
	private String FEEDBACK = "Feedback";
	private String TANDC = "Terms & Conditions";
	private String PRIVACYPOLIY ="Privacy Policy";
	private String HELP = "Help";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.settingslayout, container, false);
		mSettingsListView = (PinnedSectionListView) mRootView
				.findViewById(R.id.settings_list);
		mSettingsList = new ArrayList<SettingsData>();
		PreapreSettingsData();
		
		mSettingsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {

				SettingsData data = (SettingsData) v.getTag();
				if(data.type == SettingsData.SECTION || data.mSettingName.contains("Download"))
					return;
				Intent i = new Intent(mContext,SubscriptionView.class);
				
				Bundle b = new Bundle();
				if (data.mSettingName.equals(FEEDBACK)) 
						b.putString("url", "https://myplex.zendesk.com/account/dropboxes/20111493?x=1#/dropbox/tickets/new");
				else if(data.mSettingName.equals(TANDC))
					b.putString("url", "http://static.myplex.tv/tnc_win8.html");
				else if(data.mSettingName.equals(PRIVACYPOLIY))
					b.putString("url", "http://static.myplex.tv/privacy_win8.html");
				else if(data.mSettingName.equals(HELP))
					b.putString("url", "http://www.apalya.com/about-us/");
				
					i.putExtras(b);
					startActivity(i);
			}
		});

		return mRootView;
	}

	private void PreapreSettingsData() {
		mSettingsList.add(new SettingsData(SettingsData.SECTION, "App Settings", 0,SettingsData.VIEWTYPE_NORMAL));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, "Download only on Wifi", 0,SettingsData.VIEWTYPE_TOGGLEBUTTON));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, "Show player logs", 0,SettingsData.VIEWTYPE_TOGGLEBUTTON));
		mSettingsList.add(new SettingsData(SettingsData.SECTION, "Myplex 2.0", 0,SettingsData.VIEWTYPE_NORMAL));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, FEEDBACK, 0,SettingsData.VIEWTYPE_NORMAL));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, TANDC, 0,SettingsData.VIEWTYPE_NORMAL));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, PRIVACYPOLIY, 0,SettingsData.VIEWTYPE_NORMAL));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, HELP, 0,SettingsData.VIEWTYPE_NORMAL));

		mListAdapter = new SettingsAdapter(getContext(),
				android.R.layout.simple_list_item_1, android.R.id.text1,
				mSettingsList);
		mSettingsListView.setAdapter(mListAdapter);
	}

}
