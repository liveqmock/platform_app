package com.apalya.myplex.views;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Space;
import android.widget.TextView;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardDetailBaseData;
import com.apalya.myplex.data.CardDetailMultiMediaGroup;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;

public class CardDetailViewFactory {
	
	public interface CardDetailViewFactoryListener{
		public void onExpanded();
		public void onTextSelected(String key);
		public void onMediaGroupSelected(CardDetailMultiMediaGroup group);
	}
	public void setOnCardDetailExpandListener(CardDetailViewFactoryListener listener){
		this.mCardExpandListener = listener;
	}
	private OnClickListener mOnDescriptionExpand = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mData == null){return;}
			if(!mData.isExpanded){
				if(mCardExpandListener != null){
					mCardExpandListener.onExpanded();
				}
			}
		}
	}; 
	private OnClickListener mOnMultiMediaExpand = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mData == null){return;}
			if(!mData.isExpanded){
				if(mCardExpandListener != null){
					mCardExpandListener.onExpanded();
				}
			}
		}
	}; 
	private OnClickListener mOnCommentsExpand = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mData == null){return;}
			if(!mData.isExpanded){
				if(mCardExpandListener != null){
					mCardExpandListener.onExpanded();
				}
			}
		}
	}; 
	private CardDetailViewFactoryListener mCardExpandListener;
	private Context mContext;
	private LayoutInflater mInflator;
	private CardDetailBaseData mData;
	private View mDetails;
	private View mDescription;
	private View mCredits;
	private View mExtras;
	private View mMultimedia;
	private View mComments;

	public void SetData(CardDetailBaseData data){
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

	public View CreateView(CardDetailBaseData data, int type) {
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
			return createCommentsView();
		case CARDDETAIL_BRIEF_COMMENTS:
			return createBriefCommentsView();
		default:
			break;
		}
		return null;
	}

	private View createBriefCommentsView() {
		View v = mInflator.inflate(R.layout.carddetailcomment, null);
		mComments = v;
		LinearLayout layout = (LinearLayout)v.findViewById(R.id.carddetailcomment_contentlayout);
		addSpace(layout, (int)mContext.getResources().getDimension(R.dimen.margin_gap_16));
		for(int i = 0; i <mData.mCommentsList.size();i++ ){
			View child = mInflator.inflate(R.layout.carddetailcomment_data, null);
//			VerticalLineRelativeLayout timelinelayout = (VerticalLineRelativeLayout)child.findViewById(R.id.timeLineLayout);
//			timelinelayout.setWillNotDrawEnabled(false);
			TextView personName = (TextView)child.findViewById(R.id.carddetailcomment_personname);
			personName.setText(mData.mCommentsList.get(i).mName);
			personName.setTypeface(FontUtil.Roboto_Regular);
			TextView commentTime = (TextView)child.findViewById(R.id.carddetailcomment_time);
			commentTime.setText(mData.mCommentsList.get(i).mDate);
			commentTime.setTypeface(FontUtil.Roboto_Regular);
			TextView commentMessage  = (TextView)child.findViewById(R.id.carddetailcomment_comment);
			commentMessage.setText(mData.mCommentsList.get(i).mMessage);
			commentMessage.setTypeface(FontUtil.Roboto_Regular);
//			addSpace(layout, 16);
			layout.addView(child);
		}
		return v;
	}

	private View createCommentsView() {
		return createBriefCommentsView();
	}

	private View createExtraMultiMediaView() {
		// TODO Auto-generated method stub
		return null;
	}

	private View createPlayableMultiMediaView() {
		// TODO Auto-generated method stub
		return null;
	}

	private View createBriefRelatedView() {
		View v = mInflator.inflate(R.layout.carddetailmedia, null);
		TextView text = (TextView)v.findViewById(R.id.carddetailmedia_title);
		text.setTypeface(FontUtil.Roboto_Light);
		mMultimedia = text;		
		ImageView expand = (ImageView)v.findViewById(R.id.carddetailmedia_expand);
		expand.setOnClickListener(mOnMultiMediaExpand);
		Util.showFeedback(expand);
		LinearLayout contentLayout = (LinearLayout)v.findViewById(R.id.carddetailmedia_contentlayout);
		for(CardDetailMultiMediaGroup group:mData.mMultiMediaGroup){
			View child = mInflator.inflate(R.layout.carddetails_fullmultimediaitem, null);
			TextView groupname = (TextView)child.findViewById(R.id.carddetailmultimedia_groupname);
			groupname.setText(group.groupName);
			groupname.setTypeface(FontUtil.Roboto_Regular);
			TextView secondaryname = (TextView)child.findViewById(R.id.carddetailmultimedia_secondaryname);
			
			FadeInNetworkImageView imageView1 = (FadeInNetworkImageView)child.findViewById(R.id.carddetailmultimedia_stackview1);
			imageView1.setImageUrl(group.mList.get(0).mThumbnailUrl, MyVolley.getImageLoader());
			
			FadeInNetworkImageView imageView2 = (FadeInNetworkImageView)child.findViewById(R.id.carddetailmultimedia_stackview2);
			imageView2.setImageUrl(group.mList.get(1).mThumbnailUrl, MyVolley.getImageLoader());
			
			imageView1.setTag(group);
			imageView1.setOnClickListener(mMediaGroupClickListener);
			contentLayout.addView(child);
		}
		return v;
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
		layout.addView(videoWidget.CreateView(mData.mPlayinPlaceList.get(0)));
		
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
		View v = (LinearLayout)mInflator.inflate(R.layout.carddetailcastcrew, null);
		TextView title = (TextView)v.findViewById(R.id.carddetailcastandcrew_credits);
		title.setTypeface(FontUtil.Roboto_Light);
		mCredits = title;
		LinearLayout leftLayout = (LinearLayout)v.findViewById(R.id.carddetailcastcrew_leftlayout);
		LinearLayout rightLayout = (LinearLayout)v.findViewById(R.id.carddetailcastcrew_rightlayout);
		for(int i = 0; i < mData.mCastCrewList.size();i++){
			
			TextView leftText = new TextView(mContext);
			LinearLayout.LayoutParams leftparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			leftparams.gravity = Gravity.RIGHT;
			leftText.setLayoutParams(leftparams);
			leftText.setTextSize(12);
			leftText.setTextColor(Color.parseColor("#4b4b4c"));
			
			TextView rightText = new TextView(mContext);
			LinearLayout.LayoutParams rightparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			rightText.setLayoutParams(rightparams);
			rightText.setTextSize(12);
			rightText.setTextColor(Color.parseColor("#4b4b4c"));
			
			leftText.setText(mData.mCastCrewList.get(i).leftText);
			leftText.setTypeface(FontUtil.Roboto_Regular);
			Util.showFeedback(leftText);
			leftText.setTag( mData.mCastCrewList.get(i).leftText);
			leftText.setOnClickListener(mCastandCrewClickListener);
//			leftText.setPaintFlags(leftText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			rightText.setText(mData.mCastCrewList.get(i).rightText);
			Util.showFeedback(rightText);
			rightText.setTag(mData.mCastCrewList.get(i).rightText);
			rightText.setOnClickListener(mCastandCrewClickListener);
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
			if(mCardExpandListener != null && v.getTag() instanceof String){
				mCardExpandListener.onTextSelected((String)v.getTag());
			}
		}
	};
	private OnClickListener mMediaGroupClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(mCardExpandListener != null && v.getTag() instanceof CardDetailMultiMediaGroup){
				mCardExpandListener.onMediaGroupSelected((CardDetailMultiMediaGroup)v.getTag());
			}
		}
	};
	private View createStudioDescriptionView() {
		View v = mInflator.inflate(R.layout.carddetailstudiodesc, null);
		TextView title = (TextView)v.findViewById(R.id.carddetaildesc_studiotitle);
		title.setText("Studio Description");
		title.setTypeface(FontUtil.Roboto_Medium);
		TextView text = (TextView)v.findViewById(R.id.carddetaildesc_studiodescription);
		text.setTypeface(FontUtil.Roboto_Medium);
		text.setText(mData.myplexDescription);
		return v;
	}

	private View createMyplexDescriptionView() {
		View v = mInflator.inflate(R.layout.carddetailstudiodesc, null);
		TextView title = (TextView)v.findViewById(R.id.carddetaildesc_studiotitle);
		title.setText("Myplex Description");
		title.setTypeface(FontUtil.Roboto_Medium);
		TextView text = (TextView)v.findViewById(R.id.carddetaildesc_studiodescription);
		text.setText(mData.myplexDescription);
		text.setTypeface(FontUtil.Roboto_Medium);
		return v;
	}

	private View createFullDescriptionView() {
		
		View v = mInflator.inflate(R.layout.carddetailfulldescription, null);
		TextView movieName = (TextView)v.findViewById(R.id.carddetaildesc_movename);
		movieName.setText(mData.contentName);
		movieName.setTypeface(FontUtil.Roboto_Regular);
		TextView parentalRating = (TextView)v.findViewById(R.id.carddetaildesc_parentalRating);
		parentalRating.setText(mData.parentalRating);
		parentalRating.setTypeface(FontUtil.Roboto_Regular);
		RatingBar ratingBar = (RatingBar)v.findViewById(R.id.carddetaildesc_setRating);
		ratingBar.setRating(mData.rating);
		TextView relaseDate = (TextView)v.findViewById(R.id.carddetaildesc_releaseDate);
		relaseDate.setText(mData.releaseDate);
		relaseDate.setTypeface(FontUtil.Roboto_Regular);
		TextView moviedescription = (TextView)v.findViewById(R.id.carddetaildesc_description);
		moviedescription.setText(mData.fullDescription);
		moviedescription.setTypeface(FontUtil.RobotoCondensed_Light);
		
		ImageView expand = (ImageView)v.findViewById(R.id.carddetailfulldescription_expand);
		expand.setOnClickListener(mOnDescriptionExpand);
		Util.showFeedback(expand);
		
		LinearLayout layout = (LinearLayout)v.findViewById(R.id.carddetaildesc_contentlayout);
		layout.addView(createMyplexDescriptionView());
		layout.addView(createStudioDescriptionView());
		layout.addView(createCastCrewView());
		createPlayInPlaceView(layout);
		addSpace(layout,(int)mContext.getResources().getDimension(R.dimen.margin_gap_8));
		return v;
	}
	private View createBriefDescriptionView() {
		
		View v = mInflator.inflate(R.layout.carddetailfulldescription, null);
		mDetails = v;
		TextView movieName = (TextView)v.findViewById(R.id.carddetaildesc_movename);
		movieName.setText(mData.contentName);
		movieName.setTypeface(FontUtil.Roboto_Light);
		TextView parentalRating = (TextView)v.findViewById(R.id.carddetaildesc_parentalRating);
		parentalRating.setText(mData.parentalRating);
		parentalRating.setTypeface(FontUtil.Roboto_Medium);
		RatingBar ratingBar = (RatingBar)v.findViewById(R.id.carddetaildesc_setRating);
		ratingBar.setRating(mData.rating);
		TextView relaseDate = (TextView)v.findViewById(R.id.carddetaildesc_releaseDate);
		relaseDate.setText(mData.releaseDate);
		relaseDate.setTypeface(FontUtil.Roboto_Medium);
		
		
		TextView moviedescriptionTitle = (TextView)v.findViewById(R.id.carddetaildesc_descriptionTitle);
		moviedescriptionTitle.setTypeface(FontUtil.Roboto_Light);
		mDescription = moviedescriptionTitle;
		
		
		TextView moviedescription = (TextView)v.findViewById(R.id.carddetaildesc_description);
		moviedescription.setText(mData.fullDescription);
		moviedescription.setTypeface(FontUtil.Roboto_Regular);
		
		
		ImageView expand = (ImageView)v.findViewById(R.id.carddetailfulldescription_expand);
		expand.setOnClickListener(mOnDescriptionExpand);
		Util.showFeedback(expand);
		
		
		
		
		LinearLayout layout = (LinearLayout)v.findViewById(R.id.carddetaildesc_contentlayout);
//		layout.addView(createMyplexDescriptionView());
//		layout.addView(createStudioDescriptionView());
		layout.addView(createCastCrewView());
		createPlayInPlaceView(layout);
		addSpace(layout,(int)mContext.getResources().getDimension(R.dimen.margin_gap_8));
		return v;
	}
	public int getYPosition(String label) {
		if(label.equalsIgnoreCase("Details")){
			return (int)mDetails.getY();
		}
		else if(label.equalsIgnoreCase("Description")){
			return (int)mDescription.getY();
		}else if(label.equalsIgnoreCase("Credits")){
			return (int)mCredits.getY();
		}else if(label.equalsIgnoreCase("Extra")){
			return (int)mExtras.getY();
		}else if(label.equalsIgnoreCase("RelatedMultimedia")){
			return (int)mMultimedia.getY();
		}else if(label.equalsIgnoreCase("Comments")){
			return (int)mComments.getY();
		}
		return 0;
	}
}
