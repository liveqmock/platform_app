package com.apalya.myplex.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.MainActivity;
import com.apalya.myplex.MainBaseOptions;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataImagesItem;
import com.apalya.myplex.data.CardDataPurchaseItem;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.media.VideoViewExtn;
import com.apalya.myplex.media.VideoViewPlayer;
import com.apalya.myplex.media.VideoViewPlayer.StreamType;
import com.apalya.myplex.utils.MediaUtil;
import com.apalya.myplex.utils.MediaUtil.MediaUtilEventListener;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;

public class CardVideoPlayer implements PlayerListener {
	private Context mContext;
	private LayoutInflater mInflator;
	private View mParentLayout;
	private LayoutParams mParentLayoutParams;
	private FadeInNetworkImageView mPreviewImage;
	private ImageView mPlayButton;
	private TextView mBufferPercentage;
	private RelativeLayout mProgressBarLayout;
	private RelativeLayout mVideoViewParent;
	private VideoViewExtn mVideoView;
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
		mPlayButton = (ImageView) v
				.findViewById(R.id.cardmediasubitemvideo_play);
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
				if (imageItem.profile != null
						&& imageItem.profile.equalsIgnoreCase("xxhdpi")) {
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
			mVideoView.setVisibility(View.INVISIBLE);
			mProgressBarLayout.setVisibility(View.INVISIBLE);
			mPreviewImage.setScaleType(ScaleType.CENTER);
			mPreviewImage.setBackgroundColor(Color.BLACK);
		}else{
			  Util.showFeedback(mVideoViewParent);
		}
		return v;
	}

	private OnClickListener mPlayerClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			Map<String,String> params=new HashMap<String, String>();
			params.put("CardId", mData._id);
			params.put("CardType", mData.generalInfo.type);
			params.put("CardName", mData.generalInfo.title);
			Analytics.trackEvent(Analytics.PlayerPlaySelect,params);
			
			FetchUrl();
			mVideoViewParent.setOnClickListener(null);
			// TODO Auto-generated method stub

		}
	};

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
		mProgressBarLayout.setVisibility(View.GONE);
		mPreviewImage.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		Map<String,String> params=new HashMap<String, String>();
		params.put("CardId", mData._id);
		params.put("CardType", mData.generalInfo.type);
		params.put("CardName", mData.generalInfo.title);
		Analytics.trackEvent(Analytics.PlayerPlayComplete,params);
		
	}

	public void FetchUrl() {
		mPlayButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPreviewImage.setVisibility(View.INVISIBLE);
		MediaUtil.setUrlEventListener(new MediaUtilEventListener() {

			@Override
			public void urlReceived(boolean aStatus, String url) {
				if (!aStatus) {
					closePlayer();
					Util.showToast(mContext, "Failed in fetching the url.",Util.TOAST_TYPE_ERROR);
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Failed in fetching the url.");
					}
//					Toast.makeText(mContext, "Failed in fetching the url.",
//							Toast.LENGTH_SHORT).show();
					return;
				}
				if (url == null) {
					closePlayer();
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("No url to play.");
					}
					Util.showToast(mContext, "No url to play.",Util.TOAST_TYPE_ERROR);
//					Toast.makeText(mContext, "No url to play.",
//							Toast.LENGTH_SHORT).show();
					return;
				}
				if(isESTPackPurchased)
				{
					closePlayer();
					
					if(Util.checkDownloadStatus( mData._id, mContext)==0)
					{
						long id=Util.startDownload(url, mData.generalInfo.title, mContext);
						myplexapplication.getUserProfileInstance().downloadMap.put(mData._id, id);
					}
					else
					{
						Util.showToast(mContext, "Your download is in progress,Please check your status in Downloads section.",Util.TOAST_TYPE_ERROR);
//						Util.showToast("Your download is in progress,Please check your status in Downloads section", mContext);
					}
					return;
					
				}
				else
				{
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
					mVideoViewPlayer.setUri(uri, streamType);
				} else {
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
				mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
			}
			}
		});

		boolean lastWatchedStatus=false;
		for(CardData data:myplexapplication.getUserProfileInstance().lastVisitedCardData)
		{
			if(data._id.equalsIgnoreCase(mData._id))
			{
				lastWatchedStatus=true;
			}
		}
		if(!lastWatchedStatus)
			myplexapplication.getUserProfileInstance().lastVisitedCardData.add(mData);

		
		if(mData.currentUserData!=null)
        {    
            for(CardDataPurchaseItem data:mData.currentUserData.purchase)
            {
                if(data.type.equalsIgnoreCase("download") || data.type.equalsIgnoreCase("est")){
                    isESTPackPurchased=true;

                }
            }
        }
        
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
        }
        MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased);
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
		mPlayButton = (ImageView) v.findViewById(R.id.cardmediasubitemvideo_play);
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
				if (imageItem.profile != null
						&& imageItem.profile.equalsIgnoreCase("xxhdpi")) {
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
			mVideoViewPlayer.deregisteronBufferingUpdate();
			mProgressBarLayout.setVisibility(View.GONE);
			mVideoViewPlayer.showMediaController();
			mPlayerState = PLAYER_PLAY;
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
			mPlayerStatusListener.playerStatusUpdate("Play Info :: what = "+what+" extra= "+extra);
		}
		return false;
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play complete :: ");
		}
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
		if(!mContext.getResources().getBoolean(R.bool.isTablet)){
			derviedWidth = myplexapplication.getApplicationConfig().screenHeight;
			derviedHeight = myplexapplication.getApplicationConfig().screenWidth - statusBarHeight;
		}
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(derviedWidth,derviedHeight);
			mParentLayout.setLayoutParams(layoutparams);
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(derviedWidth,derviedHeight);
		mVideoViewParent.setLayoutParams(params);
		mVideoView.setLayoutParams(params);
		mVideoView.resizeVideo(derviedWidth,derviedHeight);
		((MainBaseOptions) mContext).hideActionBar();
		if(mPlayerFullScreen != null){
			mPlayerFullScreen.playerInFullScreen(true);
		}
		// mParentLayout.setLayoutParams(mParentLayoutParams);
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
	
}
