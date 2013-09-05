package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.List;

import com.apalya.myplex.R;
import com.apalya.myplex.data.slidemenudata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class slidemenuadapter extends BaseAdapter{
	public List<slidemenudata> mDataList = new ArrayList<slidemenudata>();
	public Context mContext;
	public LayoutInflater mInflator;
	private int width;
	
	public slidemenuadapter(Context cxt){
		this.mContext = cxt;
		mInflator = LayoutInflater.from(cxt);
		width = (int) mContext.getResources().getDimension(R.dimen.slidemenugap);
	}
	public void setData(List<slidemenudata> mDataList){
		this.mDataList = mDataList;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mDataList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflator.inflate(R.layout.slideinmenuitem, null);
		TextView txt = (TextView)convertView.findViewById(R.id.slideinmenuitemtxt);
		txt.setText(mDataList.get(position).title);
		
		Animation animation = null;
		animation = new TranslateAnimation(width/2, 0,0, 0);
//		animation = new ScaleAnimation((float) 1.0, (float) 1.0,(float) 0, (float) 1.0);
		animation.setDuration(750);
		convertView.startAnimation(animation);
		return convertView;
	}

}
