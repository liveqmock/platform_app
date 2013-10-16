package com.apalya.myplex.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;

public class NavigationOptionsMenuAdapter extends BaseAdapter {
	private List<NavigationOptionsMenu> mMenuItemList = new ArrayList<NavigationOptionsMenu>();
	public LayoutInflater mInflater;
	public final static int CARDDETAILS = 0;
	public final static int CARDEXPLORER = 1;
	public final static int NOACTION = 3;
	public final static int SEARCH = 2;
	public final static int LOGOUT = 4;
	public final static int NOFOCUS = 5;
	public final static int DOWNLOADS = 6;
	public final static int INVITE = 7;
	public final static String FAVOURITE = "Favourite";
	public final static String RECOMMENDED = "Recommended";
	public final static String MOVIES = "Movies";
	public final static String LIVETV = "Live TV";
	public final static String TVSHOWS = "TV Shows";
	public Context mContext;
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
		return (menu.mScreenType == NOFOCUS) ? false: true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		NavigationOptionsMenu menu = mMenuItemList.get(position);
		v = mInflater.inflate(menu.mResourceLayoutId, null);
		if (menu.mResourceLayoutId == R.layout.navigation_menuitemlarge) {
			NetworkImageView image = (NetworkImageView) v.findViewById(R.id.drawer_list_item_image);
			image.setDefaultImageResId(menu.mDefaultResId);
			image.setImageUrl(menu.mIconUrl, MyVolley.getImageLoader());
			if(menu.mIconUrl!=null)
				image.setScaleType(ScaleType.CENTER_CROP);
		} else if (menu.mResourceLayoutId == R.layout.navigation_menuitemsmall) {
			TextView text = (TextView) v.findViewById(R.id.drawer_list_item_text);
			ImageView image = (ImageView) v.findViewById(R.id.drawer_list_item_image);
			text.setText(menu.mLabel);
			text.setTypeface(FontUtil.Roboto_Regular);
			image.setImageResource(menu.mDefaultResId);
		}
		return v;
	}

};
