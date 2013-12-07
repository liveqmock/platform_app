package com.apalya.myplex.views;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
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
import com.apalya.myplex.data.CardDataRelatedMultimediaItem;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardDownloadedDataList;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.media.VideoViewExtn;
import com.apalya.myplex.media.VideoViewPlayer;
import com.apalya.myplex.media.VideoViewPlayer.StreamType;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MediaUtil;
import com.apalya.myplex.utils.MediaUtil.MediaUtilEventListener;
import com.apalya.myplex.utils.WidevineDrm.Settings;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.WidevineDrm;

public class CardVideoPlayer implements PlayerListener {
	private Context mContext;
	private LayoutInflater mInflator;
	private View mParentLayout;
	private LayoutParams mParentLayoutParams;
	private FadeInNetworkImageView mPreviewImage;
	private TextView mPlayButton;
	private TextView mTrailerButton;
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
	private String drmLicenseType="st";
	
	private String mVideoUrl;
	private Uri mVideoUri ;
	private boolean mTrailerAvailable = false;
	
	private static final String TAG = "CardVideoPlayer";
	

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
		
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
		
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
										Map<String,String> params=new HashMap<String, String>();
										params.put("CardId", mmItem.generalInfo._id);
										params.put("CardCategory", mmItem.content.categoryName);
										Analytics.trackEvent(Analytics.PlayerPlaySelect,params);
										FetchTrailerUrl(mmItem.generalInfo._id);
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
						&& imageItem.profile.equalsIgnoreCase(myplexapplication.getApplicationConfig().type)) {
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
		mTrailerButton.setVisibility(mTrailerAvailable == true ? View.VISIBLE : View.GONE);
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
		mTrailerButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPreviewImage.setVisibility(View.INVISIBLE);
		MediaUtil.setUrlEventListener(new MediaUtilEventListener() {

			@Override
			public void urlReceived(boolean aStatus, String url, String message) {
				if (!aStatus) {
					closePlayer();
					
					String msg ="Failed in fetching the url.";
					
					if(!TextUtils.isEmpty(message)){
						msg = message;
					}
					
					Util.showToast(mContext, msg,Util.TOAST_TYPE_ERROR);					
					
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
				/*if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Url Received :: "+url);
				}*/
				if(isESTPackPurchased || url.contains("_est_"))
				{
					url=url.replace("widevine:", "http:");
					closePlayer();
					if(Util.getSpaceAvailable()>=1)
					{
						if(Util.isWifiEnabled(mContext))
						{
							Util.startDownload(url, mData, mContext);
						}
						else
						{
							Util.showToast(mContext, "Downloading is supported only on Wifi, please turn of wifi and try again.", Util.TOAST_TYPE_INFO);
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
	        }
			
	        if(myplexapplication.mDownloadList != null){
	        	if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Download Details are available...");
				}
				CardDownloadData mDownloadData = myplexapplication.mDownloadList.mDownloadedList.get(mData._id);
				if(mDownloadData!=null){
					if(mDownloadData.mCompleted && Util.isFileExist(mData._id+".wvm"))
					{
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
							MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased);
						}
					}
				}
				else{
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Download Details for this content not available, so requesting url...");
					}
					MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased);
				}
			}
	        else 
	        {
	        	if(mPlayerStatusListener != null){
					mPlayerStatusListener.playerStatusUpdate("Download Details not available, so requesting url...");
				}
	        	MediaUtil.getVideoUrl(mData._id,qualityType,streamingType,isESTPackPurchased);	
	        }
	      }
	}
private void playVideoFile(CardDownloadData mDownloadData){

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
	private void FetchTrailerUrl(String contentId)
	{
		mPlayButton.setVisibility(View.INVISIBLE);
		mTrailerButton.setVisibility(View.INVISIBLE);
		mProgressBarLayout.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.VISIBLE);
		mPreviewImage.setVisibility(View.INVISIBLE);
		
        String qualityType = new String();
        String streamingType = new String();
        
        streamingType = ConsumerApi.STREAMNORMAL;
    	qualityType = ConsumerApi.VIDEOQUALTYLOW;
        
        if(Util.isWifiEnabled(mContext))
        {
    		qualityType= ConsumerApi.VIDEOQUALTYHIGH;
        }
        MediaUtil.setUrlEventListener(new MediaUtilEventListener() {
			
			@Override
			public void urlReceived(boolean aStatus, String url, String message) {
				if (!aStatus) {
					closePlayer();
					Util.showToast(mContext, "Failed in fetching the url.",Util.TOAST_TYPE_ERROR);
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("Failed in fetching the url.");
					}
					return;
				}
				if (url == null) {
					closePlayer();
					if(mPlayerStatusListener != null){
						mPlayerStatusListener.playerStatusUpdate("No url to play.");
					}
					Util.showToast(mContext, "No url to play.",Util.TOAST_TYPE_ERROR);
					return;
				}
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
		});
        MediaUtil.getVideoUrl(contentId,qualityType,streamingType,isESTPackPurchased);
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
								Map<String,String> params=new HashMap<String, String>();
								params.put("CardId", mmItem.generalInfo._id);
								params.put("CardCategory", mmItem.content.categoryName);
								Analytics.trackEvent(Analytics.PlayerPlaySelect,params);
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
