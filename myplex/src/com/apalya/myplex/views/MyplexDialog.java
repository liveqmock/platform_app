package com.apalya.myplex.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.utils.AlertDialogUtil.NoticeDialogListener;
import com.apalya.myplex.utils.FontUtil;

public class MyplexDialog implements OnClickListener{
	
	private Context mContext;
	private TextView titleTextView,msgTextView;
	Button leftBtn,rightBtn; 
	private Dialog mDialog;
	private NoticeDialogListener listener;


	public MyplexDialog(Context  mContext , String title,String message,String leftbutton,String rightButton,NoticeDialogListener listener) {
		
		this.mContext  =  mContext;
		this.listener = listener;
		mDialog = new Dialog(mContext);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.myplex_dialog_layout);
		mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mDialog.setCanceledOnTouchOutside(false);
		
		titleTextView = (TextView)mDialog.findViewById(R.id.dialog_title);
		msgTextView = (TextView)mDialog.findViewById(R.id.dialog_msg);
		leftBtn = (Button)mDialog.findViewById(R.id.dialog_left_btn);
		rightBtn = (Button)mDialog.findViewById(R.id.dialog_right_btn);
		titleTextView.setTypeface(FontUtil.Roboto_Regular);
		msgTextView.setTypeface(FontUtil.Roboto_Regular);
		leftBtn.setTypeface(FontUtil.Roboto_Regular);
		
		this.rightBtn.setTypeface(FontUtil.Roboto_Regular);
		
		this.leftBtn.setOnClickListener(this);
		this.rightBtn.setOnClickListener(this);
		
		if(title !=null){
			this.titleTextView.setText(title);
		}
		if(message!=null){
			this.msgTextView.setText(message);
		}
		if(leftbutton!=null){
			this.leftBtn.setText(leftbutton);
		}
		if(rightButton!=null){
			this.rightBtn.setText(rightButton);
		}		
	
	}
	
	public void showDialog(){
		mDialog.show();
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.dialog_left_btn){
			listener.onDialogOption1Click();
		}else if(v.getId() == R.id.dialog_right_btn){
			listener.onDialogOption2Click();
		}
		
		mDialog.dismiss();
	}

}
