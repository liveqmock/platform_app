package com.apalya.myplex.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.utils.EpgContent;
import com.apalya.myplex.utils.FontUtil;

public class EpgAdapter extends BaseAdapter {

	private Context context;
	private List<EpgContent> contents = new ArrayList<EpgContent>();
	private int currentIndex = 1;

	public EpgAdapter(Context context, List<EpgContent> contents) {

		this.context = context;
		this.contents = contents;
	}
	@Override
	public int getCount() {
		return contents.size();
	}

	@Override
	public Object getItem(int position) {
		return contents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.epg_item	, null);
		TextView title = (TextView)convertView.findViewById(R.id.title);
		TextView reminder = (TextView)convertView.findViewById(R.id.reminder);

//		title.setTextColor(Color.BLACK);
//		title.setTypeface(FontUtil.Roboto_Regular);
		if(currentIndex == position){
			title.setTextSize(12);
			title.setTypeface(FontUtil.Roboto_Bold);	
			convertView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.rounded));
		}else{
			title.setTypeface(FontUtil.Roboto_Light);
			title.setTextSize(10);
		}

		EpgContent content = contents.get(position);
		if(content.Name!=null && content.Name.length() > 0
				&& content.StartTime!=null && content.StartTime.length() > 0
				&& content.EndTime!=null && content.EndTime.length() > 0){	

			Date start = getDate(content.StartTime);
			SimpleDateFormat am_pm_Sdf = new SimpleDateFormat("hh:mm aa");
			String startTime = am_pm_Sdf.format(start);
			
			Date end = getDate(content.EndTime);
			String endTime = am_pm_Sdf.format(end);
			
			title.setText("("+startTime+" - "+ endTime +") "+content.Name);
			Date startDate  = getDate(content.StartTime);
			Date EndDate  = getDate(content.EndTime);
			reminder.setTypeface(FontUtil.ss_symbolicons_line);
			Date now  = new Date();		
			if(now.before(startDate)){
				reminder.setVisibility(View.VISIBLE);
				reminder.setText(context.getString(R.string.reminder_icon));			
			}else if(now.after(EndDate)){
				if(content.assetType!=null && content.assetType.equals("1") && content.assetUrl!=null 
						&& (!content.assetUrl.equalsIgnoreCase(context.getString(R.string.no_url)))){
					reminder.setVisibility(View.VISIBLE);
					reminder.setText(context.getString(R.string.play_icon));
				}else{
					reminder.setVisibility(View.GONE);
					//				reminder.setText(context.getString(R.string.play_icon));
				}
			}
		}else if(content.Name!=null && content.Name.length() > 0){
			title.setText(content.Name);			
		}
		

		return convertView;
	}


	public Date getDate(String dateString){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");				
		Date date = null;
		try {
			date   = format.parse(dateString);
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
	public void setIndex(int progSelectedIndex) {
		currentIndex = progSelectedIndex;
	}

	public interface ProgrammActionListener{
		void onProgrammSlected();
	}

}
