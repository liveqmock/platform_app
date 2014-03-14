package com.apalya.myplex.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;

public class DownloadStreamDialog  implements OnClickListener
{
	private Dialog dialog;
	private RadioButton best,good,always_ask;
	private Button continuee;
	private TextView titleTV,bestTvMsg;
	private CheckBox dontAsk;
	private DownloadListener listener;
	private Context context;
	private LinearLayout dontAskLayout,alwaysAskLayout;
	private Handler handler = new Handler();
	
	public DownloadStreamDialog(Context context,String title) 
	{
		this.context = context;
		dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.stream_or_download_dialog_layout);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setCanceledOnTouchOutside(false);

		best = (RadioButton)dialog.findViewById(R.id.best_radio_btn);
		good = (RadioButton)dialog.findViewById(R.id.good_radio_btn);
		continuee = (Button)dialog.findViewById(R.id.dialog_continue);
		titleTV  = (TextView)dialog.findViewById(R.id.dialog_title);
		dontAsk = (CheckBox)dialog.findViewById(R.id.dont_ask_me_again);
		always_ask = (RadioButton)dialog.findViewById(R.id.always_ask_radio_btn);
		dontAskLayout = (LinearLayout)dialog.findViewById(R.id.dont_ask_layout);
		alwaysAskLayout = (LinearLayout)dialog.findViewById(R.id.always_ask_layout);

		FontUtil.loadFonts(context.getAssets());
		titleTV.setTypeface(FontUtil.Roboto_Regular);
		titleTV.setText(title.toLowerCase());
		bestTvMsg = ((TextView)dialog.findViewById(R.id.best_tv_msg));
		bestTvMsg.setTypeface(FontUtil.Roboto_Light);
		((TextView)dialog.findViewById(R.id.good_tv_msg)).setTypeface(FontUtil.Roboto_Light);
		best.setTypeface(FontUtil.Roboto_Regular);
		good.setTypeface(FontUtil.Roboto_Regular);
		continuee.setTypeface(FontUtil.Roboto_Regular);
		dontAsk.setTypeface(FontUtil.Roboto_Regular);
		((TextView)dialog.findViewById(R.id.dont_ask_msg)).setTypeface(FontUtil.Roboto_Light);
		
		
		always_ask.setOnClickListener(this);
		best.setOnClickListener(this);
		good.setOnClickListener(this);		
		continuee.setOnClickListener(this);		
		initValues();
	}

	private void initValues() {
		if(Util.getSpaceAvailable()<1.5){
			bestTvMsg.setText(context.getString(R.string.unable_download_msg));
			best.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
					best.setChecked(false);
				}
			});
			best.setOnClickListener(null);
		}
		if(alwaysAskLayout.getVisibility()  == View.VISIBLE){
			if(!(SharedPrefUtils.getBoolFromSharedPreference(context, context.getString(R.string.is_dont_ask_again,false)))){
				always_ask.setChecked(true);
				return;
			}
		}
		if(!SharedPrefUtils.getBoolFromSharedPreference(context, context.getString(R.string.isDownload),true)){
			good.setChecked(true);
			best.setChecked(false);
		}else{
			best.setChecked(true);
			good.setChecked(false);
		}	
	}

	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}

	public void showDialog() 
	{
		try{
			if(dialog!=null)
				dialog.show();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void dismissDialog() 
	{
		if(dialog!=null){
			dialog.dismiss();
			dialog = null;
		}	
	}




	@Override
	public void onClick(View view) {

		switch(view.getId()){
		case R.id.dialog_continue:
			dismissDialog();
			Thread sharedPrefWriterThread = new Thread(){
				public void run() {
					if(alwaysAskLayout.getVisibility() != View.GONE){
						if(always_ask.isChecked()){
							handler.post(new Runnable() {								
								@Override
								public void run() {
									listener.onOptionSelected(false);
								}
							});
							SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.is_dont_ask_again), false);
						}else{
							SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.is_dont_ask_again), true);
						}
					}
					if(dontAskLayout.getVisibility() != View.GONE){
						if(dontAsk.isChecked()){
							SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.is_dont_ask_again), true);
						}else{
							SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.is_dont_ask_again), false);
						}
					}
					if(best.isChecked()){				
						handler.post(new Runnable() {							
							@Override
							public void run() {
								listener.onOptionSelected(true);
							}
						});
						SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.isDownload), true);				
					}else if(good.isChecked()){
						handler.post(new Runnable() {							
							@Override
							public void run() {
								listener.onOptionSelected(false);
							}
						});
						SharedPrefUtils.writeToSharedPref(context, context.getString(R.string.isDownload), false);
					}					
				};
			};
			sharedPrefWriterThread.start();
			break;	
		case R.id.best_radio_btn:
			dontAsk.setChecked(true);
			if(!best.isChecked()){
				best.setChecked(false);
				good.setChecked(true);
			}else{
				best.setChecked(true);
				good.setChecked(false);
			}
			always_ask.setChecked(false);
			break;
		case R.id.good_radio_btn:
			dontAsk.setChecked(false);
			if(!good.isChecked()){
				good.setChecked(false);
				best.setChecked(true);
			}else{
				good.setChecked(true);
				best.setChecked(false);
			}			
			always_ask.setChecked(false);
			break;			

		case R.id.always_ask_radio_btn :
			good.setChecked(false);
			best.setChecked(false);
			break;

		}		
	}
	public void setAlwaysAskAsDefault() {
		always_ask.setChecked(false);
		best.setChecked(false);
		good.setChecked(false);	
	}
	public void showAlwaysAskOption(){
		dontAskLayout.setVisibility(View.GONE);
		alwaysAskLayout.setVisibility(View.VISIBLE);
		initValues();
	}

	public interface DownloadListener{
		void onOptionSelected(boolean isDownload);
	}
}
