package com.apalya.myplex.views;

import java.util.HashMap;
import java.util.Map;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.fragments.SetttingsFragment;
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
	public void prepareRating(String header, String messageHeader, String messageHint)
	{
		mHeader = header; mMessageHeader = messageHeader; mMessageHint = messageHint;
	}
	public void showDialog(MessagePostCallback listener,final CardData data){
		mListener= listener;
		final CustomDialog dialog = new CustomDialog(mContext);
		dialog.setContentView(R.layout.feedbacklayout);
		mHeadingTextView  = (TextView)dialog.findViewById(R.id.feedback_heading);
		mHeadingTextView.setTypeface(FontUtil.Roboto_Light);
		if(data._id.equalsIgnoreCase("0"))
		{
			mHeadingTextView.setText("Love using myplex?");
		}
		else
		{
			mHeadingTextView.setText(mHeader);	
		}
		
		mMessageHeadingTextView = (TextView)dialog.findViewById(R.id.feedback_messageheading);
		mMessageHeadingTextView.setTypeface(FontUtil.Roboto_Light);
		
		if(data._id.equalsIgnoreCase("0"))
		{
			mMessageHeadingTextView.setText("Share your experience");
		}
		else
		{
			mMessageHeadingTextView.setText(mMessageHeader);	
		}
		
		
		mMessageRatingLow = (TextView)dialog.findViewById(R.id.feedback_ratingtext_low);
		mMessageRatingLow.setTypeface(FontUtil.ss_symbolicons_line);
		mMessageRatingHigh = (TextView)dialog.findViewById(R.id.feedback_ratingtext_high);
		mMessageRatingHigh.setTypeface(FontUtil.ss_symbolicons_line);
		mCancelButton = (Button)dialog.findViewById(R.id.feedback_cancel_button);
		mCancelButton.setTypeface(FontUtil.Roboto_Regular);
		mOkButton = (Button)dialog.findViewById(R.id.feedback_ok_button);
		mOkButton.setTypeface(FontUtil.Roboto_Regular);
		mMessageBox = (EditText)dialog.findViewById(R.id.feedback_messagebox);
		mMessageBox.setHint(mMessageHint);
		mMessageBox.setTypeface(FontUtil.Roboto_Regular);
		mRatingBar = (SeekBar)dialog.findViewById(R.id.feedback_ratingbar);
		mRatingBar.setProgress(5);
		mProgressBar = (ProgressBar)dialog.findViewById(R.id.feedback_progressbar);
		mOkButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MessagePost post = new MessagePost();
				mProgressBar.setVisibility(View.VISIBLE);
				String str = mMessageBox.getEditableText().toString();
				if(str == null || str.length() == 0) {
					str = "Good";
				}
				CardDetailViewFactory.RATING_POSTED = mRatingBar.getProgress()+"";
				SetttingsFragment.RATING_POSTED = mRatingBar.getProgress()+"";
				SetttingsFragment.FEEDBACK_POSTED = str;
				//Analytics.mixPanelProvidedFeedback(str, mRatingBar.getProgress()+"");
				post.sendComment(mMessageBox.getEditableText().toString(), mRatingBar.getProgress(), data,MessagePost.POST_RATING, new MessagePostCallback() {
					
					@Override
					public void sendMessage(boolean status) {
						dialog.dismiss();
						if(mListener != null){
							mListener.sendMessage(status);
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
