package com.apalya.myplex.menu;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.views.PinnedSectionListView;
import com.apalya.myplex.views.PinnedSectionListView.PinnedSectionListAdapter;

public class FilterMenuProvider extends ActionProvider {
	protected final Context context;
	private LayoutInflater mInflater;
	private PopupWindow mPopupWindow = null;
	private View mParent;
	private List<PopupWindow> mPopupWindowList = new ArrayList<PopupWindow>();
	View menu;
	private static List<FilterMenudata> mMenuDataList = new ArrayList<FilterMenudata>();

	public FilterMenuProvider(Context context) {
		super(context);
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	private void dismissPopupWindow() {
		if (mPopupWindow != null) {
			mPopupWindowList.remove(mPopupWindow);
			mPopupWindow.dismiss();
			mPopupWindow = null;
		}
	}

	private void showPopup() {
		dismissPopupWindow();
		mPopupWindow = new PopupWindow(menu, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		mPopupWindowList.add(mPopupWindow);
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
			}
		});
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.showAsDropDown(mParent);
	}

	private PinnedSectionListView listView;

	@Override
	public View onCreateActionView(MenuItem forItem) {
		mParent = mInflater.inflate(R.layout.filteractionmenuitem, null);
		mParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showPopup();
			}
		});
		return mParent;
	}
	
	private OnClickListener mDelegate;
	public void addFilterData(List<FilterMenudata> datalist,
			OnClickListener listener) {
		mDelegate = listener;
		menu = mInflater.inflate(R.layout.filtermenupopup, null);
		listView = (PinnedSectionListView)menu.findViewById(R.id.listView1);
		mMenuDataList = datalist;		
		MyPinnedSectionListAdapter adapter = new MyPinnedSectionListAdapter(
				context, android.R.layout.simple_list_item_1,
				android.R.id.text1, mMenuDataList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(mItemClicked);
	}
	private OnItemClickListener mItemClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if(mDelegate != null){
				mDelegate.onClick(arg1);
			}
			dismissPopupWindow();
		}
	};
	private class MyPinnedSectionListAdapter extends
			ArrayAdapter<FilterMenudata> implements PinnedSectionListAdapter {

		public MyPinnedSectionListAdapter(Context context, int resource,
				int textViewResourceId, List<FilterMenudata> objects) {
			super(context, resource, textViewResourceId, objects);
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
				TextView txt = (TextView) v
						.findViewById(R.id.filtersubmenutext);
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

	@Override
	public View onCreateActionView() {
		mParent = mInflater.inflate(R.layout.filteractionmenuitem, null);
		mParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showPopup();
			}
		});
		return mParent;
	}
}
