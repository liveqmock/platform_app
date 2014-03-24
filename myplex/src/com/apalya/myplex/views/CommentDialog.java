package com.apalya.myplex.views;


import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MessagePost;
import com.apalya.myplex.utils.MessagePost.MessagePostCallback;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class CommentDialog {
	public Context mContext;
	public String mHeader = new String();
	public String mMessageHeader = new String();
	public String mMessageHint = new String();
	public TextView mHeadingTextView;
	public Button mCancelButton;
	public Button mOkButton;
	public EditText mMessageBox;
	public ProgressBar mProgressBar;
	
	
	public MessagePostCallback mListener;
	public CommentDialog(Context context){
		mContext = context;
	}
	public void showDialog(MessagePostCallback listener,final CardData data){
		mListener= listener;
		final CustomDialog dialog = new CustomDialog(mContext);
		dialog.setContentView(R.layout.commentlayout);
		mHeadingTextView  = (TextView)dialog.findViewById(R.id.feedback_heading);
		mHeadingTextView.setTypeface(FontUtil.Roboto_Light);
		
		
		
		mCancelButton = (Button)dialog.findViewById(R.id.feedback_cancel_button);
		mCancelButton.setTypeface(FontUtil.Roboto_Regular);
		mOkButton = (Button)dialog.findViewById(R.id.feedback_ok_button);
		mOkButton.setTypeface(FontUtil.Roboto_Regular);
		
		mMessageBox = (EditText)dialog.findViewById(R.id.feedback_messagebox);
		mMessageBox.setHint(mMessageHint);
		mMessageBox.setTypeface(FontUtil.Roboto_Regular);
		mProgressBar = (ProgressBar)dialog.findViewById(R.id.feedback_progressbar);
		mOkButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MessagePost post = new MessagePost();
				mProgressBar.setVisibility(View.VISIBLE);
				mOkButton.setEnabled(false);
				mOkButton.setText(mContext.getString(R.string.comment_ok_adding));
				post.sendComment(mMessageBox.getEditableText().toString(), 0, data,MessagePost.POST_COMMENT, new MessagePostCallback() {
					
					@Override
					public void sendMessage(boolean status) {
						dialog.dismiss();
						if(mListener != null){
							mListener.sendMessage(status);
							
							//Analytics.COMMENT_POSTED = mMessageBox.getEditableText().toString();
						}
					}
				});
				
			}
		});
		mCancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if(mListener != null){
					mListener.sendMessage(false);
				}
			}
		});
		dialog.show();
	}
	
}
