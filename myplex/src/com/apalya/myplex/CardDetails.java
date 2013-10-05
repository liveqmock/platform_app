package com.apalya.myplex;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;

import com.apalya.myplex.adapters.ScrollingDirection;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDetailBaseData;
import com.apalya.myplex.data.CardDetailCastCrew;
import com.apalya.myplex.data.CardDetailCommentData;
import com.apalya.myplex.data.CardDetailMediaData;
import com.apalya.myplex.data.CardDetailMediaListData;
import com.apalya.myplex.data.CardDetailMultiMediaGroup;
import com.apalya.myplex.data.FilterMenudata;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.media.VideoView;
import com.apalya.myplex.media.VideoViewPlayer;
import com.apalya.myplex.media.VideoViewPlayer.StreamType;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.views.CardDetailViewFactory;
import com.apalya.myplex.views.CardDetailViewFactory.CardDetailViewFactoryListener;
import com.apalya.myplex.views.CustomDialog;
import com.apalya.myplex.views.CustomScrollView;
import com.apalya.myplex.views.FadeInNetworkImageView;
import com.apalya.myplex.views.ItemExpandListener.ItemExpandListenerCallBackListener;
import com.apalya.myplex.views.JazzyViewPager;
import com.apalya.myplex.views.JazzyViewPager.TransitionEffect;
import com.apalya.myplex.views.OutlineContainer;
import com.apalya.myplex.views.docketVideoWidget;
import com.google.android.gms.internal.c;

public class CardDetails extends BaseFragment implements
		ItemExpandListenerCallBackListener,CardDetailViewFactoryListener,PlayerListener,ScrollingDirection {
	private LayoutInflater mInflater;
	private LinearLayout mParentContentLayout;
	private CardDetailViewFactory mCardDetailViewFactory;
	
	private ImageView mDescriptionExpansion;
	private ImageView mRelatedMediaExpansion;
	private ImageView mCommentsExpansion;
	
	private LinearLayout mDescriptionContentLayout;
	private LinearLayout mMediaContentLayout;
	private LinearLayout mCommentsContentLayout;

	private boolean mDescriptionExpansionToogle = false;
	private boolean mRelatedExpansionToogle = false;
	private boolean mCommentsExpansionToogle = false;
	
	private CustomScrollView mScrollView;
	private RelativeLayout mBottomActionBar;
	
	private int mDetailType = Profile;
	public static final int Profile = 0;
	public static final int MovieDetail = 1;
	public static final int TvShowsDetail = 2;
	public static final int LiveTvDetail = 3;
	public View rootView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mMainActivity.setPotrait();
		mInflater = LayoutInflater.from(getContext());
		rootView = inflater.inflate(R.layout.carddetails, container, false);
		mScrollView = (CustomScrollView)rootView.findViewById(R.id.carddetail_scroll_view);
		mBottomActionBar = (RelativeLayout)rootView.findViewById(R.id.carddetail_bottomactionbar);
		mScrollView.setDirectionListener(this);
		RelativeLayout videoLayout = (RelativeLayout)rootView.findViewById(R.id.carddetail_videolayout);
		videoLayout.addView(createVideoPreview());
		mParentContentLayout = (LinearLayout) rootView.findViewById(R.id.carddetail_detaillayout);
		mCardDetailViewFactory = new CardDetailViewFactory(getContext());
		mCardDetailViewFactory.setOnCardDetailExpandListener(this);
		prepareContent();
		return rootView;
	}

	private CardData mCardData;
	private FadeInNetworkImageView mPreviewImage;
	private VideoView mVideoView;
	private boolean mPlaying = false;
	private ImageView mPlay;
	private RelativeLayout mProgressBar;
	VideoViewPlayer mVideoViewPlayer;
	private int mPerBuffer = 0;
	private View createVideoPreview(){
		View v = mInflater.inflate(R.layout.cardmediasubitemvideo, null);
		int width , height = 100;
		
		width = myplexapplication.getApplicationConfig().screenWidth;
		height = (width * 9)/16; 
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
		mPreviewImage = (FadeInNetworkImageView) v.findViewById(R.id.cardmediasubitemvideo_imagepreview);
		mPreviewImage.setLayoutParams(params);
		mVideoView = (VideoView)v.findViewById(R.id.cardmediasubitemvideo_videopreview);
		mVideoView.setLayoutParams(params);
		mPlay = (ImageView) v.findViewById(R.id.cardmediasubitemvideo_play);
		mProgressBar = (RelativeLayout) v.findViewById(R.id.cardmediasubitemvideo_progressbarLayout);
		
		mPreviewImage.setImageUrl("https://lh5.googleusercontent.com/-d-qS8knzDP4/SePRfjfPYhI/AAAAAAAACVA/jxox5vRCphw/IMG_0084.jpg", MyVolley.getImageLoader());
		Util.showFeedback(mPlay);

		mPlay.setOnClickListener(mPlayListener);
		return v;
	}
	private OnClickListener mPlayListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mPlaying){
				mVideoViewPlayer.closeSession();
				mProgressBar.setVisibility(View.GONE);
				showImagePreview();
				mPlaying = false;
				return;
			}
			mPlaying = true;
			hideImagePreview();
			mProgressBar.setVisibility(View.VISIBLE);
			Uri uri = Uri.parse("rtsp://59.162.166.216:554/AAJTAK_QVGA.sdp");
			uri = Uri.parse("rtsp://46.249.213.87:554/playlists/bollywood-action_qcif.hpl.3gp");
			VideoViewPlayer.StreamType streamType = StreamType.VOD;
			if(mVideoViewPlayer == null){
				mVideoViewPlayer = new VideoViewPlayer(mVideoView,mContext, uri, streamType);
				mVideoViewPlayer.openVideo();
			}else{
				mVideoViewPlayer.setUri(uri, streamType);
			}
			mVideoViewPlayer.setPlayerListener(CardDetails.this);	
		}
	};
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int perBuffer) {
		Log.e("player",perBuffer +" loading ");
		if(this.mPerBuffer <= perBuffer){
			this.mPerBuffer = perBuffer;
		}
		int currentseekposition = mVideoView.getCurrentPosition();
		if(currentseekposition < 0){
			currentseekposition = 510;
		}
		if(mVideoView.isPlaying() && currentseekposition > 500){
			mProgressBar.setVisibility(View.GONE);
			mVideoViewPlayer.deregisteronBufferingUpdate();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int arg1, int arg2) {
		mProgressBar.setVisibility(View.GONE);
		showImagePreview();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mProgressBar.setVisibility(View.GONE);
		showImagePreview();
	}

	@Override
	public void onPlayerQualityClick() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}
	private void showImagePreview(){
		mPreviewImage.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
		mPlay.setImageResource(R.drawable.player_icon_play);
		
	}
	private void hideImagePreview(){
		mPreviewImage.setVisibility(View.INVISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPlay.setImageResource(R.drawable.player_icon_pause);
	}
	private void prepareContent() {
		dummyData();
		fillData();
		prepareFilterData();
	}

	private void prepareFilterData() {
		List<FilterMenudata> filteroptions = new ArrayList<FilterMenudata>();
		filteroptions.add(new FilterMenudata(FilterMenudata.SECTION,"Details", 1));
		filteroptions.add(new FilterMenudata(FilterMenudata.ITEM,"Description", 2));
		filteroptions.add(new FilterMenudata(FilterMenudata.ITEM,"Credits", 3));
		filteroptions.add(new FilterMenudata(FilterMenudata.ITEM,"Extra", 4));
		filteroptions.add(new FilterMenudata(FilterMenudata.SECTION,"Related Multimedia", 5));
		filteroptions.add(new FilterMenudata(FilterMenudata.SECTION,"Comments", 6));
		mMainActivity.addFilterData(filteroptions, mFilterMenuClickListener);
	}
	private OnClickListener mFilterMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof FilterMenudata) {
				FilterMenudata menuData = (FilterMenudata)v.getTag();
				int moveTo = mCardDetailViewFactory.getYPosition(menuData.label);
				int currentY = mScrollView.getScrollY();
				int moveTo1 = -currentY + moveTo;
//				int diff = Math.abs(moveTo - currentY);
//				if(currentY < moveTo){
//					moveTo = currentY + diff;
//				}else{
//					moveTo = currentY - diff;
//				}
				Log.d("CardDetail"," value for "+menuData.label+" = "+mCardDetailViewFactory.getYPosition(menuData.label)+" scrollY = "+mScrollView.getScrollY());
				mScrollView.smoothScrollBy(0, moveTo1);
			}
		}
	};
	private OnClickListener mDescriptionExpansionClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mDescriptionContentLayout == null){return;}
			if(mDescriptionExpansionToogle){
				mDescriptionContentLayout.removeAllViews();
			}else{
				mDescriptionContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_FULL_DESCRIPTION));
				if(data.myplexDescription != null && data.myplexDescription.length() > 0){
					mDescriptionContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_MYPLEX_DESCRIPTION));
				}
				if(data.studioDescription != null && data.studioDescription.length() > 0){
					mDescriptionContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_STUDI0_DESCRIPTION));
				}
				if(data.mCastCrewList != null && data.mCastCrewList.size() > 0){
					mDescriptionContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_CASTANDCREW));
				}
				if(data.mPlayinPlaceList != null && data.mPlayinPlaceList.size() > 0){
					mDescriptionContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_PLAYINPLACE));
				}
			}
			mDescriptionExpansionToogle = !mDescriptionExpansionToogle;
		}
	};
	private OnClickListener mRelatedExpansionClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mMediaContentLayout == null){return;}
			if(mRelatedExpansionToogle){
				mMediaContentLayout.removeAllViews();
			}else{
				mMediaContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_EXTRA_RELATED_MULTIMEDIA ));
			}
			mRelatedExpansionToogle = !mRelatedExpansionToogle;
		}
	};
	private OnClickListener mCommentsExpansionClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mCommentsContentLayout == null){return;}
			if(mCommentsExpansionToogle){
				mCommentsContentLayout.removeAllViews();
			}else{
				mCommentsContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_COMMENTS ));
			}
			mCommentsExpansionToogle = !mCommentsExpansionToogle;
		}
	};
	private void addSpace(){
		Space gap = new Space(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)getContext().getResources().getDimension(R.dimen.margin_gap_8));
		gap.setLayoutParams(params);
		mParentContentLayout.addView(gap);
	}
	private void fillData() {
		mParentContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_BRIEF_DESCRIPTION));
		addSpace();
		mParentContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_BREIF_RELATED_MULTIMEDIA));
		addSpace();
		mParentContentLayout.addView(mCardDetailViewFactory.CreateView(data,CardDetailViewFactory.CARDDETAIL_BRIEF_COMMENTS));
		
	}

	private int count = 0;
	private CardDetailBaseData data;

	private void dummyData() {
		data = new CardDetailBaseData();
		data.contentID = "21321";
		data.contentName = "The Godfather";
		data.briefDescription = "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son. ";
		data.fullDescription = "The story begins as Don Vito Corleone, the head of a New York Mafia , oversees his daughter's wedding with his wife Wendy. His beloved son Michael has just come home from the war, but does not intend to become part of his father's business. Through Michael's life the nature of the family business becomes clear. The business of the family is just like the head of the family, kind and benevolent to those who give respect, but given to ruthless violence whenever anything stands against the good of the family. Don Vito lives his life in the way of the old country, but times are changing and some don't want to follow the old ways and look out for community and .";// An up and coming rival of the Corleone family wants to start selling drugs in New York, and needs the Don's influence to further his plan. The clash of the Don's fading old world values and the new ways will demand a terrible price, especially from Michael, all for the sake of the family";
		data.myplexDescription = "The story begins as Don Vito Corleone, the head of a New York Mafia , oversees his daughter's wedding with his wife Wendy. His beloved son Michael has just come home from the war, but does not intend to become part of his father's business. Through Michael's life the nature of the family business becomes clear. The business of the family is just like the head of the family, kind and benevolent to those who give respect, but given to ruthless violence whenever anything stands against the good of the family. Don Vito lives his life in the way of the old country, but times are changing and some don't want to follow the old ways and look out for community and";// . An up and coming rival of the Corleone family wants to start selling drugs in New York, and needs the Don's influence to further his plan. The clash of the Don's fading old world values and the new ways will demand a terrible price, especially from Michael, all for the sake of the family";
		data.studioDescription = "The story begins as Don Vito Corleone, the head of a New York Mafia , oversees his daughter's wedding with his wife Wendy. His beloved son Michael has just come home from the war, but does not intend to become part of his father's business. Through Michael's life the nature of the family business becomes clear. The business of the family is just like the head of the family, kind and benevolent to those who give respect, but given to ruthless violence whenever anything stands against the good of the family. Don Vito lives his life in the way of the old country, but times are changing and some don't want to follow the old ways and look out for community and";// . An up and coming rival of the Corleone family wants to start selling drugs in New York, and needs the Don's influence to further his plan. The clash of the Don's fading old world values and the new ways will demand a terrible price, especially from Michael, all for the sake of the family";
		data.parentalRating = "R";
		data.rating = 4.6f;
		data.releaseDate = " 24 March 1972";
		fillMediaGroup(data.mMultiMediaGroup);
		fillPlayinPlace(data.mPlayinPlaceList);
		fillReviewsList(data.mReviewsList);
		fillCommentsList(data.mCommentsList);
		fillCastCrew(data.mCastCrewList);
	}

	private void fillMediaGroup(List<CardDetailMultiMediaGroup> list) {
		{
			CardDetailMultiMediaGroup group = new CardDetailMultiMediaGroup();
			group.groupDescription = "some message";
			group.groupName = "group name";
			fillPlayinPlace(group.mList);
			list.add(group);
		}
		{
			CardDetailMultiMediaGroup group = new CardDetailMultiMediaGroup();
			group.groupDescription = "some message1";
			group.groupName = "group name1";
			fillPlayinPlace(group.mList);
			list.add(group);
		}
		{
			CardDetailMultiMediaGroup group = new CardDetailMultiMediaGroup();
			group.groupDescription = "some message2";
			group.groupName = "group name2";
			fillPlayinPlace(group.mList);
			list.add(group);
		}
		{
			CardDetailMultiMediaGroup group = new CardDetailMultiMediaGroup();
			group.groupDescription = "some message3";
			group.groupName = "group name3";
			fillPlayinPlace(group.mList);
			list.add(group);
		}
	}

	private void fillPlayinPlace(List<CardDetailMediaData> list) {
		{
			CardDetailMediaData data = new CardDetailMediaData();
			data.mThumbnailDescription = "Description ";
			data.mThumbnailMime = "Image/JPEG";
			data.mThumbnailUrl = "https://lh6.googleusercontent.com/-HEeoO3k3bPg/S0VKWAJUlbI/AAAAAAAAAik/k1x42L8UIvw/Movie-GhostRider-001.jpg";
			list.add(data);
		}
		{
			CardDetailMediaData data = new CardDetailMediaData();
			data.mThumbnailDescription = "Description ";
			data.mThumbnailMime = "Image/JPEG";
			data.mThumbnailUrl = "https://lh4.googleusercontent.com/-16Op5dZqK4s/STQf00CgLaI/AAAAAAAAAS4/y94XF3tvI2o/Blog1000-Which-way-india-stn.jpg";
			list.add(data);
		}
		{
			CardDetailMediaData data = new CardDetailMediaData();
			data.mThumbnailDescription = "Description ";
			data.mThumbnailUrl = "https://lh3.googleusercontent.com/-yqLKT4RAfBM/S32v0NNVTbI/AAAAAAAAKyw/2ggyry4KiCE/Nature%252520Wallpapers%252520%25252880%252529.jpg";
			list.add(data);
		}
		{
			CardDetailMediaData data = new CardDetailMediaData();
			data.mThumbnailDescription = "Description ";
			data.mThumbnailMime = "Image/JPEG";
			data.mThumbnailUrl = "https://lh5.googleusercontent.com/-d-qS8knzDP4/SePRfjfPYhI/AAAAAAAACVA/jxox5vRCphw/IMG_0084.jpg";
			list.add(data);
		}
		{
			CardDetailMediaData data = new CardDetailMediaData();
			data.mThumbnailDescription = "Description ";
			data.mThumbnailUrl = "https://lh5.googleusercontent.com/-eurfd_3DDJM/SYpR7j0o8CI/AAAAAAAAJ8k/XRRlN8bdQlA/DSCF3739r.jpg";
			list.add(data);
		}
	}

	private void fillReviewsList(List<CardDetailCommentData> list) {
		for (int i = 0; i < 10; i++) {
			CardDetailCommentData data = new CardDetailCommentData();
			data.mMessage = "reviews here " + i;
			data.mDate = "on " + i;
			data.mName = "Person " + i;
			list.add(data);
		}
	}

	private void fillCommentsList(List<CardDetailCommentData> list) {
		for (int i = 0; i < 10; i++) {
			CardDetailCommentData data = new CardDetailCommentData();
			data.mMessage = "I needed background with borders on the left, right, bottom and this worked for me, thanks! ";
			data.mDate = "13 hours ago";
			data.mName = "Nick " ;
			list.add(data);
		}
	}

	private void fillCastCrew(List<CardDetailCastCrew> list) {
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Director";
			data.rightText = "Hannibal Chau";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Charlie Hunnam";
			data.rightText = "Raleigh Becket";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Diego Klattenhoff";
			data.rightText = "Yancy Becket";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Idris Elba";
			data.rightText = "Stacker Pentecost";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Rinko Kikuchi";
			data.rightText = "Mako Mori";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Charlie Day";
			data.rightText = "Dr. Newton Geiszler";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Burn Gorman";
			data.rightText = "Gottlieb";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Max Martini";
			data.rightText = "Herc Hansen";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Robert Kazinsky";
			data.rightText = "Chuck Hansen";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Clifton Collins Jr.";
			data.rightText = "Ops Tendo Choi";
			list.add(data);
		}
		{
			CardDetailCastCrew data = new CardDetailCastCrew();
			data.leftText = "Ron Perlman";
			data.rightText = "Hannibal Chau";
			list.add(data);
		}
		
	}

	private static final String[] URLS = {
			"https://lh6.googleusercontent.com/-HEeoO3k3bPg/S0VKWAJUlbI/AAAAAAAAAik/k1x42L8UIvw/Movie-GhostRider-001.jpg",
			"https://lh4.googleusercontent.com/-16Op5dZqK4s/STQf00CgLaI/AAAAAAAAAS4/y94XF3tvI2o/Blog1000-Which-way-india-stn.jpg",
			"https://lh3.googleusercontent.com/-yqLKT4RAfBM/S32v0NNVTbI/AAAAAAAAKyw/2ggyry4KiCE/Nature%252520Wallpapers%252520%25252880%252529.jpg",
			"https://lh5.googleusercontent.com/-d-qS8knzDP4/SePRfjfPYhI/AAAAAAAACVA/jxox5vRCphw/IMG_0084.jpg",
			"https://lh5.googleusercontent.com/-eurfd_3DDJM/SYpR7j0o8CI/AAAAAAAAJ8k/XRRlN8bdQlA/DSCF3739r.jpg",
			"https://lh5.googleusercontent.com/-K0Weq3ovQ2Y/SPrz1dedUWI/AAAAAAAAARc/1-fuKwsJPHs/IMG_4450.JPG",
			"https://lh3.googleusercontent.com/-RgQYezPeGck/SMC0KWFderI/AAAAAAAAE-w/Tm0JFwb-1Yc/100_5136.jpg",
			"https://lh3.googleusercontent.com/-sjtaMlX_2Qo/Sen1maYoUeI/AAAAAAAABds/8ABF3laHiqg/CA-wp6.jpg",
			"https://lh6.googleusercontent.com/-V4-QM6drP5c/SA66lIlP2cI/AAAAAAAAAIA/SjF8lVpf5hI/Denali-Dance-Framed.jpg",
	// "http://lh5.ggpht.com/_mrb7w4gF8Ds/TCpetKSqM1I/AAAAAAAAD2c/Qef6Gsqf12Y/s144-c/_DSC4374%20copy.jpg",
	// "http://lh5.ggpht.com/_Z6tbBnE-swM/TB0CryLkiLI/AAAAAAAAVSo/n6B78hsDUz4/s144-c/_DSC3454.jpg",
	// "http://lh3.ggpht.com/_GEnSvSHk4iE/TDSfmyCfn0I/AAAAAAAAF8Y/cqmhEoxbwys/s144-c/_MG_3675.jpg",
	// "http://lh6.ggpht.com/_Nsxc889y6hY/TBp7jfx-cgI/AAAAAAAAHAg/Rr7jX44r2Gc/s144-c/IMGP9775a.jpg",
	// "http://lh3.ggpht.com/_lLj6go_T1CQ/TCD8PW09KBI/AAAAAAAAQdc/AqmOJ7eg5ig/s144-c/Juvenile%20Gannet%20despute.jpg",
	// "http://lh6.ggpht.com/_ZN5zQnkI67I/TCFFZaJHDnI/AAAAAAAABVk/YoUbDQHJRdo/s144-c/P9250508.JPG",
	// "http://lh4.ggpht.com/_XjNwVI0kmW8/TCOwNtzGheI/AAAAAAAAC84/SxFJhG7Scgo/s144-c/0014.jpg",
	// "http://lh6.ggpht.com/_lnDTHoDrJ_Y/TBvKsJ9qHtI/AAAAAAAAG6g/Zll2zGvrm9c/s144-c/000007.JPG",
	// "http://lh6.ggpht.com/_qvCl2efjxy0/TCIVI-TkuGI/AAAAAAAAOUY/vbk9MURsv48/s144-c/DSC_0844.JPG",
	// "http://lh4.ggpht.com/_TPlturzdSE8/TBv4ugH60PI/AAAAAAAAMsI/p2pqG85Ghhs/s144-c/_MG_3963.jpg",
	// "http://lh4.ggpht.com/_4f1e_yo-zMQ/TCe5h9yN-TI/AAAAAAAAXqs/8X2fIjtKjmw/s144-c/IMG_1786.JPG",
	// "http://lh6.ggpht.com/_iFt5VZDjxkY/TB9rQyWnJ4I/AAAAAAAADpU/lP2iStizJz0/s144-c/DSCF1014.JPG",
	};

	@Override
	public void OnItemExpand(View v) {
		if (v == null) {
			return;
		}
		if (v.getTag() instanceof CardDetailMediaListData) {
			CardDetailMediaListData mainData = (CardDetailMediaListData) v
					.getTag();
			mMediaList = mainData.mList;
			showAlbumDialog();
		}
	}

	private void showAlbumDialog() {
		mAlbumDialog = new CustomDialog(getContext());
		mAlbumDialog.setContentView(R.layout.albumview);
		setupJazziness(TransitionEffect.CubeOut);
		mAlbumDialog.setCancelable(true);
		mAlbumDialog.show();
	}

	private List<CardDetailMediaData> mMediaList = null;
	private JazzyViewPager mJazzy;
	private CustomDialog mAlbumDialog;

	private void setupJazziness(TransitionEffect effect) {
		mJazzy = (JazzyViewPager) mAlbumDialog.findViewById(R.id.jazzy_pager);
		int width , height = 100;
		width = myplexapplication.getApplicationConfig().screenWidth - 2*((int)mContext.getResources().getDimension(R.dimen.margin_gap_8));
		height = (width * 9)/16; 
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
		mJazzy.setLayoutParams(params);
		
		mJazzy.setTransitionEffect(effect);
		mJazzy.setAdapter(new MainAdapter());
		// mJazzy.setPageMargin(30);
	}

	private class MainAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			CardDetailMediaData media =  mSelectedMediaGroup.mList.get(position);
			View v = null;
			if(media.mThumbnailMime != null && media.mThumbnailMime =="Image/JPEG"){
				v = mInflater.inflate(R.layout.cardmediasubitemimage, null);
				((FadeInNetworkImageView)v).setImageUrl(media.mThumbnailUrl, MyVolley.getImageLoader());
			}else{
				docketVideoWidget videoWidget = new docketVideoWidget(mContext);
				v = videoWidget.CreateView(media);
			}
			container.addView(v);
			mJazzy.setObjectForPosition(v, position);
			return v;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			container.removeView(mJazzy.findViewFromObject(position));
		}

		@Override
		public int getCount() {
			return mSelectedMediaGroup.mList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			if (view instanceof OutlineContainer) {
				return ((OutlineContainer) view).getChildAt(0) == obj;
			} else {
				return view == obj;
			}
		}
	}

	@Override
	public void onExpanded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextSelected(String key) {
		
	}

	private CardDetailMultiMediaGroup mSelectedMediaGroup;
	@Override
	public void onMediaGroupSelected(CardDetailMultiMediaGroup group) {
		mSelectedMediaGroup = group;
		showAlbumDialog();
	}

	private static final int STATE_ONSCREEN = 0;
	private static final int STATE_OFFSCREEN = 1;
	private static final int STATE_RETURNING = 2;
	private int mState = STATE_ONSCREEN;
	private int mMinRawY = 0;
	private int mQuickReturnHeight;
	private TranslateAnimation anim;
	@Override
	public void scrollDirection(boolean value) {
		mQuickReturnHeight  = mBottomActionBar.getHeight();
		int translationY = 0;

		int mScrollY = mScrollView.getScrollY();
		int rawY = mScrollY;


		switch (mState) {
		case STATE_OFFSCREEN:
			if (rawY >= mMinRawY) {
				mMinRawY = rawY;
			} else {
				mState = STATE_RETURNING;
			}
			translationY = rawY;
			break;


		case STATE_ONSCREEN:
			if (rawY > mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			translationY = rawY;
			break;


		case STATE_RETURNING:


			translationY = (rawY - mMinRawY) + mQuickReturnHeight;


			System.out.println(translationY);
			if (translationY < 0) {
				translationY = 0;
				mMinRawY = rawY + mQuickReturnHeight;
			}


			if (rawY == 0) {
				mState = STATE_ONSCREEN;
				translationY = 0;
			}


			if (translationY > mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			break;
		}


		/** this can be used if the build is below honeycomb **/
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
			anim = new TranslateAnimation(0, 0, translationY,
					translationY);
			anim.setFillAfter(true);
			anim.setDuration(0);
			 mBottomActionBar.startAnimation(anim);
		} else {
			 mBottomActionBar.setTranslationY(translationY);
		}
	}
}