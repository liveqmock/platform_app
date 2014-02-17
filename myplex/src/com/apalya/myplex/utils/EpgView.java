package com.apalya.myplex.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.NumberPicker.OnValueChangeListener;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.EPGRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;

public class EpgView {
	private CardData mData;
	private LayoutInflater mInflator;
	private View epgView;
	private NumberPicker datePicker;
	private NumberPicker programmePicker;
	private Context mContext;
	private int newDateValue;
	private static int DELAY = 2000;// FOR date change listener
	
	public EpgView(CardData data,Context context) {
		mContext = context;
		mData  =  data;		
		mInflator = LayoutInflater.from(context);
	}

	public View createEPGView() {
		epgView  = mInflator.inflate(R.layout.carddetails_epg_view, null);
		datePicker = (NumberPicker)epgView.findViewById(R.id.datePicker);
		programmePicker = (NumberPicker)epgView.findViewById(R.id.programmPicker);		
		datePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		programmePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		
		epgView.setVisibility(View.GONE);		
		fetchEPGData(null);				
		return epgView;
	}
	public void fetchEPGData(String date){
		String requestString = ConsumerApi.getEpgUrl(mData.generalInfo.title.split(" ")[0], "1", date);
		Log.d("amlan",requestString);
		EPGRequest request = new EPGRequest(requestString,new OnEPGfetched(), new OnEPGFetchFailed());
		request.setShouldCache(false);
		RequestQueue queue = MyVolley.getRequestQueue();
		queue.add(request);
		queue.start();
	}
	

	private void fillDates(NumberPicker picker) {
		String days[] = new String[7];
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
				Locale.getDefault());
		for (int i = 0; i < 7; i++) {
			days[i] = (int) (day + i) + month;
		}
		picker.setMinValue(0);
		picker.setMaxValue(6);
		picker.setDisplayedValues(days);
		datePicker.setDividerPadding(10);
		datePicker.setOnValueChangedListener(new OnDatechangeListener());
		datePicker.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChange(NumberPicker picker, int arg1) {
				Log.d("amlan","scoll"+arg1);
			}
		});
	}
	
	protected void fillProgrammes(List<EpgContent> contents, NumberPicker programmePicker) {
		
		programmePicker.invalidate();
		programmePicker.requestLayout();
		
		String programmes[] = new String[contents.size()];
		for(int i=0;i<contents.size();i++){
			EpgContent content = contents.get(i);
			String startTime  = getTime(getDate(content.StartTime));
			String endTime  = getTime(getDate(content.EndTime));
			programmes[i] = content.Name+" ("+startTime+" - "+endTime+")";
		}
		int max = programmePicker.getMaxValue();
		if(programmes.length > max ){
			programmePicker.setMinValue(0);
			programmePicker.setValue(0);
			programmePicker.setDisplayedValues(programmes);
			programmePicker.setMaxValue(contents.size()-1);				
		}else{
			programmePicker.setMinValue(0);
			programmePicker.setValue(0);
			programmePicker.setMaxValue(contents.size()-1);
			programmePicker.setDisplayedValues(programmes);
		}
		programmePicker.setOnValueChangedListener(new ProgrammChangeListener());
	}
	private class OnDatechangeListener implements OnValueChangeListener{		
		@Override
		public void onValueChange(android.widget.NumberPicker picker, int oldVal,int newVal) {
			final NumberPicker picker2 = picker;
			newDateValue = newVal;
			handler.postDelayed(new Runnable() {				
				@Override
				public void run() {
					String values[] = picker2.getDisplayedValues();
					Calendar calendar = Calendar.getInstance();
					Scanner scanner = new Scanner(values[newDateValue]).useDelimiter("[^0-9]+");
					String dayString  = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+scanner.nextInt();
					setProgrammLoading();			
					fetchEPGData(dayString);					
				}
			}, DELAY);
		}
	};
	
	private class ProgrammChangeListener implements OnValueChangeListener{		
		@Override
		public void onValueChange(android.widget.NumberPicker picker, int oldVal,int newVal) {
			
		}
	};
	
	private class OnEPGfetched implements Listener<EpgResponse>{
			@Override
			public void onResponse(EpgResponse response) {
				if(response == null){
					removeEPGView();
					return;
				}else if( response.contents == null || response.contents.size()==0){
					removeEPGView();
					return;
				}
				List<EpgContent> contents = response.contents;	
//				Log.d("amlan","got response"+contents.size());
				showEPGData(contents);
						
			}
		}
	private class OnEPGFetchFailed implements  ErrorListener{
		@Override
		public void onErrorResponse(VolleyError error) {
			removeEPGView();
		}
	}
	
	private void showEPGData(List<EpgContent>  contents) {
		epgView.setVisibility(View.VISIBLE);
		Date date = new Date();
		fillDates(datePicker);
		fillProgrammes(contents,programmePicker);
		for(EpgContent content : contents){
			Date startDate  =  getDate(content.StartTime);
			Date endDate	=  getDate(content.EndTime);
			
			if(startDate == null || endDate == null)
				continue;
			
			if(date.before(endDate) && date.after(startDate)){
				epgView.setVisibility(View.VISIBLE);
			}				
		}	
	}
	
	public void removeEPGView(){
		epgView.setVisibility(View.GONE);
	}
	public Date getDate(String dateString){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");				
		Date date = null;
		try {
			date   = format.parse(dateString);
			System.out.println(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	public String getTime(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(date);
	}
	public void setProgrammLoading(){
		programmePicker.invalidate();
		programmePicker.requestLayout();
		
		programmePicker.setMinValue(0);
		programmePicker.setValue(0);
		programmePicker.setMaxValue(3);
		programmePicker.setDisplayedValues(new String[]{mContext.getString(R.string.loading),"",""});
	}
	
	private Handler handler = new Handler();
}
