package com.apalya.myplex.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.utils.FontUtil;

public class StringAdapter extends BaseAdapter {
	
	private Context context;
	private String[] days;
//	private ViewHolder holder;
//	private TextView day;
	private int presentIndex = 1;

	public StringAdapter(Context context, String[] days) {
		
		this.context = context;
		this.days = new String[days.length];
		this.days = days;
	}
	@Override
	public int getCount() {
		return days.length;
	}

	@Override
	public Object getItem(int position) {
		return days[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		if(convertView == null){
//			holder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.list_item_day	, null);
			TextView day = (TextView)convertView.findViewById(R.id.day_item);
//		}
//		day.setTextColor(Color.parseColor("#333333"));
//		day.setTextColor(Color.BLACK);	
		day.setTypeface(FontUtil.Roboto_Regular);
		day.setText(days[position]);
		
		if(presentIndex == position){
			day.setTypeface(FontUtil.Roboto_Bold);	
			day.setTextSize(12);
		}else{
			day.setTypeface(FontUtil.Roboto_Thin);
			day.setTextSize(10);
//			convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.wheel_bg));
		}
		/*day.setOnFocusChangeListener(new OnFocusChangeListener() {			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
			}
		});
		day.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				day.setTypeface(FontUtil.Roboto_Bold);
				day.setText("Hello");
			}
		});*/
		
		return convertView;
	}	
	/*private class ViewHolder{
		TextView day;	
	}*/
	public void setIndex(int dateSelectedIndex) {
		
		presentIndex = dateSelectedIndex; 
	}

}
