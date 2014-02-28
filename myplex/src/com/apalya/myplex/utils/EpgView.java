package com.apalya.myplex.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.EPGRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.adapters.StringAdapter;
import com.apalya.myplex.adapters.EpgAdapter;
import com.apalya.myplex.adapters.EpgAdapter.ProgrammActionListener;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.receivers.ReminderReceiver;
import com.apalya.myplex.utils.AlertDialogUtil.NoticeDialogListener;
import com.apalya.myplex.views.CardVideoPlayer;

public class EpgView implements ProgrammActionListener{
	private CardData mData;
	private LayoutInflater mInflator;
	private View epgView;
	private Context mContext;
	private List<EpgContent> epgContents  = new ArrayList<EpgContent>();
	private static String TAG = "EpgView";	
	private CardVideoPlayer player;
	private Calendar calendar;
	private ListView programmList,dateList;
	private EpgAdapter adapter;
	private StringAdapter daysAdapter;
	private int progSelectedIndex = 0, dateSelectedIndex =0;
	private String days[] =  new String[9];
	
	public EpgView(CardData data,Context context) {
		mContext = context;
		mData  =  data;		
		mInflator = LayoutInflater.from(context);
		calendar = Calendar.getInstance();
	}

	public View createEPGView() {
		epgView  = mInflator.inflate(R.layout.carddetails_epg_view, null);
		dateList  = (ListView)epgView.findViewById(R.id.dateList);
		programmList  = (ListView)epgView.findViewById(R.id.programmList);
		
		programmList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		dateList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		epgView.setVisibility(View.GONE);
		setProgrammLoding();
		String dayString  = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);
		fetchEPGData(dayString.trim());
		fillDates();
		return epgView;
	}
	public void fetchEPGData(String date){
		setProgrammLoding();
		String requestString = "";
		Log.d(TAG,"title="+mData.generalInfo.title);
		if(mData.generalInfo.title.equalsIgnoreCase("headlines today"))
			 requestString = ConsumerApi.getEpgUrl(mData.generalInfo.title.replace(" ", ""), "1", date);
		else
			 requestString = ConsumerApi.getEpgUrl(mData.generalInfo.title.trim().split(" ")[0], "1", date);
		Log.d(TAG ,requestString);
		EPGRequest request = new EPGRequest(requestString,new OnEPGfetched(), new OnEPGFetchFailed());
		request.setShouldCache(false);
		RequestQueue queue = MyVolley.getRequestQueue();
		queue.add(request);
		queue.start();
	}
	

	private void setProgrammLoding() {
		List<EpgContent> contents = new ArrayList<EpgContent>();
		for(int i=0;i<3;i++){
			EpgContent content = new EpgContent("Loading", "", "", "", "");
			contents.add(content);
		}
		adapter = new EpgAdapter(mContext, contents );
		programmList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
//		adapter.notifyDataSetInvalidated();
	}

	private void fillDates() {
		
		for(int i=-3;i<=3;i++){
			
			days[i+4] = getMonthAndDate(i);
		}		
		days[0] = "";
		days[8] = "";

		daysAdapter = new StringAdapter(mContext, days);
		
		/*dateList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,long id) {
				
				final int position1 = position;
				dateList.smoothScrollToPosition(position-1);
				handler.postDelayed(new Runnable() {
					public void run() {
						dateList.setItemChecked(position1, true);
					}
				}, 300);
				daysAdapter.setIndex(position);		
				
				
				String dateString =days[position];
				if(dateString.length()<=1)
					return;
				Calendar calendar = Calendar.getInstance();
				Scanner scanner = new Scanner(dateString).useDelimiter("[^0-9]+");
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(new SimpleDateFormat("MMM").parse(dateString.replaceAll("\\d","").trim()));
					int monthInt = cal.get(Calendar.MONTH) + 1;
					String dayString  = calendar.get(Calendar.YEAR)+"-"+monthInt+"-"+scanner.nextInt();
					fetchEPGData(dayString);
				} catch (ParseException e) {
					e.printStackTrace();
				}catch (Exception e) {
				}
			}			
		});*/
		dateList.setOnScrollListener(new AbsListView.OnScrollListener() {			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == SCROLL_STATE_IDLE){						
					dateSelectedIndex = dateList.getFirstVisiblePosition()+2;
					dateList.smoothScrollToPosition(dateSelectedIndex-2);
					handler.postDelayed(new Runnable() {
						public void run() {
							dateList.setItemChecked(dateSelectedIndex, true);
						}
					}, 300);
					Calendar cal = Calendar.getInstance();
					daysAdapter.setIndex(dateSelectedIndex);
					String dateString =days[dateList.getFirstVisiblePosition()+1];
					Calendar calendar = Calendar.getInstance();
					Scanner scanner = new Scanner(dateString).useDelimiter("[^0-9]+");
					try {
					cal.setTime(new SimpleDateFormat("MMM").parse(dateString.replaceAll("\\d","").trim()));
					int monthInt = cal.get(Calendar.MONTH) + 1;
					String dayString  = calendar.get(Calendar.YEAR)+"-"+monthInt+"-"+scanner.nextInt();
					Toast.makeText(mContext, dayString, Toast.LENGTH_SHORT).show();
					fetchEPGData(dayString);
					} catch (ParseException e) {						
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) 
			{
				
			}
		});
		/*dateList.setOnScrollListener(new AbsListView.OnScrollListener() {			
			private int totalVisibleItem;
			private int lastVisibleItem;
			private boolean isScrollingDown;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == SCROLL_STATE_IDLE){						
				
					
					int marginIndex=1;
					
					if(totalVisibleItem > 3 && lastVisibleItem !=0){
						marginIndex=isScrollingDown?3:2;
					}
					
					lastVisibleItem=0;
					dateSelectedIndex = dateList.getFirstVisiblePosition()+marginIndex;					
					
			
					dateList.smoothScrollToPosition(dateSelectedIndex-marginIndex);
					
					handler.postDelayed(new Runnable() {
						public void run() {
							dateList.setItemChecked(dateSelectedIndex, true);
						}
					}, 300);
					Calendar cal = Calendar.getInstance();
					daysAdapter.setIndex(dateSelectedIndex);
					String dateString =days[dateSelectedIndex];
					Calendar calendar = Calendar.getInstance();
					Scanner scanner = new Scanner(dateString).useDelimiter("[^0-9]+");
					try {
					cal.setTime(new SimpleDateFormat("MMM").parse(dateString.replaceAll("\\d","").trim()));
					int monthInt = cal.get(Calendar.MONTH) + 1;
					String dayString  = calendar.get(Calendar.YEAR)+"-"+monthInt+"-"+scanner.nextInt();
					fetchEPGData(dayString);
					} catch (ParseException e) {						
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) 
			{
				 if (firstVisibleItem > lastVisibleItem) {
				        isScrollingDown = true;
				    }
				    else {
				        isScrollingDown = false;
				    }
				    lastVisibleItem = firstVisibleItem;
				    
				Log.d(TAG,"totalItemCount:"+visibleItemCount);
				totalVisibleItem=visibleItemCount;
			}
		});*/
		
		dateList.setAdapter(daysAdapter);
		dateList.setSelection(3);
	}
	
	private String getMonthAndDate(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, days);
		String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
				Locale.getDefault());
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		return day+" "+month;
	}


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
//		fillProgrammes(contents,programmePicker);
		adapter = new EpgAdapter(mContext, contents);
		programmList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		int index = 0;
		for(EpgContent content : contents){
			Date startDate  =  getDate(content.StartTime);
			Date endDate	=  getDate(content.EndTime);
			
			if(startDate == null || endDate == null)
				continue;
			if(date.before(endDate) && date.after(startDate)){
//				programmePicker.setValue(index);
				programmList.setSelection(index);
				break;
			}				
			index++;
		}
		epgContents = contents;
		programmList.setOnScrollListener(new AbsListView.OnScrollListener() {			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == SCROLL_STATE_IDLE){
					if(epgContents.size()>2)
						progSelectedIndex = (programmList.getFirstVisiblePosition()+programmList.getLastVisiblePosition())/2;
					else
						progSelectedIndex = 0;
					
					adapter.setIndex(progSelectedIndex);
					
					Log.d(TAG,"assert url"+epgContents.get(progSelectedIndex).assetUrl);
					String assertUrl = epgContents.get(progSelectedIndex).assetUrl;
					String assetType = epgContents.get(progSelectedIndex).assetType;
				
					programmList.setSelection(progSelectedIndex-1);
					handler.postDelayed(new Runnable() {
						public void run() {
							programmList.setItemChecked(progSelectedIndex, true);
							
						}
					}, 300);
					
					if(player.isMediaPlaying())
						return;
					if(assetType.equals("1") && assertUrl!=null  && (!assertUrl.equalsIgnoreCase(mContext.getString(R.string.no_url)))){
						Log.d(TAG,"got url for playback ="+assertUrl);
						player.createRecordPlayView(assertUrl,epgContents.get(progSelectedIndex).Name);
					}else{
						player.removeRecordPay();
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				}
		});
		
		programmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			private int position1;

			@Override
			public void onItemClick(AdapterView<?> adpater, View view, int position,long id) {
				
				position1 = position;
				
				handler.postDelayed(new Runnable() {
					public void run() {
						programmList.setItemChecked(position1, true);
					}
				}, 300);
				
				
				final Date now  = new Date();
				final Date programmeTime  = getDate(epgContents.get(position1).StartTime);
				final Calendar prg = Calendar.getInstance();
				prg.setTime(programmeTime);
				
				Log.d(TAG,"title "+epgContents.get(position1).Name);
				if(!now.before(programmeTime)){
					Log.d(TAG,"assert url"+epgContents.get(position1).assetUrl);
					String assertUrl = epgContents.get(position1).assetUrl;
					String assetType = epgContents.get(position1).assetType;
					if(player.isMediaPlaying())
						return;
					if(assetType.equals("1") && assertUrl!=null  && (!assertUrl.equalsIgnoreCase(mContext.getString(R.string.no_url)))){
						Log.d(TAG,"got url for playback ="+assertUrl);
						player.removeProgrammeName();
						player.initPlayBack(assertUrl.trim());
					}else{
						player.removeRecordPay();
					}
					return;
				}
				AlertDialogUtil.showAlert(mContext, "Do you want to set reminder", "no","yes",  new NoticeDialogListener() {					
					@Override
					public void onDialogOption2Click() {
							
							Calendar calendar = Calendar.getInstance();
							calendar.set(Calendar.SECOND, prg.get(Calendar.SECOND));
							calendar.set(Calendar.MINUTE, prg.get(Calendar.MINUTE));
							calendar.set(Calendar.HOUR_OF_DAY, prg.get(Calendar.HOUR_OF_DAY));
							calendar.set(Calendar.DAY_OF_MONTH, prg.get(Calendar.DAY_OF_MONTH));

							EpgContent content = epgContents.get(position1);
							Intent alarmintent = new Intent(mContext, ReminderReceiver.class);
							alarmintent.putExtra("title",content.Name);
							alarmintent.putExtra("note","The programm is scheduled at "+getTime(getDate(content.StartTime)));
							alarmintent.putExtra("_id",mData._id);
							 
							PendingIntent sender = PendingIntent.getBroadcast(mContext, 0,
							alarmintent,PendingIntent.FLAG_UPDATE_CURRENT|  Intent.FILL_IN_DATA);
							
							 
							AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
							am.set(AlarmManager.RTC_WAKEUP, prg.getTimeInMillis(), sender);
					}
					
					@Override
					public void onDialogOption1Click() {
					}
				});				
			}			
		});		
	}
	
	public void removeEPGView(){
		epgView.setVisibility(View.GONE);
	}
	public Date getDate(String dateString){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");				
		Date date = null;
		try {
			date   = format.parse(dateString);
//			System.out.println(date);
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
	public void setCardVideoPlayer(CardVideoPlayer player){
		this.player = player;
	}
	private Handler handler = new Handler();

	@Override
	public void onProgrammSlected() {
		// TODO Auto-generated method stub
		
	}	
}
