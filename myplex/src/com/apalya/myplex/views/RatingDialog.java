package com.apalya.myplex.views;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
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

public class RatingDialog {
	public Context mContext;
	public String mHeader = new String();
	public String mMessageHeader = new String();
	public String mMessageHint = new String();
	public TextView mHeadingTextView;
	public TextView mMessageHeadingTextView;
	public TextView mMessageRatingLow;
	public TextView mMessageRatingHigh;
	public Button mCancelButton;
	public Button mOkButton;
	public EditText mMessageBox;
	public SeekBar mRatingBar;
	public ProgressBar mProgressBar;
	
	public MessagePostCallback mListener;
	public RatingDialog(Context context){
		mContext = context;
	}
	public void prepareRating(){
		mHeader = mContext.getResources().getString(R.string.ratingheading);
		mMessageHeader = mContext.getResources().getString(R.string.ratingmessageheading);
		mMessageHint = mContext.getResources().getString(R.string.ratingmessagehint);
	}
	public void prepareFeedback(){
		mHeader = mContext.getResources().getString(R.string.feedbackheading);
		mMessageHeader = mContext.getResources().getString(R.string.feedbackmessageheading);
		mMessageHint = mContext.getResources().getString(R.string.feedbackmessagehint);
	}
	public void showDialog(MessagePostCallback listener,final CardData data){
		mListener= listener;
		final CustomDialog dialog = new CustomDialog(mContext);
		dialog.setContentView(R.layout.feedbacklayout);
		mHeadingTextView  = (TextView)dialog.findViewById(R.id.feedback_heading);
		mHeadingTextView.setTypeface(FontUtil.Roboto_Medium);
		mHeadingTextView.setText(mHeader);
		mMessageHeadingTextView = (TextView)dialog.findViewById(R.id.feedback_messageheading);
		mMessageHeadingTextView.setTypeface(FontUtil.Roboto_Medium);
		mMessageHeadingTextView.setText(mMessageHeader);
		mMessageRatingLow = (TextView)dialog.findViewById(R.id.feedback_ratingtext_low);
		mMessageRatingLow.setTypeface(FontUtil.ss_symbolicons_line);
		mMessageRatingHigh = (TextView)dialog.findViewById(R.id.feedback_ratingtext_high);
		mMessageRatingHigh.setTypeface(FontUtil.ss_symbolicons_line);
		mCancelButton = (Button)dialog.findViewById(R.id.feedback_cancel_button);
		mCancelButton.setTypeface(FontUtil.Roboto_Medium);
		mOkButton = (Button)dialog.findViewById(R.id.feedback_ok_button);
		mOkButton.setTypeface(FontUtil.Roboto_Medium);
		mMessageBox = (EditText)dialog.findViewById(R.id.feedback_messagebox);
		mMessageBox.setHint(mMessageHint);
		mMessageBox.setTypeface(FontUtil.Roboto_Medium);
		mRatingBar = (SeekBar)dialog.findViewById(R.id.feedback_ratingbar);
		mProgressBar = (ProgressBar)dialog.findViewById(R.id.feedback_progressbar);
		mOkButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MessagePost post = new MessagePost();
				mProgressBar.setVisibility(View.VISIBLE);
				post.sendComment(mMessageBox.getEditableText().toString(), mRatingBar.getProgress(), data,MessagePost.POST_RATING, new MessagePostCallback() {
					
					@Override
					public void sendMessage(boolean status) {
						dialog.dismiss();
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
