package com.apalya.myplex.views;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.R.bool;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


import com.apalya.myplex.LiveScoreWebView;
import com.apalya.myplex.MainBaseOptions;
import com.apalya.myplex.R;
import com.apalya.myplex.SubscriptionView;
import com.apalya.myplex.data.ApplicationConfig;
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataPurchaseItem;
import com.apalya.myplex.data.CardDataRelatedMultimediaItem;
import com.apalya.myplex.data.CardDataVideosItem;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.MatchStatus;
import com.apalya.myplex.data.MatchStatus.MATCH_TYPE;
import com.apalya.myplex.data.Team;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.media.VideoViewExtn;
import com.apalya.myplex.media.VideoViewPlayer;
import com.apalya.myplex.media.VideoViewPlayer.OnLicenseExpiry;
import com.apalya.myplex.media.VideoViewPlayer.StreamType;
import com.apalya.myplex.utils.AlertDialogUtil;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.LogOutUtil;
import com.apalya.myplex.utils.MediaUtil;
import com.apalya.myplex.utils.MediaUtil.MediaUtilEventListener;
import com.apalya.myplex.utils.MediaUtility;
import com.apalya.myplex.utils.MediaUtility.VideoUrlFetchListener;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.SportsStatusRefresh;
import com.apalya.myplex.utils.SportsStatusRefresh.OnResponseListener;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.WidevineDrm.Settings;
import com.apalya.myplex.views.DownloadStreamDialog.DownloadListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.internal.co;
import com.google.android.gms.location.LocationClient;
import com.apalya.myplex.data.myplexapplication;

public class CardVideoPlayer implements PlayerListener, AlertDialogUtil.NoticeDialogListener,VideoUrlFetchListener  {
	private Context mContext;
	private LayoutInflater mInflator;
	private View mParentLayout;
	private LayoutParams mParentLayoutParams;
	private FadeInNetworkImageView mPreviewImage;
	private TextView mPlayButton;
	private TextView mTrailerButton,recordedProgName;
	private TextView mBufferPercentage;
	private RelativeLayout mProgressBarLayout;
	private RelativeLayout mVideoViewParent;
	private VideoViewExtn mVideoView;
	private LinearLayout mScoreCardLayout;
	private CardData mData;
	private VideoViewPlayer mVideoViewPlayer;
	private int mPerBuffer;
	private int mWidth;
	private int mHeight;
	private int mPlayerState;
	private static int PLAYER_PLAY = 1;
	private static int PLAYER_PAUSE = 2;
	private static int PLAYER_STOPPED = 3;
	private static int PLAYER_BUFFERING = 4;
	private PlayerFullScreen mPlayerFullScreen;
	boolean isESTPackPurchased=false;
	private String drmLicenseType="st";
	
	private String mVideoUrl;
	private Uri mVideoUri ;
	private boolean mTrailerAvailable = false;
	private boolean isLocalPlayback = false;
	private static final String TAG = "CardVideoPlayer";
//	private Location location;
//	private LocationClient locationClient;
	private int state=0;
	int currentDuration = 0;
	private String  download_link,adaptive_link;

	private SportsStatusRefresh sportsStatusRefresh;
	private boolean isFullScreen,isTriler;

	public void setFullScreenListener(PlayerFullScreen mListener){
		this.mPlayerFullScreen = mListener;
	}
	public void setPlayerStatusUpdateListener(PlayerStatusUpdate listener){
		mPlayerStatusListener = listener;
	}
	private PlayerStatusUpdate mPlayerStatusListener;
	public interface PlayerFullScreen{
		public void playerInFullScreen(boolean value);
	}
	public interface PlayerStatusUpdate{
		public void playerStatusUpdate(String value);
	}
	public CardVideoPlayer(Context context, CardData data) {
		this.mContext = context;
		this.mData = data;
		
//		locationClient =  new LocationClient(mContext,new ConnectionCallbacks(),new ConnectionFailedCallBack());
//		locationClient.connect();	
		
		if(mData !=null && mData.relatedMultimedia !=null &&
				mData.relatedMultimedia.values !=null
				 && mData.relatedMultimedia.values.size() >0)
		{
			for (CardDataRelatedMultimediaItem mmItem : mData.relatedMultimedia.values) {
				{
					if(mmItem.content !=null && mmItem.content.categoryName !=null && mmItem.content.categoryName.equalsIgnoreCase("trailer") && mmItem.generalInfo !=null && mmItem.generalInfo._id !=null)
					{
						mTrailerAvailable = true;
						break;
					}
				}
			}
		}
		
	
		mInflator = LayoutInflater.from(mContext);
	}

	public View CreatePlayerView(View parentLayout) {
		mParentLayout = parentLayout;
		mParentLayoutParams = (LayoutParams) mParentLayout.getLayoutParams();
		View v = mInflator.inflate(R.layout.cardmediasubitemvideo, null);
		mVideoViewParent = (RelativeLayout) v;

		mWidth = myplexapplication.getApplicationConfig().screenWidth;
		mHeight = (mWidth * 9) / 16;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				mWidth, mHeight);
		mPreviewImage = (FadeInNetworkImageView) v
				.findViewById(R.id.cardmediasubitemvideo_imagepreview);
		mPreviewImage.setLayoutParams(params);

		mVideoView = (VideoViewExtn) v
				.findViewById(R.id.cardmediasubitemvideo_videopreview);
		mVideoView.setLayoutParams(params);
		mVideoView.resizeVideo(mWidth, mHeight);
		mPlayButton = (TextView) v .findViewById(R.id.cardmediasubitemvideo_play);
		mPlayButton.setTypeface(FontUtil.ss_symbolicons_line);
		
		mTrailerButton = (TextView)v.findViewById(R.id.cardmediasubitemtrailer_play);
		mTrailerButton.setTypeface(FontUtil.ss_symbolicons_line);
		recordedProgName = (TextView)v.findViewById(R.id.recordedProgName);
		
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		
        initSportsStatusLayout(v);
		int[] location = new int[2];
		mTrailerButton.getLocationOnScreen(location);
		if(location.length >0)
		{
			Log.i(TAG, "Toast pos, X:"+mTrailerButton.getBottom()+5+" Y:"+location[1]+10);
			//Util.showToastAt(mContext, "Play Trailer", Util.TOAST_TYPE_INFO, Gravity.TOP|Gravity.LEFT,mTrailerButton.getBottom()+5, location[1]+10);
		}

		mTrailerButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(mTrailerAvailable/*mData !=null && mData.relatedMultimedia !=null &&
								mData.relatedMultimedia.values !=null
								 && mData.relatedMultimedia.values.size() >0*/)
						{
							for (CardDataRelatedMultimediaItem mmItem : mData.relatedMultimedia.values) {
								{
									if(mmItem.content !=null && mmItem.content.categoryName !=null && mmItem.content.categoryName.equalsIgnoreCase("trailer") && mmItem.generalInfo !=null && mmItem.generalInfo._id !=null)
									{
										Analytics.isTrailer = true;
										Analytics.startVideoTime();
										
//										FetchTrailerUrl(mmItem.generalInfo._id);
										Analytics.gaPlayedMovieEvent(mData, 0);
										Map<String,String> params=new HashMap<String, String>();
										//FetchTrailerUrl(mmItem.generalInfo._id);
										if(canBePlayed(true)){	
											isTriler = true;
											fetchUrl(mmItem.generalInfo._id);			
											mVideoViewParent.setOnClickListener(null);		
										}
										mVideoViewParent.setOnClickListener(null);
										break;
									}
								}
							}
						}
					}
				});

		mBufferPercentage = (TextView) v
				.findViewById(R.id.carddetaildesc_movename);

		Random rnd = new Random();
		int Low = 100;
		int High = 196;

		int color = Color.argb(255, rnd.nextInt(High - Low) + Low,
				rnd.nextInt(High - Low) + Low, rnd.nextInt(High - Low) + Low);
		mPreviewImage.setBackgroundColor(color);
		mProgressBarLayout = (RelativeLayout) v
				.findViewById(R.id.cardmediasubitemvideo_progressbarLayout);

		if (mData.images != null) {
			for (CardDataImagesItem imageItem : mData.images.values) {
				if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
						&& imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)) {
					if (imageItem.link == null
							|| imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
						mPreviewImage.setImageResource(0);
					} else if (imageItem.link != null) {
//						mPreviewImage.setDefaultImageResId(R.drawable.placeholder);
						mPreviewImage.setErrorImageResId(R.drawable.placeholder);
						mPreviewImage.setImageUrl(imageItem.link,
								MyVolley.getImageLoader());
					}
					break;
				}
			}
		}
		mVideoViewParent.setOnClickListener(mPlayerClickListener);
		if(mData._id.equalsIgnoreCase("0"))
		{
			mVideoViewParent.setOnClickListener(null);
			mPlayButton.setVisibility(View.GONE);
			mTrailerButton.setVisibility(View.GONE);
			mVideoView.setVisibility(View.INVISIBLE);
			mProgressBarLayout.setVisibility(View.INVISIBLE);
			mPreviewImage.setScaleType(ScaleType.CENTER);
			mPreviewImage.setBackgroundColor(Color.BLACK);
		}else{
			  Util.showFeedback(mVideoViewParent);
		}
		return v;
	}


	 	
	public void fetchUrl(String id){			

		recordedProgName.setVisibility(View.GONE);
		mPlayButton.setVisibility(View.INVISIBLE);			
		mTrailerButton.setVisibility(View.INVISIBLE);			
		mProgressBarLayout.setVisibility(View.VISIBLE);			
		mVideoView.setVisibility(View.VISIBLE);			
		mPreviewImage.setVisibility(View.INVISIBLE);			

		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.INVISIBLE);
		}
		
		if(sportsStatusRefresh !=null){
			sportsStatusRefresh.stop();
		}
		
		if(checkForLocalPlayback()){
			return;
		}
		MediaUtility utility ;                			
		if(id==null){			
			utility = new MediaUtility(mContext,this,false);			
			utility.fetchVideoUrl(mData._id);			
		}			
		else{			
			utility = new MediaUtility(mContext,this,true);			
			utility.fetchVideoUrl(id);			
		}			
	}			
	        			
        
	private boolean checkForLocalPlayback() {
		
		if(isTriler)
			return false;
		
		if (myplexapplication.mDownloadList == null) {
			return false;
		}

		CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList
				.get(mData._id);

		if (mDownloadData == null) {
			return false;
		}

		boolean isFileExist = Util.isFileExist(mData._id + ".wvm");

		if (!isFileExist) {
			if (mPlayerStatusListener != null) {
				mPlayerStatusListener
						.playerStatusUpdate("Download Completed and file doesn't exists, starting player.....");
			}
			Util.removeDownload(mDownloadData.mDownloadId, mContext);			
			return false;
		}
		
		if (mDownloadData.mCompleted && mDownloadData.mPercentage == 0) {
			if (mPlayerStatusListener != null) {
				mPlayerStatusListener
						.playerStatusUpdate("Download failed and removing request and deleting the file");
			}
			closePlayer();
			Util.removeDownload(mDownloadData.mDownloadId, mContext);
			Util.showToast(
					mContext,
					"Download has failed, Please check if sufficent memory is available.",
					Util.TOAST_TYPE_ERROR);
			return false;
		}

		if (mPlayerStatusListener != null) {
			mPlayerStatusListener
					.playerStatusUpdate("file exists, starting player.....per download :"
							+ mDownloadData.mPercentage);
		}

		playVideoFile(mDownloadData);
		return true;

	}
	private OnClickListener mPlayerClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			//This event is handled in CardDEtails
			if(canBePlayed(true)){
				isTriler = false;
				//FetchUrl();
				 fetchUrl(null);
				mVideoViewParent.setOnClickListener(null);
				Analytics.startVideoTime();
				Analytics.gaPlayedMovieEvent(mData, 0);
			}
			// TODO Auto-generated method stub

		}
	};
	private boolean lastWatchedStatus = false;
	

	public void closePlayer() {
		mPlayerState = PLAYER_STOPPED; 
		if (mVideoViewPlayer != null) {
			mVideoViewPlayer.closeSession();
		}
		mPerBuffer = 0;
		if (mVideoViewPlayer != null) {
			mVideoViewPlayer.hideMediaController();
		}
		mVideoViewParent.setOnClickListener(mPlayerClickListener);
		mPlayButton.setVisibility(View.VISIBLE);
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		mProgressBarLayout.setVisibility(View.GONE);
		mPreviewImage.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
		mVideoViewParent.setEnabled(true);
		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.VISIBLE);
		}
		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.VISIBLE);
		}
		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
				
	}

	public void FetchUrl() {	
		
		isLocalPlayback = false;
		
//		
//		if(mData._id == null || !AllowedContentIdList.isAllowed(mData._id) ){
//		
//			Util.showToast(mContext, "Your country is not allowed for this content.",Util.TOAST_TYPE_ERROR);
//			return;
//		}
		
//		location = locationClient.getLastLocation();
//		if(location!=null)
//			Log.d("amlan",location.getLatitude()+":"+location.getLongitude());
		
		mPlayButton.setVisibility(View.INVISIBLE);
		mTrailerButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPreviewImage.setVisibility(View.INVISIBLE);
		if(mScoreCardLayout != null){
			mScoreCardLayout.setVisibility(View.INVISIBLE);
		}
		if(sportsStatusRefresh !=null){
			sportsStatusRefresh.stop();
		}
		MediaUtil.setUrlEventListener(new MediaUtilEventListener() {

			@Override
			public void urlReceived(boolean aStatus, String url, String message, String statusCode) {				
				if (!aStatus) {
					closePlayer();
					
					String msg ="Failed in fetching the url.";
					
					if(!TextUtils.isEmpty(message)){
						msg = message;
					}
					
//					Util.showToast(mContext, msg,Util.TOAST_TYPE_ERROR);		
					
					if(statusCode != null && statusCode.equalsIgnoreCase("ERR_USER_NOT_SUBSCRIBED")){
						
						PackagePopUp popup = new PackagePopUp(mContext,(View)mParentLayout.getParent());
						myplexapplication.getCardExplorerData().cardDataToSubscribe =  mData;
						popup.showPackDialog(mData, ((Activity)mContext).getActionBar().getCustomView());	
						
					}
					
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Failed in fetching the url.");
						mixPanelUnableToPlayVideo(Analytics.FAILED_TO_FETCH_URL);
					}
//					Toast.makeText(mContext, "Failed in fetching the url.",
//							Toast.LENGTH_SHORT).show();
					return;
				}
				if (url == null) {
					closePlayer();
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("No url to play.");
						mixPanelUnableToPlayVideo(Analytics.NO_URL_TO_PLAY);
					}
					Util.showToast(mContext, "No url to play.",Util.TOAST_TYPE_ERROR);
//					Toast.makeText(mContext, "No url to play.",
//							Toast.LENGTH_SHORT).show();
					return;
				}
				/*if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Url Received :: "+url);
				}*/
				if(isESTPackPurchased || url.contains("_est_"))
				{
					url=url.replace("widevine:", "http:");
//					url = "http://122.248.233.48/wvm/100_ff_4_medium.wvm";
//					url = "https://demostb.s3.amazonaws.com/myplex2.apk";
					closePlayer();
					if(Util.getSpaceAvailable()>=1)
					{
						if(Util.isWifiEnabled(mContext))
						{
							Util.startDownload(url, mData, mContext);
						}
						else
						{
							Util.showToast(mContext, "Downloading is supported only on Wifi, please turn on wifi and try again.", Util.TOAST_TYPE_INFO);
						}
					}
					else
					{
						Util.showToast(mContext, "Download failed due to insufficent memory, please free space up to 1GB to start download", Util.TOAST_TYPE_INFO);
					}
					return;
				}				
				else{
					drmLicenseType="st";
				}
				
				if(mData.content !=null && mData.content.drmEnabled)
				{
					String licenseData="clientkey:"+myplexapplication.getDevDetailsInstance().getClientKey()+",contentid:"+mData._id+",type:"+drmLicenseType+",profile:0";
					
					byte[] data;
					try {
						data = licenseData.getBytes("UTF-8");
						String base64 = Base64.encodeToString(data, Base64.DEFAULT);
						Settings.USER_DATA=base64;
						Settings.DEVICE_ID=myplexapplication.getDevDetailsInstance().getClientDeviceId();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				if(!lastWatchedStatus)
					myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);
				Util.showAdultToast(mContext.getString(R.string.adultwarning), mData, mContext);
				Uri uri ;
//				uri = Uri.parse("rtsp://46.249.213.87:554/playlists/bollywood-action_qcif.hpl.3gp");
//				uri = Uri.parse("http://59.162.166.211:8080/player/3G_H264_320x240_600kbps.3gp");				
//				uri = Uri.parse("http://122.248.233.48/wvm/100_ff_5.wvm");
				uri = Uri.parse(url);
				// Toast.makeText(getContext(), "URL:"+url,
				// Toast.LENGTH_SHORT).show();
				if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Playing :: "+url);
				}				
			
				
				// if movie then fetch elapse time
				// default init player
				
				initializeVideoPlay(uri);
				
			}

			@Override
			public void lastPausedTimeFetched(int ellapseTime) {
				if(ellapseTime > 60){
					if(mVideoViewPlayer ==null){						
						mVideoViewPlayer = new  VideoViewPlayer(mVideoView, mContext,null ,StreamType.VOD);
					}
//					mVideoViewPlayer.setmPositionWhenPaused(ellapseTime*1000);
				}
			}

			
		});
		 

//		/*boolean*/ lastWatchedStatus=false;
		for(CardData data:myplexapplication.getUserProfileInstance().lastVisitedCardData)
		{
			if(data._id.equalsIgnoreCase(mData._id))
			{
				lastWatchedStatus=true;
				
			}
		}
		/*if(!lastWatchedStatus)
			myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);*/

		String expiryTime=null;
		boolean allowPlaying=true;
		if(mData.currentUserData!=null)
        {    
			for(CardDataPurchaseItem data:mData.currentUserData.purchase)
            {
                if(data.type.equalsIgnoreCase("download") || data.type.equalsIgnoreCase("est")){
                    isESTPackPurchased=true;

                }
                expiryTime=data.validity;
            }
        }
		//Following check is not needed for now since DRM
/*		if(expiryTime!=null)
		{
			if(!Util.isTokenValid(expiryTime))
			{
				closePlayer();
				Util.showToast(mContext, "Your Subscription has been expired.",Util.TOAST_TYPE_ERROR);
				return;
			}
			else
			{
				allowPlaying=true;
			}
		}
		else
		{
			allowPlaying=true;
		}*/
        
		if(allowPlaying)
		{
	        String qualityType = new String();
	        String streamingType = new String();
	        
	        streamingType = ConsumerApi.STREAMNORMAL;
	        
	        if(mData.content !=null && mData.content.drmEnabled)
	        {
	        	qualityType = ConsumerApi.VIDEOQUALTYSD;
	        	streamingType = ConsumerApi.STREAMADAPTIVE;
	        }
	        else
	        	qualityType = ConsumerApi.VIDEOQUALTYLOW;
	        	
	        
	        if(Util.isWifiEnabled(mContext))
	        {
	        	if(mData.content !=null && mData.content.drmEnabled){
	        		qualityType= ConsumerApi.VIDEOQUALTYSD;
	        		streamingType = ConsumerApi.STREAMADAPTIVE;
	        	}
	        	else
	        		qualityType= ConsumerApi.VIDEOQUALTYHIGH;
	        	
	        	if(mContext.getResources().getBoolean(R.bool.isTablet) && mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(ConsumerApi.CONTENT_SPORTS_LIVE)){
	        		// for tablet use very high quality link.
	        		qualityType= ConsumerApi.VIDEOQUALTYVERYHIGH;
	        	}
	        }
			
	        if(myplexapplication.mDownloadList != null){
	        	if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Download Details are available...");
				}
				CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(mData._id);
				if(mDownloadData!=null){
					
					// for local playback
					if(mDownloadData.mCompleted && Util.isFileExist(mData._id+".wvm"))
					{
						// download complete
						if(mDownloadData.mPercentage==0)
						{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download failed and removing request and deleting the file");
							}
							closePlayer();
							Util.removeDownload(mDownloadData.mDownloadId, mContext);
							Util.showToast(mContext, "Download has failed, Please check if sufficent memory is available.",Util.TOAST_TYPE_ERROR);
							
						}
						else{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download inprogess and file exists, starting player.....");
							}
							playVideoFile(mDownloadData);
						}
					}
					else
					{
						// download inprogress
						if(Util.isFileExist(mData._id+".wvm"))
						{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download Completed and file exists, starting player.....");
							}
							playVideoFile(mDownloadData);
						}
						else{
							if(mPlayerStatusListener != null){
								mPlayerStatusListener.playerStatusUpdate("Download Completed and file doesn't exists, starting player.....");
							}
							Util.removeDownload(mDownloadData.mDownloadId, mContext);
							MediaUtil.setContext(mContext);
							MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased,ConsumerApi.STREAMINGFORMATHLS);
							
						}
					}
				}
				else{
					
					// streaming 
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Download Details for this content not available, so requesting url...");
					}
					MediaUtil.setContext(mContext);
					MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased,ConsumerApi.STREAMINGFORMATHLS);
					/*if(mData.generalInfo != null && mData.generalInfo.type != null ){
						if(!mData.generalInfo.type.equalsIgnoreCase("live")){
							// Its a live video Dont fetch resumed status						
							MediaUtil.getPlayerState(mData._id);	
						}									
					}*/
				}
			}
	        else 
	        {
	        	// streaming Movie
	        	if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Download Details not available, so requesting url...");
				}
	        	MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased,ConsumerApi.STREAMINGFORMATHLS);	
	        	/*if(mData.generalInfo != null && mData.generalInfo.type != null ){
					if(!mData.generalInfo.type.equalsIgnoreCase("live")){
						// Its a live video Dont fetch resumed status					
						MediaUtil.getPlayerState(mData._id);	
					}									
				}*/
	        }
	      }
	}
	
	protected void initializeVideoPlay(Uri uri ) {
		VideoViewPlayer.StreamType streamType = StreamType.VOD;
		if (mVideoViewPlayer == null) {
			mVideoViewPlayer = new VideoViewPlayer(mVideoView, mContext, uri,streamType);
			// mVideoViewPlayer.openVideo();
			mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
			mVideoViewPlayer.setUri(uri, streamType);
		} else {
			mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
			mVideoViewPlayer.setUri(uri, streamType);
		}
		mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);		
		mVideoViewPlayer.hideMediaController();
		mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
		mVideoView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				mVideoViewPlayer.onTouchEvent(event);
				return false;
			}
		});
//		mVideoViewPlayer.goToTime(MediaUtil.ELLAPSE_TIME);
	}
private void playVideoFile(CardDownloadData mDownloadData){

	isLocalPlayback  = true;
	drmLicenseType="lp";
	String url="file://"+mDownloadData.mDownloadPath;
	

	if(mData.content !=null && mData.content.drmEnabled)
	{
		String licenseData="clientkey:"+myplexapplication.getDevDetailsInstance().getClientKey()+",contentid:"+mData._id+",type:"+drmLicenseType+",profile:0";
		
		byte[] data;
		try {
			data = licenseData.getBytes("UTF-8");
			String base64 = Base64.encodeToString(data, Base64.DEFAULT);
			Settings.USER_DATA=base64;
			Settings.DEVICE_ID=myplexapplication.getDevDetailsInstance().getClientDeviceId();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	Uri uri ;
	uri = Uri.parse(url);
	if(mDownloadData.mCompleted)
	{
		DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		uri=manager.getUriForDownloadedFile(mDownloadData.mDownloadId);
	}
	if(mPlayerStatusListener != null){
		mPlayerStatusListener.playerStatusUpdate("Playing :: "+url);
	}
	VideoViewPlayer.StreamType streamType = StreamType.VOD;
	if (mVideoViewPlayer == null) {
		mVideoViewPlayer = new VideoViewPlayer(mVideoView,
				mContext, uri, streamType);
		//mVideoViewPlayer.openVideo();
		mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
		mVideoViewPlayer.setUri(uri, streamType);
	} else {
		mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
		mVideoViewPlayer.setUri(uri, streamType);
	}
	mVideoViewPlayer.setOnLicenseExpiryListener(onLicenseExpiryListener);
	mVideoViewPlayer.hideMediaController();
	mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
	mVideoView.setOnTouchListener(new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			mVideoViewPlayer.onTouchEvent(event);
			return false;
		}
	});
	
//	 int ellapseTime = SharedPrefUtils.getIntFromSharedPreference(mContext, mData._id);
//		mVideoViewPlayer.setmPositionWhenPaused(ellapseTime *1000);	
}
	private void FetchTrailerUrl(String contentId)
	{
		
//		if(mData._id == null || !AllowedContentIdList.isAllowed(mData._id) ){
//			
//			Util.showToast(mContext, "Your country is not allowed for this content.",Util.TOAST_TYPE_ERROR);
//			return;
//		}
		for(CardData data:myplexapplication.getUserProfileInstance().lastVisitedCardData)
		{
			if(data._id.equalsIgnoreCase(mData._id))
			{
				lastWatchedStatus=true;
			}
		}
		
		mPlayButton.setVisibility(View.INVISIBLE);
		mTrailerButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPreviewImage.setVisibility(View.INVISIBLE);
		
        String qualityType = new String();
        String streamingType = new String();
        
        streamingType = ConsumerApi.STREAMNORMAL;
    	qualityType = ConsumerApi.VIDEOQUALTYHIGH;
        
        if(Util.isWifiEnabled(mContext))
        {
    		qualityType= ConsumerApi.VIDEOQUALTYHIGH;
        }
        MediaUtil.setUrlEventListener(new MediaUtilEventListener() {
			
			@Override
			public void urlReceived(boolean aStatus, String url, String message, String statusCode ) {
				if (!aStatus) {
					closePlayer();
					
					String msg ="Failed in fetching the url.";
					
					if(!TextUtils.isEmpty(message)){
						msg = message;
					}
					
					Util.showToast(mContext, msg,Util.TOAST_TYPE_ERROR);	
					
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Failed in fetching the url.");
						mixPanelUnableToPlayVideo(Analytics.FAILED_TO_FETCH_URL);
					}
					return;
				}
				if (url == null) {
					closePlayer();
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("No url to play.");
					}
					mixPanelUnableToPlayVideo(Analytics.NO_URL_TO_PLAY);
					Util.showToast(mContext, "No url to play.",Util.TOAST_TYPE_ERROR);
					return;
				}
				if(!lastWatchedStatus)
					myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);
				Uri uri ;
//				uri = Uri.parse("rtsp://46.249.213.87:554/playlists/bollywood-action_qcif.hpl.3gp");
//				uri = Uri.parse("http://59.162.166.211:8080/player/3G_H264_320x240_600kbps.3gp");
//				uri = Uri.parse("http://122.248.233.48/wvm/100_ff_5.wvm");
				uri = Uri.parse(url);
				// Toast.makeText(getContext(), "URL:"+url,
				// Toast.LENGTH_SHORT).show();
				if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Playing :: "+url);
				}
				VideoViewPlayer.StreamType streamType = StreamType.VOD;
				if (mVideoViewPlayer == null) {
					mVideoViewPlayer = new VideoViewPlayer(mVideoView,
							mContext, uri, streamType);
					//mVideoViewPlayer.openVideo();
					mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
					mVideoViewPlayer.setUri(uri, streamType);
				} else {
					mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
					mVideoViewPlayer.setUri(uri, streamType);
				}
				mVideoViewPlayer.hideMediaController();
				mVideoViewPlayer.setPlayerStatusUpdateListener(mPlayerStatusListener);
				mVideoView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent event) {
						mVideoViewPlayer.onTouchEvent(event);
						return false;
					}
				});
			}

			@Override
			public void lastPausedTimeFetched(int ellapseTime) {}	
		});
        MediaUtil.setContext(mContext);
        MediaUtil.getVideoUrl(contentId,qualityType,streamingType,isESTPackPurchased,ConsumerApi.STREAMINGFORMATHTTP);
	}
	
	private void mixPanelUnableToPlayVideo(String error) {
    	
       	int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  cardData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		String contentName = cardData.generalInfo.title;
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_NAME_PROPERTY,contentName);
		params.put(Analytics.CONTENT_ID_PROPERTY,cardData._id);
		params.put(Analytics.CONTENT_TYPE_PROPERTY,Analytics.movieOrLivetv(cardData.generalInfo.type));
		params.put(Analytics.REASON_FAILURE,error);
		String event = Analytics.EVENT_UNABLE_TO_PLAY + Analytics.EMPTY_SPACE + contentName;
		Analytics.trackEvent(event,params);
		//Analytics.createEventGA(easyTracker, Analytics.EVENT_PLAY,Analytics.CONTENT_PLAY_ERROR,contentName );
    }
	
	public View CreateTabletPlayerView(View parentLayout) {

		mWidth = myplexapplication.getApplicationConfig().screenWidth;
		mWidth = (myplexapplication.getApplicationConfig().screenWidth/3)*2;
		int marginleft = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
		mWidth -= marginleft*2;
		mHeight = (mWidth * 9)/16;

		mParentLayout = parentLayout;
		LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(mWidth,mHeight);
		layoutparams.setMargins(marginleft, marginleft, marginleft, marginleft);
		mParentLayout.setLayoutParams(layoutparams);
		View v = mInflator.inflate(R.layout.cardmediasubitemvideo, null);
		mVideoViewParent = (RelativeLayout) v;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
		mPreviewImage = (FadeInNetworkImageView) v.findViewById(R.id.cardmediasubitemvideo_imagepreview);
		mPreviewImage.setLayoutParams(params);

		mVideoView = (VideoViewExtn) v.findViewById(R.id.cardmediasubitemvideo_videopreview);
		mVideoView.setLayoutParams(params);
		mVideoView.resizeVideo(mWidth, mHeight);
		mPlayButton = (TextView) v.findViewById(R.id.cardmediasubitemvideo_play);
		mPlayButton.setTypeface(FontUtil.ss_symbolicons_line);
		
		mTrailerButton = (TextView)v.findViewById(R.id.cardmediasubitemtrailer_play);
		mTrailerButton.setTypeface(FontUtil.ss_symbolicons_line);
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		recordedProgName = (TextView)v.findViewById(R.id.recordedProgName);
		
		initSportsStatusLayout(v);
		mTrailerButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mTrailerAvailable/*mData !=null && mData.relatedMultimedia !=null &&
						mData.relatedMultimedia.values !=null
						 && mData.relatedMultimedia.values.size() >0*/)
				{
					for (CardDataRelatedMultimediaItem mmItem : mData.relatedMultimedia.values) {
						{
							if(mmItem.content !=null && mmItem.content.categoryName !=null && mmItem.content.categoryName.equalsIgnoreCase("trailer") && mmItem.generalInfo !=null && mmItem.generalInfo._id !=null)
							{
								Analytics.isTrailer = true;
								Analytics.startVideoTime();
																
								FetchTrailerUrl(mmItem.generalInfo._id);
								mVideoViewParent.setOnClickListener(null);
								break;
							}
						}
					}
				}
				
			}
		});
		
		mBufferPercentage = (TextView) v.findViewById(R.id.carddetaildesc_movename);

		Random rnd = new Random();
		int Low = 100;
		int High = 196;

		int color = Color.argb(255, rnd.nextInt(High - Low) + Low,
				rnd.nextInt(High - Low) + Low, rnd.nextInt(High - Low) + Low);
		mPreviewImage.setBackgroundColor(color);
		mProgressBarLayout = (RelativeLayout) v
				.findViewById(R.id.cardmediasubitemvideo_progressbarLayout);

		if (mData.images != null) {
			for (CardDataImagesItem imageItem : mData.images.values) {
				if (imageItem.type != null && imageItem.type.equalsIgnoreCase("coverposter") && imageItem.profile != null
						&& imageItem.profile.equalsIgnoreCase(ApplicationConfig.MDPI)) {
					if (imageItem.link == null
							|| imageItem.link.compareTo("Images/NoImage.jpg") == 0) {
						mPreviewImage.setImageResource(0);
					} else if (imageItem.link != null) {
						mPreviewImage.setImageUrl(imageItem.link,
								MyVolley.getImageLoader());
					}
					break;
				}
			}
		}
		mVideoViewParent.setOnClickListener(mPlayerClickListener);
		if(mData._id.equalsIgnoreCase("0"))
		{
			mVideoViewParent.setOnClickListener(null);
			mPlayButton.setVisibility(View.GONE);
			mTrailerButton.setVisibility(View.GONE);
			mVideoView.setVisibility(View.INVISIBLE);
			mProgressBarLayout.setVisibility(View.INVISIBLE);
			mPreviewImage.setScaleType(ScaleType.CENTER);
			mPreviewImage.setBackgroundColor(Color.BLACK);
		}
		// mPlay.setOnClickListener(mPlayListener);
		return v;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("onSeekComplete");
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int perBuffer) {
//		if(mPlayerStatusListener != null){
//			mPlayerStatusListener.playerStatusUpdate("buffering :: "+perBuffer);
//		}
		if (this.mPerBuffer <= perBuffer) {
			this.mPerBuffer = perBuffer;
		}
		if (mBufferPercentage != null) {
			mBufferPercentage.setText("Loading " + mPerBuffer + "%");
		}
		int currentseekposition = mVideoView.getCurrentPosition();
		if (currentseekposition < 0) {
			currentseekposition = 510;
		}
		if (mVideoView.isPlaying() && currentseekposition > 500) {
			this.mPerBuffer = 0;
			mVideoViewPlayer.deregisteronBufferingUpdate();
			mProgressBarLayout.setVisibility(View.GONE);
			mVideoViewPlayer.showMediaController();
			mPlayerState = PLAYER_PLAY;
			if(mPlayerStatusListener != null){
				mPlayerStatusListener.playerStatusUpdate("Buffering ended");
			}
			if(!mContext.getResources().getBoolean(R.bool.isTablet)){
				((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}else if(mVideoView.isPlaying() && mData.generalInfo.type.equalsIgnoreCase("live")){
			this.mPerBuffer = 0;
			mVideoViewPlayer.deregisteronBufferingUpdate();
			mProgressBarLayout.setVisibility(View.GONE);
			mVideoViewPlayer.showMediaController();
			mPlayerState = PLAYER_PLAY;
			if(mPlayerStatusListener != null){
				mPlayerStatusListener.playerStatusUpdate("Buffering ended");
			}
			if(!mContext.getResources().getBoolean(R.bool.isTablet)){
				((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int arg1, int arg2) {
		if(mPlayerStatusListener != null){
			String what = new String();
			switch (arg1) {
				case MediaPlayer.MEDIA_ERROR_UNKNOWN:
					what = "MEDIA_ERROR_UNKNOWN";
					break;
				case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
					what = "MEDIA_ERROR_SERVER_DIED";
					break;
				default:
					what = ""+arg1;
					break;
			}
			String error = new String();
			switch (arg2) {
			default:
				error = ""+arg2;
				break;
		}
			mPlayerStatusListener.playerStatusUpdate("Play Error :: what = "+what+" extra= "+error);
		}
		closePlayer();
		return false;
	}
	@Override
	public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
		if(mPlayerStatusListener != null){
			String what = new String();
			switch (arg1) {
			case MediaPlayer.MEDIA_INFO_UNKNOWN:
				what = "MEDIA_INFO_UNKNOWN";
				break;
			case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
				what = "MEDIA_INFO_VIDEO_TRACK_LAGGING";
				break;
//			case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: 
//				break;

			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				what = "MEDIA_INFO_BUFFERING_START";
				break;

			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				what = "MEDIA_INFO_BUFFERING_END";
				break;

			case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
				what = "MEDIA_INFO_BAD_INTERLEAVING";
				break;

			case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
				what = "MEDIA_INFO_NOT_SEEKABLE";
				break;

			case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
				what = "MEDIA_INFO_METADATA_UPDATE";
				break;
//			case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE: 
//				break;
//			case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT: 
//				break;
			default:
				what = ""+arg1;
				break;
			}
			String extra = new String();
			extra = ""+arg2;
//			mPlayerStatusListener.playerStatusUpdate("Play Info :: what = "+what+" extra= "+extra);
		}
		return false;
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play complete :: ");
		}
		closePlayer();
		
//		locationClient.disconnect();
	}
	@Override
	public void onDrmError(){
		closePlayer();
	}
	
	public void resumePreviousOrientaionTimer(){
		if(mTimer != null ){
			mTimer.cancel();
		}
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				resumePreviousOrientaion();
				
			}
		} , 5000);
	}
	private Timer mTimer;
	private void  resumePreviousOrientaion(){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(mPlayerState == PLAYER_PLAY){
					((MainBaseOptions)mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}else{
					((MainBaseOptions)mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
		});
	}
	@Override
	public void onFullScreen(boolean value) {
		isFullScreen = value;
		if (mContext.getResources().getBoolean(R.bool.isTablet)) {
			if (value) {
				playInLandscape();
			} else {
				playInPortrait();
			}
		} else {
			if(getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
				((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				resumePreviousOrientaionTimer();
			}
			else {
				((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				resumePreviousOrientaionTimer();
			} 
		}
	}
	public int getScreenOrientation(){
		Display getOrient = ((Activity) mContext).getWindowManager().getDefaultDisplay();
		if(getOrient.getWidth() < getOrient.getHeight()){
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; 
		}
		return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		
	}
	public void playInLandscape() {
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play in lanscape :: ");
		}
		if(mVideoViewPlayer != null){
			mVideoViewPlayer.playerInFullScreen(true);
		}
		int statusBarHeight = Util.getStatusBarHeight(mContext);

		int derviedWidth = myplexapplication.getApplicationConfig().screenWidth;
		int derviedHeight = myplexapplication.getApplicationConfig().screenHeight;
		int modifiedWidth=derviedWidth;
		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			derviedWidth = myplexapplication.getApplicationConfig().screenHeight;
			derviedHeight = myplexapplication.getApplicationConfig().screenWidth - statusBarHeight;
			// only for live
			modifiedWidth=derviedWidth;
			if(mData.generalInfo.type.equalsIgnoreCase("live"))
				modifiedWidth = (derviedHeight * 4)/3;
		}
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			derviedHeight = myplexapplication.getApplicationConfig().screenHeight - statusBarHeight;
			LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(derviedWidth,derviedHeight);
			mParentLayout.setLayoutParams(layoutparams);
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(derviedWidth,derviedHeight);
		RelativeLayout.LayoutParams parent = new RelativeLayout.LayoutParams(derviedWidth,derviedHeight);
		// only for live
		if(mData.generalInfo.type.equalsIgnoreCase("live"))
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mVideoViewParent.setLayoutParams(parent);
		mVideoViewParent.setEnabled(false);
		mVideoView.setLayoutParams(params);
		mVideoView.resizeVideo(modifiedWidth,derviedHeight);
		((MainBaseOptions) mContext).hideActionBar();
		if(mPlayerFullScreen != null){
			mPlayerFullScreen.playerInFullScreen(true);
		}
		// mParentLayout.setLayoutParams(mParentLayoutParams);
		mParentLayout.setBackgroundColor(Color.BLACK);
	}
	public void playInPortrait() {
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play in portrait :: ");
		}
		if(mVideoViewPlayer != null){
			mVideoViewPlayer.playerInFullScreen(false);
		}
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			mWidth = myplexapplication.getApplicationConfig().screenWidth;
			mWidth = (myplexapplication.getApplicationConfig().screenWidth/3)*2;
			int marginleft = (int)mContext.getResources().getDimension(R.dimen.margin_gap_12);
			mWidth -= marginleft*2;
			mHeight = (mWidth * 9)/16;

			LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(mWidth,mHeight);
			layoutparams.setMargins(marginleft, marginleft, marginleft, marginleft);
			mParentLayout.setLayoutParams(layoutparams);
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mWidth, mHeight);
		mVideoViewParent.setLayoutParams(params);
		mVideoView.setLayoutParams(params);
		mVideoView.resizeVideo(mWidth, mHeight);
		((MainBaseOptions) mContext).showActionBar();
		if(mPlayerFullScreen != null){
			mPlayerFullScreen.playerInFullScreen(false);
		}
		// mParentLayout.setLayoutParams(params);
	}
	@Override
	public void onStateChanged(int state , int elapsedTime) 
	{
		this.state = PlayerListener.STATE_PAUSED;
		currentDuration = elapsedTime;
		switch (state) {
		case PlayerListener.STATE_PAUSED:
			if(isLocalPlayback){
				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
			}
			Log.d(TAG, "paused" + elapsedTime);
			break;
		case PlayerListener.STATE_PLAYING:
			Log.d(TAG, "playing" + elapsedTime);
			break;
		case PlayerListener.STATE_STOP:
			if(isLocalPlayback){
				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
			}
			Log.d(TAG, "stop");
			break;
		case PlayerListener.STATE_RESUME:
			Log.d(TAG, "resumes");
			break;
		case PlayerListener.STATE_STARTED:
			Log.d(TAG, "started");
			break;
		case PlayerListener.STATE_COMPLETED:
			Log.d(TAG,"completed");
			if(isLocalPlayback){
				SharedPrefUtils.writeToSharedPref(mContext, mData._id, elapsedTime);
			}
			break;
		}
		MediaUtil.savePlayerState(mData._id, state, elapsedTime);

		
	}
	
	
	public int getStopPosition(){
		return (mVideoView.getCurrentPosition()/1000);
	}
	public boolean isMediaPlaying(){		
		if(mVideoView == null){
			return false;
		}
		if(mVideoView.getCurrentPosition() == 0)
			return false;
		else
			return true;

	}
	@Override
	public void onDialogOption2Click() 
	{
		
		LogOutUtil.onClickLogout(mContext);
	}
	@Override
	public void onDialogOption1Click() {
		
	}

	/**
	 * @param is a movie Only to show the alert message
	 * @return can able to play 
	 */
	public boolean canBePlayed(boolean isMovie) {
		// Before playing any video we have to check whether user has logged In
		// or not.
		String email = myplexapplication.getUserProfileInstance()
				.getUserEmail();
		if (email.equalsIgnoreCase("NA") || email.equalsIgnoreCase("")) {
			AlertDialogUtil.showAlert(mContext, mContext.getResources()
					.getString(R.string.must_logged_in), mContext
					.getResources().getString(R.string.continiue_as_guest),
					mContext.getResources().getString(R.string.login_to_play),
					CardVideoPlayer.this);
			return false;
		}
		
		if (myplexapplication.mDownloadList != null && mData != null) {
			
			CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList
					.get(mData._id);
			if (mDownloadData != null) {
				return true;
			}
		}
		
		if(!Util.isNetworkAvailable(mContext)){
			Util.showToast(mContext, mContext.getString(R.string.error_network_not_available), Util.TOAST_TYPE_ERROR);								
			return false;
		}	
		
		if(mData.generalInfo.type.equalsIgnoreCase("live"))	
			return true;
		if(mData.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_TV_SEASON) || mData.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_TV_SERIES))
			return false;

		String networkInfo = Util.getInternetConnectivity(mContext);
		if (networkInfo.equalsIgnoreCase("2G")) {
			Util.showToast(
					mContext.getResources().getString(
							R.string.error_message_2g_videoplay), mContext);
			return false;
		} else if (networkInfo.equalsIgnoreCase("3G")) {
			if (isMovie)
				Util.showToast(
						mContext.getResources().getString(
								R.string.alert_message_3g_movie), mContext);
			else
				Util.showToast(
						mContext.getResources().getString(
								R.string.alert_message_3g_trailer), mContext);
			return true;
		}

		// It is a wifi
		return true;
	}

	private OnClickListener mScoreCardClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if(!Util.isNetworkAvailable(mContext)){
				Util.showToast(mContext, "No Network connection",Util.TOAST_TYPE_ERROR);
				return;
			}			
			
			if(mData.matchInfo == null || TextUtils.isEmpty(mData.matchInfo.matchMobileUrl )){
				Util.showToast(mContext, "Not Available",Util.TOAST_TYPE_ERROR);
				return;
			}
			Intent i = new Intent(mContext,LiveScoreWebView.class);
			Bundle b = new Bundle();
			b.putString("url", mData.matchInfo.matchMobileUrl );
			b.putBoolean("isProgressDialogCancelable", true);
			i.putExtras(b);	
			((Activity) mContext).startActivityForResult(i, ConsumerApi.SUBSCRIPTIONREQUEST);
			
		}
	};
	
	private void initSportsStatusLayout(final View view){
		if(mData.generalInfo != null && mData.generalInfo.type != null && mData.generalInfo.type.equalsIgnoreCase(ConsumerApi.CONTENT_SPORTS_LIVE)){
			mTrailerButton.setVisibility(View.GONE);
			mScoreCardLayout = (LinearLayout)view.findViewById(R.id.cardmedia_scorecard_layout);
			mScoreCardLayout.setVisibility(View.VISIBLE);
			mScoreCardLayout.setOnClickListener(mScoreCardClickListener);
			mTrailerAvailable=false;
			
			
			OnResponseListener onResponseListener = new OnResponseListener() {

				@Override
				public void response(boolean status, MatchStatus matchStatus) {

					if (!status) {
						return;
					}
					
					if(!TextUtils.equals(MatchStatus.STATUS_LIVE, matchStatus.status)){
						stopSportsStatusRefresh();
					}
					
					TextView textView1 = (TextView) view
							.findViewById(R.id.cardmedia_scorecard_textLine1);
					TextView textView2 = (TextView) view
							.findViewById(R.id.cardmedia_scorecard_textLine2);
					TextView textView3 = (TextView) view
							.findViewById(R.id.cardmedia_scorecard_textLine3);
					
					ValueAnimator fadeAnim2 = ObjectAnimator.ofFloat(mScoreCardLayout,
							"alpha", 0f, 1f);
					fadeAnim2.setDuration(800);
					fadeAnim2.start();
					
					if(matchStatus.matchType == MATCH_TYPE.FIFA){
						
						Team team1= matchStatus.teams.get(0);
						Team team2= matchStatus.teams.get(1);
						
						
						if(team1.validate() && team2.validate()){

							String text = "("+team1.score+") "+ team1.sname + " " + "vs"							
									+" "+team2.sname + " ("+team2.score+")";
							textView1.setText(text);
						}
						
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView2.setVisibility(View.VISIBLE);
							textView2.setText(matchStatus.statusDescription);	
							textView2.setSelected(true);
						}
						
						return;
					}
					
					if(!TextUtils.equals(MatchStatus.STATUS_LIVE, matchStatus.status) || 
							matchStatus.teams == null || 
							matchStatus.teams.isEmpty()){

						if(!TextUtils.isEmpty(matchStatus.matchTitle)){
							textView1.setText(matchStatus.matchTitle);
						}
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView2.setText(matchStatus.statusDescription);
							textView2.setSelected(true);
						}
						return;
					}
					
					if(matchStatus.teams.size() ==1){
						
						Team team= matchStatus.teams.get(0);
						
						if(team.validate()){
							textView1.setText(team.sname +" " + team.score);						
						}
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView2.setText(matchStatus.statusDescription);
						}
						
						return;
					}
					
					if(matchStatus.teams.size() == 2 ){
						
						Team team1= matchStatus.teams.get(0);
						Team team2= matchStatus.teams.get(1);
						if(team1.validate()){
							textView1.setText(team1.sname +" " + team1.score);
						}
						if(team2.validate()){
							textView2.setText(team2.sname +" " + team2.score);						
						}
						if(!TextUtils.isEmpty(matchStatus.statusDescription)){
							textView3.setVisibility(View.VISIBLE);
							textView3.setText(matchStatus.statusDescription);	
							textView3.setSelected(true);
						}
					}
					

				}

			
			};
			
			sportsStatusRefresh = new SportsStatusRefresh(mData._id, onResponseListener);
			sportsStatusRefresh.start();
		}
	}
	
	
	public void stopSportsStatusRefresh(){
		if(sportsStatusRefresh != null){
			sportsStatusRefresh.stop();
		}
	}
	
	@Override
	public void onUrlFetched(List<CardDataVideosItem> items) 
	{
		String videoType = mData.generalInfo.type;		
		Log.d(TAG,"Video type "+ videoType);
		
//		initPlayBack("https://myplexv2betadrmstreaming.s3.amazonaws.com/813/813_sd_est_1391082325821.wvm");
		if(videoType.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_MOVIE)){		
			chooseStreamOrDownload(items);
		}else if(videoType.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE)){
			chooseLiveStreamType(items,false);			
		}else if(videoType.equalsIgnoreCase(ConsumerApi.TYPE_TV_EPISODE)){
			chooseStreamOrDownload(items);
		}else if(videoType.equalsIgnoreCase(ConsumerApi.CONTENT_SPORTS_LIVE)){
			chooseLiveStreamType(items,false);	
		}else if(videoType.equalsIgnoreCase(ConsumerApi.CONTENT_SPORTS_VOD)){
			chooseLiveStreamType(items,true);	
		}
		
	}
	
	/*@Override
	public void onUrlFetched(List<CardDataVideosItem> items) 
	{
		String videoType = mData.generalInfo.type;		
		Log.d(TAG,"Video type "+ videoType);
		
		initPlayBack("https://myplexv2betadrmstreaming.s3.amazonaws.com/813/813_sd_est_1391082325821.wvm");
		if(videoType.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_MOVIE)){		
			chooseStreamOrDownload(items);
		}else if(videoType.equalsIgnoreCase(ConsumerApi.VIDEO_TYPE_LIVE)){
			chooseLiveStreamType(items,false);			
		}
		
	}*/
	@Override
	public void onTrailerUrlFetched(List<CardDataVideosItem> videos) {
		chooseLiveStreamType(videos,true);
		
	}
	
	private String getLink(Map<String, String> pMap, String firstPref, String secondPref,String ...profile)
	{		
		for(String string : profile){
			if(pMap.get(string+firstPref)!=null)
				return pMap.get(string+firstPref);
			else if(pMap.get(string+secondPref)!=null)
				return pMap.get(string+secondPref);				
			
		}
		return "";
		
	}
	
	private void chooseLiveStreamType(List<CardDataVideosItem> items,boolean isTrailer) 
	{
		HashMap<String, String> profileMap = new HashMap<String, String>();
		
		for(CardDataVideosItem item : items){
			if(item.profile.equalsIgnoreCase(ConsumerApi.STREAMADAPTIVE)){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHLS))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYVERYHIGH)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATRTSP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHTTP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHLS))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYHIGH)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATRTSP))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYLOW)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHLS))){
				profileMap.put(item.profile+item.format, item.link);
			}else if((item.profile.equalsIgnoreCase(ConsumerApi.VIDEOQUALTYLOW)) && (item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATRTSP))){
				profileMap.put(item.profile+item.format, item.link);
			}
		}
			
		if (android.os.Build.VERSION.SDK_INT >= 19) {
				initPlayBack(getLink(profileMap, ConsumerApi.STREAMADAPTIVE,
						ConsumerApi.STREAMINGFORMATHLS,
						new String[]{ConsumerApi.STREAMADAPTIVE,ConsumerApi.VIDEOQUALTYVERYHIGH,
							ConsumerApi.VIDEOQUALTYHIGH,ConsumerApi.VIDEOQUALTYLOW}));
		}
		
		String mConnectivity  = Util.getInternetConnectivity(mContext);
		if(isTrailer){
			if(mConnectivity.equalsIgnoreCase("wifi")){
				initPlayBack(getLink(profileMap, ConsumerApi.STREAMINGFORMATHTTP,
						ConsumerApi.STREAMINGFORMATRTSP,
						new String[]{ConsumerApi.VIDEOQUALTYHIGH,ConsumerApi.VIDEOQUALTYLOW}));
			}else if(mConnectivity.equalsIgnoreCase("3G")){
				initPlayBack(getLink(profileMap, ConsumerApi.STREAMINGFORMATHTTP,
						ConsumerApi.STREAMINGFORMATRTSP,
						new String[]{ConsumerApi.VIDEOQUALTYHIGH,ConsumerApi.VIDEOQUALTYLOW}));
			}else if(mConnectivity.equalsIgnoreCase("2G")){
				initPlayBack(getLink(profileMap, ConsumerApi.STREAMINGFORMATHTTP,ConsumerApi.STREAMINGFORMATRTSP,
						new String[]{ConsumerApi.VIDEOQUALTYLOW}));
			}else{
				Toast.makeText(mContext,mContext.getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
			}
			return;
		}
		
		if(mConnectivity.equalsIgnoreCase("wifi")){
			initPlayBack(getLink(profileMap, ConsumerApi.STREAMINGFORMATHLS,
					ConsumerApi.STREAMINGFORMATRTSP,
					new String[]{ConsumerApi.VIDEOQUALTYVERYHIGH,ConsumerApi.VIDEOQUALTYHIGH,ConsumerApi.VIDEOQUALTYLOW}));
		}else if(mConnectivity.equalsIgnoreCase("3G")){
			initPlayBack(getLink(profileMap, ConsumerApi.STREAMINGFORMATHLS,
					ConsumerApi.STREAMINGFORMATRTSP,
					new String[]{ConsumerApi.VIDEOQUALTYHIGH,ConsumerApi.VIDEOQUALTYLOW}));
		}else if(mConnectivity.equalsIgnoreCase("2G")){
			initPlayBack(getLink(profileMap, ConsumerApi.STREAMINGFORMATHLS,ConsumerApi.STREAMINGFORMATRTSP,
					new String[]{ConsumerApi.VIDEOQUALTYLOW}));
		}else{
			Toast.makeText(mContext,mContext.getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
		}
		
		
	}	
	private void chooseStreamOrDownload(List<CardDataVideosItem> items) {
		CardDataVideosItem adaptive = new CardDataVideosItem();
		CardDataVideosItem download = new CardDataVideosItem();	
		for(CardDataVideosItem item : items){
			if(item.type!=null && item.type.equalsIgnoreCase("adaptive")){
				if((item.link!=null && item.link.length()>0)&&(item.format!=null 
										&& item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHTTP)))				
					adaptive = item;
			}else if(item.type.equalsIgnoreCase("download")){
				if((item.link!=null && item.link.length()>0)&&(item.format!=null &&
										item.format.equalsIgnoreCase(ConsumerApi.STREAMINGFORMATHTTP)))		
					download = item;	
			}
		}
		if(download!=null)
			 download_link = download.link;		
		if(adaptive!=null){			
			adaptive_link = adaptive.link;
		}
		final int ellapseTime = adaptive.elapsedTime;
		if(SharedPrefUtils.getBoolFromSharedPreference(mContext, mContext.getString(R.string.is_dont_ask_again))){
			if(SharedPrefUtils.getBoolFromSharedPreference(mContext, mContext.getString(R.string.isDownload))){
				if(download_link!=null)					
					initPlayBack(download_link);
				else if(adaptive_link!=null){                                        			
					initPlayBack(adaptive_link);			
					onLastPausedTimeFetched(adaptive.elapsedTime);			
				}
			}else{
				 Util.showToast(mContext, mContext.getString(R.string.switch_to_download_in_setting_msg), Util.TOAST_TYPE_INFO);
				 if(adaptive_link!=null)			
					 initPlayBack(adaptive_link);
				else if(download_link!=null)			
					initPlayBack(download_link);
			}
		}else if(download_link!=null && adaptive_link!=null){
				DownloadStreamDialog dialog = new DownloadStreamDialog(mContext,mData.generalInfo.title+" rental options");
				dialog.setListener(new DownloadListener() {			
					@Override
					public void onOptionSelected(boolean isDownload) {
						if(isDownload){
							initPlayBack(download_link);
						}else{
							initPlayBack(adaptive_link);
							onLastPausedTimeFetched(ellapseTime);
						}					
					}
				});
				dialog.showDialog();	
				return;
		}else if(adaptive_link!=null){
			initPlayBack(adaptive_link);
			onLastPausedTimeFetched(adaptive.elapsedTime);	
			return;
		}else if(download_link!=null){
			initPlayBack(download_link);
			return;
		}
	}
	@Override
	public void onUrlFetchFailed(String message) 
	{
		closePlayer();
		if(message != null && message.equalsIgnoreCase("ERR_USER_NOT_SUBSCRIBED")){
			if(mData.generalInfo.type.equalsIgnoreCase(ConsumerApi.TYPE_TV_EPISODE)){
				if(mPlayerStatusListener!=null)
					mPlayerStatusListener.playerStatusUpdate("ERR_USER_NOT_SUBSCRIBED");
				return;
			}
			PackagePopUp popup = new PackagePopUp(mContext,(View)mParentLayout.getParent());
			myplexapplication.getCardExplorerData().cardDataToSubscribe =  mData;
			popup.showPackDialog(mData, ((Activity)mContext).getActionBar().getCustomView());	
			
		}else
			Util.showToast(mContext,message,Util.TOAST_TYPE_INFO);
		
	}
	
	private void setBitrateForTrailer(String url) {
		if(url == null) return;
		String bitrateTrailer = null;
		if(Analytics.isTrailer) {
			if(url.contains("high")) bitrateTrailer = "high";
			if(url.contains("low")) bitrateTrailer = "low";
			if(url.contains("veryhigh")) bitrateTrailer = "veryhigh";
			if(url.contains("medium")) bitrateTrailer = "medium";
		}
		else {
			if(url.contains("vhigh")) bitrateTrailer = "vhigh";
			if(url.contains("low")) bitrateTrailer = "low";
			if(url.contains("medium")) bitrateTrailer = "medium";
		}
		if(bitrateTrailer != null) {
			if(mData != null && mData.generalInfo != null) {
				String cardId = mData.generalInfo._id;
				String key = Analytics.TRAILER_BITRATE+cardId;
				SharedPrefUtils.writeToSharedPref(myplexapplication.getAppContext(), key, bitrateTrailer);
			}			
		}
	}
	public void initPlayBack(String url){
		Log.d(TAG,"Got the link for playback = "+url);
		if (url == null) {
			closePlayer();
			if(mPlayerStatusListener != null){
				mPlayerStatusListener.playerStatusUpdate("No url to play.");
			}
			Util.showToast(mContext, "No url to play.",Util.TOAST_TYPE_ERROR);
			return;
		}
		
		if(isESTPackPurchased || url.contains("_est_"))
		{
			url=url.replace("widevine:", "http:");			
			closePlayer();
			if(Util.getSpaceAvailable()>=1)
			{
				if(Util.isWifiEnabled(mContext))
				{
					/*if(ApplicationSettings.ENABLE_SHOW_PLAYER_LOGS_SETTINGS){
                        if(mData._id != null && mData._id.equalsIgnoreCase("413")){
                                url="http://192.168.60.36/myplex/413_sd_est_1388644246475.wvm";
                        }else if(mData._id != null && mData._id.equalsIgnoreCase("415")){
                                url="http://192.168.60.36/myplex/415_sd_est_1388645176954.wvm";
                        }else if(mData._id != null && mData._id.equalsIgnoreCase("446")){
                                url="http://192.168.60.36/myplex/446_sd_est_1386786732268.wvm";
                        }
                }*/
					Util.startDownload(url, mData, mContext);
				}
				else
				{
					Util.showToast(mContext, "Downloading is supported only on Wifi, please turn on wifi and try again.", Util.TOAST_TYPE_INFO);
				}
			}
			else
			{
				Util.showToast(mContext, "Download failed due to insufficent memory, please free space up to 1GB to start download", Util.TOAST_TYPE_INFO);
			}
			return;
		}				
		else{
			drmLicenseType="st";
		}

		if(mData.content !=null && mData.content.drmEnabled)
		{
			String licenseData="clientkey:"+myplexapplication.getDevDetailsInstance().getClientKey()+",contentid:"+mData._id+",type:"+drmLicenseType+",profile:0";

			byte[] data;
			try {
				data = licenseData.getBytes("UTF-8");
				String base64 = Base64.encodeToString(data, Base64.DEFAULT);
				Settings.USER_DATA=base64;
				Settings.DEVICE_ID=myplexapplication.getDevDetailsInstance().getClientDeviceId();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	
		}
		if(!lastWatchedStatus)
			myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);
		Util.showAdultToast(mContext.getString(R.string.adultwarning), mData, mContext);
		Uri uri ;	
		uri = Uri.parse(url);
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Playing :: "+url);
		}
		setBitrateForTrailer(url);//url not uri
		initializeVideoPlay(uri);
	}
	
	public boolean isFullScreen() {	
		   return isFullScreen;			
		}
	public void setFullScreen(boolean isFullScreen) {			
		this.isFullScreen = isFullScreen;			
		}        			
	public void onLastPausedTimeFetched(int ellapseTime) {			
		if(ellapseTime > 60){			
			if(mVideoViewPlayer ==null){                                                			
					mVideoViewPlayer = new  VideoViewPlayer(mVideoView, mContext,null ,StreamType.VOD);			
				}			
			mVideoViewPlayer.setmPositionWhenPaused(ellapseTime*1000);			
			}			
	}
	OnLicenseExpiry onLicenseExpiryListener = new VideoViewPlayer.OnLicenseExpiry() {
		
		@Override
		public void licenseExpired() {
			
			if(mContext == null || ! (mContext instanceof Activity)){
				return;
			}
			
			((Activity)mContext).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Util.showToast(mContext,"License Expired.",Util.TOAST_TYPE_INFO);
					PackagePopUp popup = new PackagePopUp(mContext,(View)mParentLayout.getParent());
					myplexapplication.getCardExplorerData().cardDataToSubscribe =  mData;
					popup.showPackDialog(mData, ((Activity)mContext).getActionBar().getCustomView());	
					
				}
			});			
		}
	};
	
	public void updateCardPreviewImage(CardData data){
		if(data == null || data.images==null || data.images.values==null)
			return;
		mData = data;
		if(mData.relatedMultimedia==null 
				|| mData.relatedMultimedia.values==null 
					|| mData.relatedMultimedia.values.size()==0){
			mTrailerAvailable = false;
		}else{
			for (CardDataRelatedMultimediaItem mmItem : mData.relatedMultimedia.values) {
				{
					if(mmItem.content !=null && mmItem.content.categoryName !=null 
							&& mmItem.content.categoryName.equalsIgnoreCase("trailer") 
								&& mmItem.generalInfo !=null && mmItem.generalInfo._id !=null)
					{
						mTrailerAvailable = true;
						break;
					}
				}
			}
		}
		if(isMediaPlaying())
			return;
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		for(CardDataImagesItem imageItem : mData.images.values) {
			mPreviewImage.setImageUrl(imageItem.link,MyVolley.getImageLoader());
		}
	}
	
	public void createRecordPlayView(String url,String programmName){
		final String urlString = url;
		mTrailerButton.setVisibility(View.VISIBLE);
		mTrailerButton.setText(mContext.getString(R.string.record_play));
		mTrailerButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {	
				mVideoViewParent.setOnClickListener(null);
				mProgressBarLayout.setVisibility(View.VISIBLE);	
				mPreviewImage.setVisibility(View.INVISIBLE);
				mTrailerButton.setVisibility(View.INVISIBLE);
				mPlayButton.setVisibility(View.INVISIBLE);
//				mPlayButton.setOnClickListener(null);
//				mVideoViewParent.setEnabled(false);
				recordedProgName.setVisibility(View.INVISIBLE);
				initPlayBack(urlString);
				
			}
		});
		recordedProgName.setVisibility(View.VISIBLE);
		recordedProgName.setText(programmName);
		recordedProgName.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mTrailerButton.performClick();
			}
		});
	}
	public void removeRecordPay(){
		closePlayer();
		mTrailerButton.setVisibility(View.GONE);
		recordedProgName.setVisibility(View.GONE);
		mPlayButton.setVisibility(View.VISIBLE);		
	}
	public void removeProgrammeName(){
		if(mPreviewImage!=null)
			mPreviewImage.setVisibility(View.INVISIBLE);	
		if(recordedProgName!=null)
			recordedProgName.setVisibility(View.GONE);
		if(mTrailerButton!=null)
			mTrailerButton.setVisibility(View.GONE);
		if(mPlayButton!=null)
			mPlayButton.setVisibility(View.GONE);
		
	}
	public boolean getTrailer(){
		return isTriler;
	}
}
