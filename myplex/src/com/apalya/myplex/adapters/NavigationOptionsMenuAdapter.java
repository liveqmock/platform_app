package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.apalya.myplex.R;
import com.apalya.myplex.data.NavigationOptionsMenu;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;

public class NavigationOptionsMenuAdapter extends BaseAdapter {
	private List<NavigationOptionsMenu> mMenuItemList = new ArrayList<NavigationOptionsMenu>();
	public LayoutInflater mInflater;
	public final static int CARDDETAILS_ACTION = 0;
	public final static int CARDEXPLORER_ACTION = 1;
	public final static int NOACTION_ACTION = 3;
	public final static int SEARCH_ACTION = 2;
	public final static int LOGOUT_ACTION = 4;
	public final static int NOFOCUS_ACTION = 5;
	public final static int INVITE_ACTION = 6;
	public final static int DOWNLOAD_ACTION = 7;
	public final static int SETTINGS_ACTION = 90;
	public final static String DOWNLOADS = "downloads";
	public final static String FAVOURITE = "favourites";
	public final static String RECOMMENDED = "home";
	public final static String DISCOVER = "discover";
	public final static String MOVIES = "movies";
	public final static String LIVETV = "live TV";
	public final static String PURCHASES = "purchases";
	public final static String TVSHOWS = "TV shows";
	public final static String SETTINGS = "settings";
	public final static String LOGOUT = "logout";
	public final static String LOGO = "ApplicationLogo";
	public final static String INVITEFRIENDS = "invite friends";
			
	public Context mContext;
	private boolean isLoggedIn = true;
	
	public NavigationOptionsMenuAdapter(Context context){
		this.mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}
	@Override
	public int getCount() {
		return mMenuItemList.size();
	}
	public void setMenuList(List<NavigationOptionsMenu> menuItemList ){
		this.mMenuItemList = menuItemList;
		notifyDataSetChanged();
	}
	
	public void setLoginStatus(boolean loginStatus)
	{
		isLoggedIn = loginStatus;
	}
	
	@Override
	public Object getItem(int arg0) {
		return mMenuItemList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	@Override
	public boolean areAllItemsEnabled() { 
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		NavigationOptionsMenu menu = mMenuItemList.get(position);
		return (menu.mScreenType == NOFOCUS_ACTION) ? false: true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		NavigationOptionsMenu menu = mMenuItemList.get(position);
		v = mInflater.inflate(menu.mResourceLayoutId, null);
		if (menu.mResourceLayoutId == R.layout.navigation_menuitemlarge) {
			TextView noImageText = (TextView)v.findViewById(R.id.drawer_list_item_image_text);
			noImageText.setTypeface(FontUtil.ss_symbolicons_line);
			NetworkImageView image = (NetworkImageView) v.findViewById(R.id.drawer_list_item_image);
			if(menu.mIconUrl == null || menu.mIconUrl.length() == 0){
				noImageText.setVisibility(View.VISIBLE);
				image.setVisibility(View.GONE);
			}else{
				noImageText.setVisibility(View.GONE);
				image.setVisibility(View.VISIBLE);
			}
			TextView text = (TextView) v.findViewById(R.id.drawer_list_item_text);
			text.setTypeface(FontUtil.Roboto_Light);
			text.setText(menu.mLabel);
			image.setDefaultImageResId(menu.mDefaultResId);
			image.setImageUrl(menu.mIconUrl, MyVolley.getImageLoader());
			String name=myplexapplication.getUserProfileInstance().getName();
			if(name!=null && !name.equalsIgnoreCase("Guest"))
				image.setScaleType(ScaleType.CENTER_CROP);
		} else if (menu.mResourceLayoutId == R.layout.navigation_menuitemsmall) {
			TextView text = (TextView) v.findViewById(R.id.drawer_list_item_text);
//			ImageView image = (ImageView) v.findViewById(R.id.drawer_list_item_image);
			TextView image = (TextView) v.findViewById(R.id.drawer_list_item_image);
			image.setText(menu.mDefaultResId);
			image.setTypeface(FontUtil.ss_symbolicons_line);
			
			text.setText(menu.mLabel);
			text.setTypeface(FontUtil.Roboto_Light);
			if(menu.mScreenType == NOFOCUS_ACTION)
				text.setTextColor(Color.parseColor("#888888"));
//			image.setImageResource(menu.mDefaultResId);
		}
		return v;
	}

};
