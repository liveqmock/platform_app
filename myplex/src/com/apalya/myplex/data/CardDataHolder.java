package com.apalya.myplex.data;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public class CardDataHolder {
	public TextView mTitle;
	public RelativeLayout mTitleLayout;
	public ImageView mDelete;
	public ImageView mFavourite;
	public NetworkImageView mPreview;
	public ImageView mOverLayPlay;
	public ImageView mComments;
	public ImageView mReviews;
	public TextView mCommentsText;
	public TextView mReviewsText;
	public TextView mRentText;
	public LinearLayout mRentLayout;
	public ProgressBar mFavProgressBar;
	public CardData mDataObject;
}
