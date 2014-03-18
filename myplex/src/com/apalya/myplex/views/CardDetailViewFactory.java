package com.apalya.myplex.views;

import java.util.HashMap;
import java.util.Map;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.R;

import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataCertifiedRatingsItem;
import com.apalya.myplex.data.CardDataCommentsItem;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardDataRelatedCastItem;
import com.apalya.myplex.data.CardDataUserReviewsItem;
import com.apalya.myplex.data.CardDetailMultiMediaGroup;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.CircleImageLoader;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FetchCardField;
import com.apalya.myplex.utils.FetchCardField.FetchComplete;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MessagePost.MessagePostCallback;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.google.analytics.tracking.android.EasyTracker;

public class CardDetailViewFactory {
	
	public static final String SECTION_DETAILS = "Details";
	public static final String SECTION_DESCRIPTION = "Description";
	public static final String SECTION_MYPLEXDESCRITION = "Myplex Descrition";
	public static final String SECTION_STUDIODESCRITION = "Studio Descrition";
	public static final String SECTION_CREDITS = "Credits";
	public static final String SECTION_COMMENTS = "Comments";
	public static final String SECTION_RELATEDMULTIMEDIA = "Related Multimedia";
	
	public static String COMMENT_POSTED = null; //useful to getdata from MessagePost to CardDetailViewFactory
	public static String RATING_POSTED = null; //useful to getdata from MessagePost to CardDetailViewFactory
	
	private View mDetails;
	private View mDescription;
	private View mMyPlexDescription;
	private View mStudioDescription;
	private View mCredits;
	private View mExtras;
	private View mMultimedia;
	private View mComments;
	
	
	public interface CardDetailViewFactoryListener{
		public void onDescriptionExpanded();
		public void onCommentsExpanded();
		public void onCommentsCollapsed();
		public void onDescriptionCollapsed();
		public void onTextSelected(String key);
		public void onMediaGroupSelected(CardDetailMultiMediaGroup group);
		public void onSimilarContentAction();
		public void onFullDetailCastAction();
		public void onProgressBarVisibility(int value);
	}
	public void setOnCardDetailExpandListener(CardDetailViewFactoryListener listener){
		this.mListener = listener;
	}
//	private OnClickListener mOnMultiMediaExpand = new OnClickListener() {
//		
//		@Override
//		public void onClick(View v) {
//			if(mData == null){return;}
//			if(!mData.isExpanded){
//				if(mCardExpandListener != null){
//					mCardExpandListener.onExpanded();
//				}
//			}
//		}
//	}; 
	private CardDetailViewFactoryListener mListener;
	private Context mContext;
	private LayoutInflater mInflator;
	private CardData mData;
	

	public void SetData(CardData data){
		this.mData = data;
	}
	public CardDetailViewFactory(Context cxt) {
		this.mContext = cxt;
		mInflator = LayoutInflater.from(mContext);
	}

	public static final int CARDDETAIL_BRIEF_DESCRIPTION = 0;
	public static final int CARDDETAIL_FULL_DESCRIPTION = 1;
	public static final int CARDDETAIL_MYPLEX_DESCRIPTION = 2;
	public static final int CARDDETAIL_STUDI0_DESCRIPTION = 3;
	public static final int CARDDETAIL_CASTANDCREW = 4;
	public static final int CARDDETAIL_EPISODE = 5;
	public static final int CARDDETAIL_PROGRAMGUIDE = 6;
	public static final int CARDDETAIL_PLAYINPLACE = 7;
	public static final int CARDDETAIL_BREIF_RELATED_MULTIMEDIA = 8;
	public static final int CARDDETAIL_PAYABLE_RELATED_MULTIMEDIA = 9;
	public static final int CARDDETAIL_EXTRA_RELATED_MULTIMEDIA = 10;
	public static final int CARDDETAIL_COMMENTS = 11;
	public static final int CARDDETAIL_BRIEF_COMMENTS = 12;
	public static final int CARDDETAIL_WEBVIEW = 13;
	
	public View CreateView(CardData data, int type) {
		mData = data;
		switch (type) {
		case CARDDETAIL_BRIEF_DESCRIPTION:
			return createBriefDescriptionView();
		case CARDDETAIL_FULL_DESCRIPTION:
			return createFullDescriptionView();
		case CARDDETAIL_MYPLEX_DESCRIPTION:
			return createMyplexDescriptionView();
		case CARDDETAIL_STUDI0_DESCRIPTION:
			return createStudioDescriptionView();
		case CARDDETAIL_CASTANDCREW:
			return createCastCrewView();
		case CARDDETAIL_EPISODE:
			return createEpisodeView();
		case CARDDETAIL_PROGRAMGUIDE:
			return createProgramGuideView();
//		case CARDDETAIL_PLAYINPLACE:
//			return createPlayInPlaceView(data);
		case CARDDETAIL_BREIF_RELATED_MULTIMEDIA:
			return createBriefRelatedView();
		case CARDDETAIL_PAYABLE_RELATED_MULTIMEDIA:
			return createPlayableMultiMediaView();
		case CARDDETAIL_EXTRA_RELATED_MULTIMEDIA:
			return createExtraMultiMediaView();
		case CARDDETAIL_COMMENTS:
			if(mData._id.equalsIgnoreCase("0"))
			{
			return null;	
			}
			return createCommentsView();
		case CARDDETAIL_BRIEF_COMMENTS:
			if(mData._id.equalsIgnoreCase("0"))
			{
			return null;	
			}
			return createCommentsView();
		case CARDDETAIL_WEBVIEW:
			return createWebView();
		default:
			break;
		}
		return null;
	}

	public void scrollReachedEnd(){
		/*if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			if(!mRefreshInProgress){
				refreshSection();
			}
		}*/
	}
	public static final int  COMMENTSECTION_COMMENTS = 101;
	public static final int COMMENTSECTION_REVIEW = 102;
	private void fillCommentSectionData(int type,CardData card){
		
		mCommentContentLayout.removeAllViews();
		if(type == COMMENTSECTION_COMMENTS){
			if(card.comments == null){return ;}
			if(card.comments.values == null ){return ;}
			if(card.comments.values.size() == 0){return ;}
			for(CardDataCommentsItem commentsItem:card.comments.values){
				View child = mInflator.inflate(R.layout.carddetailcomment_data, null);
	//			VerticalLineRelativeLayout timelinelayout = (VerticalLineRelativeLayout)child.findViewById(R.id.timeLineLayout);
	//			timelinelayout.setWillNotDrawEnabled(false);
				TextView personName = (TextView)child.findViewById(R.id.carddetailcomment_personname);
				personName.setText(commentsItem.name);
				personName.setTypeface(FontUtil.Roboto_Regular);
				TextView commentTime = (TextView)child.findViewById(R.id.carddetailcomment_time);
				commentTime.setText(Util.getDate(commentsItem.timestamp));
				commentTime.setTypeface(FontUtil.Roboto_Regular);
				TextView commentMessage  = (TextView)child.findViewById(R.id.carddetailcomment_comment);
				commentMessage.setText(commentsItem.comment);
				commentMessage.setTypeface(FontUtil.Roboto_Regular);
	//			addSpace(layout, 16);
				mCommentContentLayout.addView(child);
				
				
			}
		}else if(type == COMMENTSECTION_REVIEW){
			if(card.userReviews == null){return ;}
			if(card.userReviews.values == null ){return ;}
			if(card.userReviews.values.size() == 0){return ;}
			for(CardDataUserReviewsItem reviewItem:card.userReviews.values){
				View child = mInflator.inflate(R.layout.carddetailcomment_data, null);
	//			VerticalLineRelativeLayout timelinelayout = (VerticalLineRelativeLayout)child.findViewById(R.id.timeLineLayout);
	//			timelinelayout.setWillNotDrawEnabled(false);
				TextView personName = (TextView)child.findViewById(R.id.carddetailcomment_personname);
				personName.setText(reviewItem.username);
				personName.setTypeface(FontUtil.Roboto_Regular);
				TextView commentTime = (TextView)child.findViewById(R.id.carddetailcomment_time);
				commentTime.setText(Util.getDate(reviewItem.timestamp));
				commentTime.setTypeface(FontUtil.Roboto_Regular);
				TextView commentMessage  = (TextView)child.findViewById(R.id.carddetailcomment_comment);
				commentMessage.setText(reviewItem.review);
				commentMessage.setTypeface(FontUtil.Roboto_Regular);
	//			addSpace(layout, 16);
				mCommentContentLayout.addView(child);
								
			}
		}
	}
	private LinearLayout mCommentContentLayout;
	private int mCurrentCommentViewType = COMMENTSECTION_COMMENTS;
	private View createCommentsView() {
		mComments = null;
//		if(mData.comments == null){return null;}
//		if(mData.comments.values == null ){return null;}
//		if(mData.comments.values.size() == 0){return null;}
		View v = mInflator.inflate(R.layout.carddetailbriefcomment, null);
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			v.setBackgroundResource(0);
		}
		mCommentContentLayout = (LinearLayout)v.findViewById(R.id.carddetailcomment_contentlayout);
//		LayoutTransition transition = new LayoutTransition();
//		transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
//		mCommentContentLayout.setLayoutTransition(transition);
		mComments = mCommentContentLayout;
		addSpace(mCommentContentLayout, (int)mContext.getResources().getDimension(R.dimen.margin_gap_16));
		fillCommentSectionData(COMMENTSECTION_COMMENTS,mData);
	
		LinearLayout commentLayout = (LinearLayout)v.findViewById(R.id.carddetailcomment_commentlayout);
		LinearLayout reviewLayout = (LinearLayout)v.findViewById(R.id.carddetailcomment_reviewlayout);
		
		final ImageView commentImage = (ImageView)v.findViewById(R.id.carddetailcomment_commentimage);
		final TextView commentHeading = (TextView)v.findViewById(R.id.carddetailcomment_commentheading);
		commentHeading.setTypeface(FontUtil.Roboto_Regular);
		final ImageView reviewImage = (ImageView)v.findViewById(R.id.carddetailcomment_reviewimage);
		final TextView reviewHeading = (TextView)v.findViewById(R.id.carddetailcomment_reviewheading);
		reviewHeading.setTypeface(FontUtil.Roboto_Regular);
		final Button editBox = (Button)v.findViewById(R.id.carddetailcomment_edittext);
		editBox.setTypeface(FontUtil.Roboto_Regular);
//		Util.showFeedback(editBox);
		editBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final String label = (String) editBox.getText();
				if(label.equalsIgnoreCase(mContext.getResources().getString(R.string.carddetailcommentsection_editcomment))){
					CommentDialog dialog = new CommentDialog(mContext);
					dialog.showDialog(new MessagePostCallback() {
						
						@Override
						public void sendMessage(boolean status) {
							if(status){
								Util.showToast(mContext, "Comment has been posted successfully.",Util.TOAST_TYPE_INFO);
								if(COMMENT_POSTED != null) {
									//mixPanelEneteredCommentsReviews(COMMENT_POSTED,"comment");
									Analytics.mixPanelEnteredCommentsReviews(mData,COMMENT_POSTED,"comment","0");
								}
								//final String label = (String) editBox.getText();
								//Toast.makeText(mContext, "Comment has posted successfully.", Toast.LENGTH_SHORT).show();
								mCurrentCommentViewType = COMMENTSECTION_COMMENTS;
								refreshSection();
							}else{
								//remove this
								if(COMMENT_POSTED != null) {
									Analytics.mixPanelEnteredCommentsReviews(mData,COMMENT_POSTED,"comment","0");
								}
								Util.showToast(mContext, "Unable to post your comment.",Util.TOAST_TYPE_ERROR);
//								Toast.makeText(mContext, "Unable to post your comment.", Toast.LENGTH_SHORT).show();
							}
						}
						
					}, mData);
				}else{
					RatingDialog dialog = new RatingDialog(mContext);
					if(mData.generalInfo.type.equalsIgnoreCase("live")){
						dialog.prepareRating(mData.generalInfo.title,
								mContext.getString(R.string.rate_this)+" "+mData.generalInfo.title,
								mContext.getString(R.string.add_live_tv_review)
								);
					}else{
						dialog.prepareRating();
					}
					dialog.showDialog(new MessagePostCallback() {
						
						@Override
						public void sendMessage(boolean status) {
							if(status){
								Util.showToast(mContext, "Review has posted successfully.",Util.TOAST_TYPE_INFO);
								if(COMMENT_POSTED != null) {
									Analytics.mixPanelEnteredCommentsReviews(mData,COMMENT_POSTED,"review",RATING_POSTED);
								}
//								Toast.makeText(mContext, "Review has posted successfully.", Toast.LENGTH_SHORT).show();
								mCurrentCommentViewType = COMMENTSECTION_REVIEW;
								refreshSection();
							}else{
								//remove this
								if(COMMENT_POSTED != null) {
									Analytics.mixPanelEnteredCommentsReviews(mData,COMMENT_POSTED,"review",RATING_POSTED);
								}
								Util.showToast(mContext, "Unable to post your review.",Util.TOAST_TYPE_ERROR);
//								Toast.makeText(mContext, "Unable to post your review.", Toast.LENGTH_SHORT).show();
							}
						}
						
					}, mData);
				}
			}
		});
		
		commentImage.setImageResource(R.drawable.card_iconcommentblue);
		commentHeading.setTextColor(mContext.getResources().getColor(R.color.theme_text_selector));
		commentLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				commentImage.setImageResource(R.drawable.card_iconcommentblue);
				commentHeading.setTextColor(mContext.getResources().getColor(R.color.theme_text_selector));
				reviewImage.setImageResource(R.drawable.card_iconuser);
				reviewHeading.setTextColor(Color.parseColor("#000000"));
				editBox.setText(R.string.carddetailcommentsection_editcomment);
//				editBox.setOnClickListener(null);
//				fillCommentSectionData(COMMENTSECTION_COMMENTS,mData);
				mCurrentCommentViewType = COMMENTSECTION_COMMENTS;
				refreshSection();
			}
		});
		
		

		reviewLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				commentImage.setImageResource(R.drawable.card_iconcomment);
				commentHeading.setTextColor(Color.parseColor("#000000"));
				reviewImage.setImageResource(R.drawable.card_iconuserblue);
				reviewHeading.setTextColor(mContext.getResources().getColor(R.color.theme_text_selector));
				editBox.setText(R.string.carddetailcommentsection_editreview);
//				editBox.setOnClickListener(mRateListener);
//				fillCommentSectionData(COMMENTSECTION_REVIEW,mData);
				mCurrentCommentViewType = COMMENTSECTION_REVIEW;
				refreshSection();
			}
		});
		mCommentSectionProgressBar  = (ProgressBar)v.findViewById(R.id.carddetailcomment_progressBar);
//		
//		mCommentRefresh = (ImageView)v.findViewById(R.id.carddetailcomment_expand);
//		RelativeLayout commentRefreshlayout = (RelativeLayout)v.findViewById(R.id.carddetailcomment_expandlayout);
//		Util.showFeedback(commentRefreshlayout);
//		commentRefreshlayout.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				refreshSection(type);
//			}
//		});
		refreshSection();
		
		
		
		return v;
	}
	private ImageView mCommentRefresh;
	private ProgressBar mCommentSectionProgressBar;
	private void rotateRefresh(){
		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			if(mListener != null){
				mListener.onProgressBarVisibility(View.VISIBLE);
			}
			return;
		}
		
		mCommentSectionProgressBar.setVisibility(View.VISIBLE);
//		 Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
//		 mCommentRefresh.startAnimation(animation);
	}
	private void stopRefresh(){
		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			if(mListener != null){
				mListener.onProgressBarVisibility(View.INVISIBLE);
			}
			return;
		}
		mCommentSectionProgressBar.setVisibility(View.GONE);
//		 Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.stoprotate);
//		 mCommentRefresh.startAnimation(animation);
	}
	private boolean mRefreshInProgress = false;
	private void refreshSection() {
		mRefreshInProgress = true;
		rotateRefresh();
		String FieldName = new  String();
		if(mCurrentCommentViewType == COMMENTSECTION_COMMENTS){
			FieldName = ConsumerApi.FIELD_COMMENTS;
		}else{
			FieldName = ConsumerApi.FIELD_USERREVIEWS;
		}
		FetchCardField fetch = new FetchCardField();
		fetch.Fetch(mData, FieldName, new FetchComplete() {
			
			@Override
			public void response(CardResponseData data) {
				mRefreshInProgress = false;
				stopRefresh();
				if(data == null){return;}
				if(data.results == null){return;}
				if(data.results.size() == 0 ){return;}
				CardData cardData = data.results.get(0);
				fillCommentSectionData(mCurrentCommentViewType,cardData);
			}
		});
		
	}

	private View createExtraMultiMediaView() {
		// TODO Auto-generated method stub
		return null;
	}

	private View createPlayableMultiMediaView() {
		// TODO Auto-generated method stub
		return null;
	}
	private View createSimillarContent(){
		if(mData.similarContent == null){return null;}
		if(mData.similarContent.values == null ){return null;}
		if(mData.similarContent.values.size() == 0 ){return null;}
		CardData simillarData = mData.similarContent.values.get(0);
		View child = mInflator.inflate(R.layout.carddetails_fullmultimediaitem, null);
		TextView groupname = (TextView)child.findViewById(R.id.carddetailmultimedia_groupname);
		if(mData._id.equalsIgnoreCase("0"))
		{
			groupname.setText(mContext.getString(R.string.lastwatchedcontent));
		}
		else
		{
			groupname.setText(mContext.getString(R.string.similarcontent));	
		}
		
		groupname.setTypeface(FontUtil.Roboto_Medium);
		TextView secondaryname = (TextView)child.findViewById(R.id.carddetailmultimedia_secondaryname);
		secondaryname.setTypeface(FontUtil.Roboto_Medium);
//		secondaryname.setText("description for smillar content");
		ImageView view  = (ImageView)child.findViewById(R.id.carddetailmultimedia_stackview);
//		Random rnd = new Random();
//		int Low = 100;
//		int High = 196;
//		int cardColor = Color.argb(255, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low, rnd.nextInt(High-Low)+Low);
//		view.setBackgroundColor(cardColor);
		
		child.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mListener != null){
					mListener.onSimilarContentAction();
				}
			}
		});
		String requesturl = null;
		if(simillarData.images != null){
			for(CardDataImagesItem imageItem:simillarData.images.values){
				if(imageItem.profile != null && imageItem.profile.equalsIgnoreCase("xxhdpi")){
					if (imageItem.link != null && !(imageItem.link.compareTo("Images/NoImage.jpg") == 0)) {
						requesturl = imageItem.link;
					}
					break;
				}
			}
		}
		if(requesturl !=null){
			CircleImageLoader imageLoader = new CircleImageLoader();
			imageLoader.loadImage(mContext, view, requesturl);
		}
		return child;
	}
	
	private View createBriefRelatedView() {
		mMultimedia = null;	 
		boolean itemsAdded = false;
		View v = mInflator.inflate(R.layout.carddetailmedia, null);
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			v.setBackgroundResource(0);
		}
		TextView text = (TextView)v.findViewById(R.id.carddetailmedia_title);
		text.setTypeface(FontUtil.Roboto_Light);
			
		ImageView expand = (ImageView)v.findViewById(R.id.carddetailmedia_expand);
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			expand.setVisibility(View.INVISIBLE);
		}
//		expand.setOnClickListener(mOnMultiMediaExpand);
//		Util.showFeedback(expand);
		LinearLayout contentLayout = (LinearLayout)v.findViewById(R.id.carddetailmedia_contentlayout);
		// This line is making unnessary HTTP request so no need to create a http request.
//		View simillarView  = createSimillarContent();
		View simillarView  = createDummySimilarContent(); 
		if(simillarView != null){
			contentLayout.addView(simillarView);
			mMultimedia = simillarView;
			itemsAdded = true;
		}
//		for(CardDetailMultiMediaGroup group:mData.mMultiMediaGroup){
//			View child = mInflator.inflate(R.layout.carddetails_fullmultimediaitem, null);
//			TextView groupname = (TextView)child.findViewById(R.id.carddetailmultimedia_groupname);
//			groupname.setText(group.groupName);
//			groupname.setTypeface(FontUtil.Roboto_Regular);
//			TextView secondaryname = (TextView)child.findViewById(R.id.carddetailmultimedia_secondaryname);
//			
//			FadeInNetworkImageView imageView1 = (FadeInNetworkImageView)child.findViewById(R.id.carddetailmultimedia_stackview1);
//			imageView1.setImageUrl(group.mList.get(0).mThumbnailUrl, MyVolley.getImageLoader());
//			
//			FadeInNetworkImageView imageView2 = (FadeInNetworkImageView)child.findViewById(R.id.carddetailmultimedia_stackview2);
//			imageView2.setImageUrl(group.mList.get(1).mThumbnailUrl, MyVolley.getImageLoader());
//			
//			imageView1.setTag(group);
//			imageView1.setOnClickListener(mMediaGroupClickListener);
//			contentLayout.addView(child);
//		}
		if(itemsAdded){
			return v;
		}
		return v;
	}
	private View createDummySimilarContent() 
	{
		View similarContentDummyView = mInflator.inflate(R.layout.carddetails_fullmultimediaitem, null);
		
		TextView groupname = (TextView)similarContentDummyView.findViewById(R.id.carddetailmultimedia_groupname);
		groupname.setText(mContext.getString(R.string.similarcontent));		
		groupname.setTypeface(FontUtil.Roboto_Medium);
		
		TextView secondaryname = (TextView)similarContentDummyView.findViewById(R.id.carddetailmultimedia_secondaryname);
		secondaryname.setTypeface(FontUtil.Roboto_Medium);
		ImageView similarImage  = (ImageView)similarContentDummyView.findViewById(R.id.carddetailmultimedia_stackview);
		if( mData.images.values != null ||  mData.images.values.size() > 0){
		String link  = mData.images.values.get(0).link;		
			if(link!=null){
				CircleImageLoader imageLoader = new CircleImageLoader();
				imageLoader.loadImage(mContext, similarImage, link);
			}
		}
		if(mData._id.equalsIgnoreCase("0"))
		{
			groupname.setText(mContext.getString(R.string.lastwatchedcontent));
		}
		else
		{
			if(mData.generalInfo.type.equalsIgnoreCase("live")){				
				groupname.setText(mContext.getString(R.string.similar_live_tv));	
			}else if(mData.generalInfo.type.equalsIgnoreCase("movie")){
				groupname.setText(mContext.getString(R.string.similar_movies));
			}else{
				groupname.setText(mContext.getString(R.string.similarcontent));
			}
		}
	
		if(mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(ConsumerApi.CONTENT_SPORTS_LIVE)){
			//groupname.setText(mContext.getString(R.string.similarcontent_sportslive));	
		}
		
		similarContentDummyView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mListener != null){
					mListener.onSimilarContentAction();
				}
			}
		});
		
		return similarContentDummyView;
	}
	private void addSpace(ViewGroup v,int space){
		Space gap = new Space(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,space);
		gap.setLayoutParams(params);
		v.addView(gap);
	}
	private View addSepartor(){
		View v = new View(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.margin_gap_2));
		params.topMargin = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
		v.setBackgroundColor(Color.parseColor("#efefef"));
		v.setLayoutParams(params);
		return v;
	}
	private void createPlayInPlaceView(LinearLayout layout) {
		layout.addView(addSepartor());
		LinearLayout imagelayout = new LinearLayout(mContext);
		mExtras = imagelayout;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		params.topMargin = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
		params.leftMargin = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
		params.rightMargin = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
		imagelayout.setLayoutParams(params);
		int width , height = 100;
		
		width = myplexapplication.getApplicationConfig().screenWidth - ((2*(int)mContext.getResources().getDimension(R.dimen.margin_gap_12))+(int)mContext.getResources().getDimension(R.dimen.margin_gap_8));
		width = width/2;
		height = (width * 9)/16; 
		{
		FadeInNetworkImageView v = (FadeInNetworkImageView)mInflator.inflate(R.layout.cardmediasubitemimage, null);
		
		LinearLayout.LayoutParams Imageparams = new LinearLayout.LayoutParams(width,height);
		v.setLayoutParams(Imageparams);
		v.setImageUrl("https://lh6.googleusercontent.com/-HEeoO3k3bPg/S0VKWAJUlbI/AAAAAAAAAik/k1x42L8UIvw/Movie-GhostRider-001.jpg", MyVolley.getImageLoader());
		imagelayout.addView(v);
		}
		{
			FadeInNetworkImageView v = (FadeInNetworkImageView)mInflator.inflate(R.layout.cardmediasubitemimage, null);
			
			LinearLayout.LayoutParams Imageparams = new LinearLayout.LayoutParams(width,height);
			Imageparams.leftMargin = 8;
			v.setLayoutParams(Imageparams);
			
			v.setImageUrl("https://lh4.googleusercontent.com/-16Op5dZqK4s/STQf00CgLaI/AAAAAAAAAS4/y94XF3tvI2o/Blog1000-Which-way-india-stn.jpg", MyVolley.getImageLoader());
			imagelayout.addView(v);
			}
		
		layout.addView(imagelayout);
		docketVideoWidget videoWidget = new docketVideoWidget(mContext);
//		layout.addView(videoWidget.CreateView(mData.mPlayinPlaceList.get(0)));
		
		{
			LinearLayout imagelayout1 = new LinearLayout(mContext);
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.topMargin = (int)mContext.getResources().getDimension(R.dimen.margin_gap_8);
			params1.leftMargin = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
			params1.rightMargin = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
			imagelayout1.setLayoutParams(params1);

			
			{
				FadeInNetworkImageView v = (FadeInNetworkImageView)mInflator.inflate(R.layout.cardmediasubitemimage, null);
				
				LinearLayout.LayoutParams Imageparams1 = new LinearLayout.LayoutParams(width,height);
				Imageparams1.leftMargin = 8;
				v.setLayoutParams(Imageparams1);
				
				v.setImageUrl("https://lh4.googleusercontent.com/-16Op5dZqK4s/STQf00CgLaI/AAAAAAAAAS4/y94XF3tvI2o/Blog1000-Which-way-india-stn.jpg", MyVolley.getImageLoader());
				imagelayout1.addView(v);
				}
			layout.addView(imagelayout1);
		}
		
//		for(CardDetailMediaData media:mData.mPlayinPlaceList){
//			if(media.mThumbnailMime != null && media.mThumbnailMime =="Image/JPEG"){
//				
//				FadeInNetworkImageView v = (FadeInNetworkImageView)mInflator.inflate(R.layout.cardmediasubitemimage, null);
//				v.setImageUrl(media.mThumbnailUrl, MyVolley.getImageLoader());
//				int width , height = 100;
//				
//				width = myplexUtils.mScreenWidth - (int)(mContext.getResources().getDimension(R.dimen.margin_gap_48) +mContext.getResources().getDimension(R.dimen.margin_gap_24));
//				height = (width * 9)/16; 
//				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
//				v.setLayoutParams(params);
//				
//				layout.addView(v);
//			}else{
//				docketVideoWidget videoWidget = new docketVideoWidget(mContext);
//				layout.addView(videoWidget.CreateView(media));
//			}
//			addSpace(layout,(int)mContext.getResources().getDimension(R.dimen.margin_gap_4));
//		}
	}
	
	private View createProgramGuideView() {
		// TODO Auto-generated method stub
		return null;
	}

	private View createEpisodeView() {
		// TODO Auto-generated method stub
		return null;
	}

	private View createCastCrewView() {
		mCredits = null;
		if(mData.relatedCast == null){return null;}
		if(mData.relatedCast.values == null){return null;}
		if(mData.relatedCast.values.size() == 0){return null;}
		View v = (LinearLayout)mInflator.inflate(R.layout.carddetailcastcrew, null);
	
		TextView title = (TextView)v.findViewById(R.id.carddetailcastandcrew_credits);
		title.setTypeface(FontUtil.Roboto_Light);
		mCredits = title;
		LinearLayout leftLayout = (LinearLayout)v.findViewById(R.id.carddetailcastcrew_leftlayout);
		LinearLayout rightLayout = (LinearLayout)v.findViewById(R.id.carddetailcastcrew_rightlayout);
		RelativeLayout fullDetailLayout = (RelativeLayout)v.findViewById(R.id.carddetailcastcrew_fulldetails);
		fullDetailLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mListener != null){
					mListener.onFullDetailCastAction();
				}
			}
		});
		for(CardDataRelatedCastItem relatedCastItem: mData.relatedCast.values){
			
			String LeftString = new String();
			if(relatedCastItem.roles != null && relatedCastItem.roles.size() > 0){
				for(String role:relatedCastItem.roles){
					if(LeftString.length() > 0){
						LeftString = ",";
					}
					LeftString += role;
				}
			}else{
				for(String role:relatedCastItem.types){
					if(LeftString.length() > 0){
						LeftString = ",";
					}
					LeftString += role;
				}
			}
			TextView leftText = new TextView(mContext);
			LinearLayout.LayoutParams leftparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			leftparams.gravity = Gravity.RIGHT;
			leftText.setLayoutParams(leftparams);
			leftText.setSingleLine();
			leftText.setTextSize(12);
			leftText.setTextColor(Color.parseColor("#4b4b4c"));
			
			TextView rightText = new TextView(mContext);
			LinearLayout.LayoutParams rightparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			rightText.setLayoutParams(rightparams);
			rightText.setTextSize(12);
			rightText.setTextColor(Color.parseColor("#4b4b4c"));
			
			leftText.setText(relatedCastItem.name);
			leftText.setTypeface(FontUtil.Roboto_Regular);
			Util.showFeedback(leftText);
			leftText.setTag( relatedCastItem.name);
			leftText.setOnClickListener(mCastandCrewClickListener);
//			leftText.setPaintFlags(leftText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			rightText.setText(LeftString);
			rightText.setSingleLine();
			rightText.setTypeface(FontUtil.Roboto_Regular);
//			rightText.setPaintFlags(rightText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			leftLayout.addView(leftText);
			rightLayout.addView(rightText);
		}
		return v;
	}
	private OnClickListener mCastandCrewClickListener = new  OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mListener != null && v.getTag() instanceof String){
				mListener.onTextSelected((String)v.getTag());
			}
		}
	};
	private OnClickListener mMediaGroupClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mListener != null && v.getTag() instanceof CardDetailMultiMediaGroup){
				mListener.onMediaGroupSelected((CardDetailMultiMediaGroup)v.getTag());
			}
		}
	};
	private View createStudioDescriptionView() {
		if(mData.generalInfo == null || mData.generalInfo.studioDescription == null || mData.generalInfo.studioDescription.length() == 0){
			return null;
		}
		View v = mInflator.inflate(R.layout.carddetailstudiodesc, null);
		TextView title = (TextView)v.findViewById(R.id.carddetaildesc_studiotitle);
		title.setText("Studio Description");
		mStudioDescription = title;
		title.setTypeface(FontUtil.Roboto_Light);
		TextView text = (TextView)v.findViewById(R.id.carddetaildesc_studiodescription);
		text.setTypeface(FontUtil.Roboto_Regular);
		text.setText(mData.generalInfo.studioDescription);
		return v;
	}

	private View createMyplexDescriptionView() {
		if(mData.generalInfo == null || mData.generalInfo.myplexDescription == null || mData.generalInfo.myplexDescription.length() == 0){
			return null;
		}
		View v = mInflator.inflate(R.layout.carddetailstudiodesc, null);
		TextView title = (TextView)v.findViewById(R.id.carddetaildesc_studiotitle);
		title.setTypeface(FontUtil.Roboto_Light);
		mMyPlexDescription = title;
		TextView text = (TextView)v.findViewById(R.id.carddetaildesc_studiodescription);
		text.setText(mData.generalInfo.myplexDescription);
		text.setTypeface(FontUtil.Roboto_Regular);
		return v;
	}
		
	private View createFullDescriptionView() {
		if(mData.generalInfo == null){mDetails = null; return null;}
		
		//mixPanelExpandedCastCrew();		
		Analytics.mixPanelExpandedCastCrew(mData);
		View v = mInflator.inflate(R.layout.carddetailfulldescription, null);
		mDetails  = v;
		TextView movieName = (TextView)v.findViewById(R.id.carddetaildesc_movename);
		if(mData.generalInfo.title != null){
			movieName.setText(mData.generalInfo.title.toLowerCase());	
		}
		movieName.setTypeface(FontUtil.Roboto_Light);
		TextView parentalRating = (TextView)v.findViewById(R.id.carddetaildesc_parentalRating);
		if(mData.content != null && mData.content.certifiedRatings != null && mData.content.certifiedRatings.values != null){
			for(CardDataCertifiedRatingsItem ratingItem:mData.content.certifiedRatings.values){
				parentalRating.setText(ratingItem.rating);
				break;
			}
		}
		parentalRating.setTypeface(FontUtil.Roboto_Medium);
		
		RatingBar ratingBar = (RatingBar)v.findViewById(R.id.carddetaildesc_setRating);
		if(mData.userReviews != null){
			ratingBar.setRating(mData.userReviews.averageRating);	
		}
		TextView relaseDate = (TextView)v.findViewById(R.id.carddetaildesc_releaseDate);
		if(mData.content != null && mData.content.releaseDate != null){
			relaseDate.setText(mData.content.releaseDate);
		}
		relaseDate.setTypeface(FontUtil.Roboto_Medium);
		
		if(mData._id.equalsIgnoreCase("0")){
			ImageView imgDuration = (ImageView) v.findViewById(R.id.carddetailbriefdescription_imgduration);
			imgDuration.setVisibility(View.GONE);
			parentalRating.setVisibility(View.GONE);
			relaseDate.setVisibility(View.GONE);
			ratingBar.setVisibility(View.GONE);
		}
		
		TextView contentDuration = (TextView)v.findViewById(R.id.carddetaildesc_duration);
		if(mData.content != null ){
			contentDuration.setText(""+mData.content.duration);
		}
		contentDuration.setTypeface(FontUtil.Roboto_Medium);
		
		TextView moviedescriptionTitle = (TextView)v.findViewById(R.id.carddetaildesc_descriptionTitle);
		moviedescriptionTitle.setTypeface(FontUtil.Roboto_Light);
		mDescription = moviedescriptionTitle;
		
		TextView moviedescription = (TextView)v.findViewById(R.id.carddetaildesc_description);
		if(mData.generalInfo.briefDescription != null){
			moviedescription.setText(mData.generalInfo.description);
		}
		moviedescription.setTypeface(FontUtil.Roboto_Regular);
		
		RelativeLayout expand = (RelativeLayout)v.findViewById(R.id.carddetailfulldescription_expandlayout);
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			//.setVisibility(View.VISIBLE);
		}
		expand.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mListener != null){
					mListener.onDescriptionCollapsed();
				}
			}
		});
		Util.showFeedback(expand);
		LinearLayout layout = (LinearLayout)v.findViewById(R.id.carddetaildesc_contentlayout);
		View myplexDescView = createMyplexDescriptionView(); 
		View studioDescView = createStudioDescriptionView();
		if(myplexDescView != null  && studioDescView != null){
			layout.addView(myplexDescView);	
		}
		else{
			if(myplexDescView != null){
				layout.addView(myplexDescView);	
			}
			if(studioDescView != null){
				layout.addView(studioDescView);	
			}
		}
		View credits = createCastCrewView();
		if(credits != null){
			layout.addView(createCastCrewView());
		}
		mFullDescPackageButton = (Button)v.findViewById(R.id.carddetaildesc_purchasebutton);
		UpdateSubscriptionStatus(mData);
//		createPlayInPlaceView(layout);
		addSpace(layout,(int)mContext.getResources().getDimension(R.dimen.margin_gap_12));
		return v;
	}
	private String mPriceStarts = "starts from ";
	private String mRupeeCode  = null;
	private View createBriefDescriptionView() {
		mCredits = null;
		mDetails = null;
		mDescription = null;
		mMyPlexDescription = null;
		mStudioDescription = null;
		if(mData.generalInfo == null){  return null;}
		
		View v = mInflator.inflate(R.layout.carddetailbreifdescription, null);
		mDetails = v;
		TextView movieName = (TextView)v.findViewById(R.id.carddetailbreifdescription_movename);
		if(mData.generalInfo.title != null){
			movieName.setText(mData.generalInfo.title.toLowerCase());	
		}
		movieName.setTypeface(FontUtil.Roboto_Light);
		TextView parentalRating = (TextView)v.findViewById(R.id.carddetailbriefdescription_parentalRating);
		if(mData.content != null && mData.content.certifiedRatings != null && mData.content.certifiedRatings.values != null){
			for(CardDataCertifiedRatingsItem ratingItem:mData.content.certifiedRatings.values){
				parentalRating.setText(ratingItem.rating);
				break;
			}
		}
		
		if(mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(ConsumerApi.CONTENT_SPORTS_LIVE)){
			v.findViewById(R.id.carddetailbriefdescription_setRating).setVisibility(View.GONE);
			v.findViewById(R.id.carddetailbriefdescriptionsubtitle).setVisibility(View.GONE);
			
		}
		
		parentalRating.setTypeface(FontUtil.Roboto_Medium);
		RatingBar ratingBar = (RatingBar)v.findViewById(R.id.carddetailbriefdescription_setRating);
		if(mData.userReviews != null){
			ratingBar.setRating(mData.userReviews.averageRating);	
		}
		
		TextView relaseDate = (TextView)v.findViewById(R.id.carddetailbriefdescription_releaseDate);
		if(mData.content != null && mData.content.releaseDate != null){
			relaseDate.setText(mData.content.releaseDate);
		}
		relaseDate.setTypeface(FontUtil.Roboto_Medium);
		
		
		
		if(mData._id.equalsIgnoreCase("0")){
			ImageView imgDuration = (ImageView) v.findViewById(R.id.carddetailbriefdescription_imgduration);
			imgDuration.setVisibility(View.GONE);
			parentalRating.setVisibility(View.GONE);
			relaseDate.setVisibility(View.GONE);
			ratingBar.setVisibility(View.GONE);
		}
		
		TextView contentDuration = (TextView)v.findViewById(R.id.carddetailbriefdescription_duration);
		if(mData.content != null ){
			contentDuration.setText(""+mData.content.duration);
		}
		contentDuration.setTypeface(FontUtil.Roboto_Medium);
		
		TextView moviedescription = (TextView)v.findViewById(R.id.carddetailbriefdescription_description);
		if(mData.generalInfo.briefDescription != null){
			moviedescription.setText(mData.generalInfo.briefDescription);
		}
		moviedescription.setTypeface(FontUtil.Roboto_Regular);
		mBriefDescPackageButton = (Button)v.findViewById(R.id.carddetailbriefdescription_purchasebutton);
		UpdateSubscriptionStatus(mData);
		
		
		
		RelativeLayout expand = (RelativeLayout)v.findViewById(R.id.carddetailbriefdescription_expandlayout);
		expand.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mListener != null){
					mListener.onDescriptionExpanded();
				}				
			}
		});
		Util.showFeedback(expand);
		return v;
	}
	public void UpdateSubscriptionStatus(CardData data){
		UpdatePackageButton(mBriefDescPackageButton,data);
		UpdatePackageButton(mFullDescPackageButton,data);
	}
	private void UpdatePackageButton(Button packageButton,CardData data){
		final CardData mData = data;
		if(packageButton == null){return;}
		packageButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				final View view = v;
					view.setEnabled(false);
					view.postDelayed(new Runnable() {					
						@Override
						public void run() {
							view.setEnabled(true);
						}
					}, 2000);
					PackagePopUp popup = new PackagePopUp(mContext,mParentView);
					myplexapplication.getCardExplorerData().cardDataToSubscribe =  mData;
					popup.showPackDialog(mData, ((Activity)mContext).getActionBar().getCustomView());	
							
			
			}
		});
		Util.showFeedbackOnSame(packageButton);
		packageButton.setTypeface(FontUtil.Roboto_Medium);
		float price = 10000.99f;
		if(mData.packages == null || mData.packages.size() == 0){
			packageButton.setText(mContext.getString(R.string.cardstatusfree));
			packageButton.setOnClickListener(null);
		}else{
			if(mData.currentUserData != null && mData.currentUserData.purchase != null && mData.currentUserData.purchase.size() != 0){
				packageButton.setText(mContext.getString(R.string.cardstatuspaid));
				packageButton.setOnClickListener(null);
			}else{
				for(CardDataPackages packageitem:mData.packages){
					if(packageitem.priceDetails != null){
						for(CardDataPackagePriceDetailsItem priceDetailItem:packageitem.priceDetails){
							if(!priceDetailItem.paymentChannel.equalsIgnoreCase(ConsumerApi.PAYMENT_CHANNEL_INAPP) && priceDetailItem.price < price){
								price = priceDetailItem.price;
							}
						}
						if(mRupeeCode == null){
							mRupeeCode = mContext.getResources().getString(R.string.price_rupeecode); 
						}
						packageButton.setText(mPriceStarts + mRupeeCode + " "+price);
						if(price == 0)
						{
							packageButton.setText(mContext.getString(R.string.cardstatustempfree));
							packageButton.setOnClickListener(null);
						}else if(price  == 10000.99f){
							packageButton.setText(mContext.getString(R.string.cardstatusfree));
							packageButton.setOnClickListener(null);
						}
						else
							packageButton.setText(mPriceStarts + mRupeeCode + " "+price);

					}else{
						packageButton.setText(mContext.getString(R.string.cardstatusfree));
						packageButton.setOnClickListener(null);
					}
					
				}	
			}
		}
		if(mData._id.equalsIgnoreCase("0")){
			packageButton.setVisibility(View.GONE);
		}
	}
	private Button mBriefDescPackageButton = null;
	private Button mFullDescPackageButton = null;
	private View mParentView = null;
	public void setParent(View parent){
		this.mParentView =  parent;
	}
	private OnClickListener packageButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final View view = v;
				view.setEnabled(false);
				view.postDelayed(new Runnable() {					
					@Override
					public void run() {
						view.setEnabled(true);
					}
				}, 2000);
				PackagePopUp popup = new PackagePopUp(mContext,mParentView);
				myplexapplication.getCardExplorerData().cardDataToSubscribe =  mData;
				popup.showPackDialog(mData, ((Activity)mContext).getActionBar().getCustomView());	
						
		}
	};
	public View getSubSection(String label){
		if(label.equalsIgnoreCase(SECTION_DETAILS)){
			return mDetails;
		}else if(label.equalsIgnoreCase(SECTION_DESCRIPTION)){
			return mDescription;
		}else if(label.equalsIgnoreCase(SECTION_MYPLEXDESCRITION)){
			return mMyPlexDescription;
		}else if(label.equalsIgnoreCase(SECTION_STUDIODESCRITION)){
			return mStudioDescription;
		}else if(label.equalsIgnoreCase(SECTION_CREDITS)){
			return mCredits;
		}else if(label.equalsIgnoreCase(SECTION_RELATEDMULTIMEDIA)){
			return mMultimedia;
		}else if(label.equalsIgnoreCase(SECTION_COMMENTS)){
			return mComments;
		}
		return null;
	}
	public int getSectionItemHeight(View v){
		if(v != null){
			return (int) v.getHeight();
		}
		return 0;
	}
	public int getSectionHeight(String label) {
		int returnValue =  0;
		if(label.equalsIgnoreCase(SECTION_DETAILS)){
			returnValue = getSectionItemHeight(mDetails);
		}else if(label.equalsIgnoreCase(SECTION_DESCRIPTION)){
			returnValue = getSectionItemHeight(mDetails);
			returnValue += getSectionItemHeight(mDescription);
		}else if(label.equalsIgnoreCase(SECTION_MYPLEXDESCRITION)){
			returnValue = getSectionItemHeight(mDetails);
			returnValue += getSectionItemHeight(mDescription);
			returnValue += getSectionItemHeight(mMyPlexDescription);
		}else if(label.equalsIgnoreCase(SECTION_STUDIODESCRITION)){
			returnValue = getSectionItemHeight(mDetails);
			returnValue += getSectionItemHeight(mDescription);
			returnValue += getSectionItemHeight(mMyPlexDescription);
			returnValue += getSectionItemHeight(mStudioDescription);
		}else if(label.equalsIgnoreCase(SECTION_CREDITS)){
			returnValue = getSectionItemHeight(mDetails);
			returnValue += getSectionItemHeight(mDescription);
			returnValue += getSectionItemHeight(mMyPlexDescription);
			returnValue += getSectionItemHeight(mStudioDescription);
			returnValue += getSectionItemHeight(mCredits);
		}else if(label.equalsIgnoreCase(SECTION_RELATEDMULTIMEDIA)){
			returnValue = getSectionItemHeight(mDetails);
			returnValue += getSectionItemHeight(mDescription);
			returnValue += getSectionItemHeight(mMyPlexDescription);
			returnValue += getSectionItemHeight(mStudioDescription);
			returnValue += getSectionItemHeight(mCredits);
			returnValue += getSectionItemHeight(mMultimedia);
		}else if(label.equalsIgnoreCase(SECTION_COMMENTS)){
			returnValue = getSectionItemHeight(mDetails);
			returnValue += getSectionItemHeight(mDescription);
			returnValue += getSectionItemHeight(mMyPlexDescription);
			returnValue += getSectionItemHeight(mStudioDescription);
			returnValue += getSectionItemHeight(mCredits);
			returnValue += getSectionItemHeight(mMultimedia);
			returnValue += getSectionItemHeight(mComments);
		}	
		return returnValue;
	}
	
	private View createWebView() {
		
		if(mData.generalInfo == null ){
			return null;
		}
		
		View v = mInflator.inflate(R.layout.carddetails_webview, null);
		
		WebView webView = (WebView) v.findViewById(R.id.webview);
		
		String url = "http://touch.fifa.com/u17womensworldcup/matches/index.html";
		
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);	
		WebSettings webSettings = webView.getSettings();
//		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setLoadsImagesAutomatically(true);
		webView.loadUrl(url);
		
		return v;
	}
	
}
