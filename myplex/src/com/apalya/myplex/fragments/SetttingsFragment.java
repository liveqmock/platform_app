package com.apalya.myplex.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.drm.DrmUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.apalya.myplex.BaseFragment;
import com.apalya.myplex.LiveScoreWebView;
import com.apalya.myplex.R;
import com.apalya.myplex.SubscriptionView;
import com.apalya.myplex.TwitterWebView;
import com.apalya.myplex.adapters.SettingsAdapter;
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.SettingsData;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.DeviceRegUtil;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.WidevineDrm;
import com.apalya.myplex.utils.MessagePost.MessagePostCallback;
import com.apalya.myplex.views.DownloadStreamDialog;
import com.apalya.myplex.views.DownloadStreamDialog.DownloadListener;
import com.apalya.myplex.views.PinnedSectionListView;
import com.apalya.myplex.views.RatingDialog;

public class SetttingsFragment extends BaseFragment {

	private View mRootView;
	private PinnedSectionListView mSettingsListView;
	private SettingsAdapter mListAdapter;
	private List<SettingsData> mSettingsList;

	public static String RATING_POSTED = null; //analytics useful to getdata from MessagePost to CardDetailViewFactory
	public static String FEEDBACK_POSTED = null; //analytics useful to getdata from MessagePost to CardDetailViewFactory

	private String FEEDBACK = "feedback";
	private String TANDC = "terms & conditions";
	private String PRIVACYPOLIY ="privacy policy";
	private String HELP = "help";
	private String DOWNLOAD_OR_STREAM_MSG = "movie rental options: ";
	public static final String DRM_STATUS_STRING="WVDRM status";
	public static final String DRM_LEVAL_STRING="WVDRM statusKey";
	public static final String ROOT_STATUS_STRING="root status";
	public static final String DERIGISTER_DEVICE="deRegister device";
	public static final String SENSOR_SCROLL="Sensor Scroll";

	private int debug_mode_counter=0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.settingslayout, container, false);
		mSettingsListView = (PinnedSectionListView) mRootView
				.findViewById(R.id.settings_list);		
		PreapreSettingsData();
		debug_mode_counter=0;
		
		Analytics.mixPanelBrowsedSettings();
		Analytics.createScreenGA(Analytics.SCREEN_SETTINGS);
		mSettingsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {

				SettingsData data = (SettingsData) v.getTag();
				if(data.type == SettingsData.SECTION && data.mSettingName.equalsIgnoreCase("App Settings")){	
				
					if(debug_mode_counter == 5 ){
						
						ApplicationSettings.ENABLE_SHOW_PLAYER_LOGS_SETTINGS = true;
						PreapreSettingsData();
						mListAdapter.notifyDataSetChanged();
						return;
					}
					
					debug_mode_counter ++;
					return;
				}
				
				if(data.type == SettingsData.ITEM && data.mSettingName.equalsIgnoreCase(DERIGISTER_DEVICE)){	
					
					DeviceRegUtil deviceRegUtil = new DeviceRegUtil(mContext);
					deviceRegUtil.unregister();
					return;
				}
				
				if (data.type == SettingsData.ITEM
						&& data.mSettingName
								.contains(DRM_STATUS_STRING)) {					
					return;
				}
	
						
				if(data.type == SettingsData.SECTION || data.mSettingName.contains("Download"))
					return;
				
				if (data.mSettingName.equals(FEEDBACK)) {
					Analytics.mixPanelFeedbackInitiation();
					UserProfile userProfile = myplexapplication.getUserProfileInstance();
					CardData profileData=new CardData();
					profileData._id="0";
					RatingDialog dialog = new RatingDialog(mContext);
					dialog.prepareFeedback();
					dialog.showDialog(new MessagePostCallback() {
						
						@Override
						public void sendMessage(boolean status) {
							if(status){
								Analytics.mixPanelProvidedFeedback(FEEDBACK_POSTED, RATING_POSTED);
								Util.showToast(mContext, "Thanks for your feedback.",Util.TOAST_TYPE_INFO);
								
							}else{
								Util.showToast(mContext, "Unable to post your review.",Util.TOAST_TYPE_ERROR);
							}
						}
						
					}, profileData);
				}else if(data.mSettingName.contains(DOWNLOAD_OR_STREAM_MSG)){	
				DownloadStreamDialog dialog = new DownloadStreamDialog(mContext, "Movie rental options");	
				dialog.setAlwaysAskAsDefault();
				dialog.showAlwaysAskOption();	
				dialog.setListener(new DownloadListener() {                                                			
				@Override			
				public void onOptionSelected(boolean isDownload)			
				{
					PreapreSettingsData();
					mListAdapter.notifyDataSetChanged();
				}			
				});			
				dialog.showDialog();
				}
				else
				{
				Intent i = new Intent(mContext,LiveScoreWebView.class);
				
				Bundle b = new Bundle();
				if(data.mSettingName.equals(TANDC))
					b.putString("url", "http://help.myplex.com/terms-of-use/");
				else if(data.mSettingName.equals(PRIVACYPOLIY))
					b.putString("url", "http://help.myplex.com/privacy-policy/");
				else if(data.mSettingName.equals(HELP))
					b.putString("url", "http://help.myplex.com/");
				
					i.putExtras(b);
					startActivity(i);
				}
			}
		});

		return mRootView;
	}

	private void PreapreSettingsData() {
		String currentRentalOptions  ="";
		if(!(SharedPrefUtils.getBoolFromSharedPreference(mContext, mContext.getString(R.string.is_dont_ask_again,false)))){
				currentRentalOptions = "always ask";
		}else{
			currentRentalOptions = SharedPrefUtils.getBoolFromSharedPreference(mContext, mContext.getString(R.string.isDownload),true)?
					mContext.getString(R.string.download):mContext.getString(R.string.stream);
		}
					
		mSettingsList = new ArrayList<SettingsData>();		
		mSettingsList.add(new SettingsData(SettingsData.SECTION, "App Settings", 0,SettingsData.VIEWTYPE_NORMAL));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, DOWNLOAD_OR_STREAM_MSG+"\t\t"+currentRentalOptions, 0,SettingsData.VIEWTYPE_NORMAL));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, "Download only on Wifi", 0,SettingsData.VIEWTYPE_TOGGLEBUTTON));
		mSettingsList.add(new SettingsData(SettingsData.ITEM, SENSOR_SCROLL, 0,SettingsData.VIEWTYPE_TOGGLEBUTTON));
		if(ApplicationSettings.ENABLE_SHOW_PLAYER_LOGS_SETTINGS){
			mSettingsList.add(new SettingsData(SettingsData.ITEM, "Show player logs", 0,SettingsData.VIEWTYPE_TOGGLEBUTTON));
			WidevineDrm widevineDRM= new WidevineDrm(mContext);
			String drmStatus= widevineDRM.isProvisionedDevice()?" (Available)":" (Not Available)";
			mSettingsList.add(new SettingsData(SettingsData.ITEM, DRM_STATUS_STRING +drmStatus , 0,SettingsData.VIEWTYPE_NORMAL));
//			mSettingsList.add(new SettingsData(SettingsData.ITEM, DRM_LEVAL_STRING + " : "+widevineDRM.getWVLeval() , 0,SettingsData.VIEWTYPE_NORMAL));
//			mSettingsList.add(new SettingsData(SettingsData.ITEM, ROOT_STATUS_STRING, 0,SettingsData.VIEWTYPE_NORMAL));
			mSettingsList.add(new SettingsData(SettingsData.ITEM, DERIGISTER_DEVICE , 0,SettingsData.VIEWTYPE_NORMAL));
			
		}
		String version= Util.getAppVersionName(mContext);
		mSettingsList.add(new SettingsData(SettingsData.SECTION, mContext.getString(R.string.app_name) + " "+version, 0,SettingsData.VIEWTYPE_NORMAL));
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
