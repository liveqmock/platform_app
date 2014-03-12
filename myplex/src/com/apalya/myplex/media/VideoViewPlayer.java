package com.apalya.myplex.media;



import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.LogMergePolicy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.drm.DrmErrorEvent;
import android.drm.DrmEvent;
import android.drm.DrmInfoEvent;
import android.drm.DrmInfoRequest;
import android.drm.DrmManagerClient;
import android.drm.DrmRights;
import android.drm.DrmStore;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.ErrorManagerData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.exception.DRMException;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.WidevineDrm;
import com.apalya.myplex.utils.WidevineDrm.Settings;
import com.apalya.myplex.views.CardVideoPlayer;
import com.apalya.myplex.views.CardVideoPlayer.PlayerFullScreen;
import com.apalya.myplex.views.CardVideoPlayer.PlayerStatusUpdate;
import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
public class VideoViewPlayer implements MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,
		MediaPlayer.OnCompletionListener, OnPreparedListener,
		OnSeekCompleteListener, OnBufferingUpdateListener {


	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	private static final int STATE_SUSPEND = 6;
	private static final int STATE_RESUME = 7;
	private static final int STATE_SUSPEND_UNSUPPORTED = 8;

	private static final String TAG = "VideoViewPlayer";
	private int mCurrentState = STATE_IDLE;

	private VideoView mVideoView;

	private Uri mUri;
	private WidevineDrm drmManager;

	// State maintained for proper onPause/OnResume behaviour.

	private int mAutoStartCount = 0;
	private int mPositionWhenPaused = -1;
	private boolean mWasPlayingWhenPaused = false;
	private boolean mControlResumed = false;
	private boolean mStopHandler = false;
	private boolean mSessionClosed = false;
	private Context mContext = null;
	private ErrorManagerData errordata = null; 
	private ProgressBar mProgressBar = null;
	private boolean iPlayerStarted=false;

	public static enum StreamType {
		LIVE, VOD
	};

	public static enum StreamProtocol {
		RTSP, HTTP_PROGRESSIVEPLAY
	};

	StreamType mStreamType = null;
	StreamProtocol mStreamProtocol = StreamProtocol.RTSP;

	private MediaController2 mMediaPlayerController = null;

	private MediaPlayer mMediaPlayer;
	private PlayerListener mPlayerListener = null;

	private static final int START = 1;
	private static final int STOP = 2;
	private static final int END = 201;
	private static final int INTERVAL_BUFFERING_PER_UPDATE = 200;

//	private ImageView mCenterPlayButton;

	private Handler mBufferProgressHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			
			if(mStopHandler){
				return;
			}
			if(mSessionClosed){
				return;
			}
			switch (msg.what) {
			case START:
				if (mVideoView == null) {
					return;
				}
				int perBuffer = mVideoView.getBufferPercentage();
				onBufferingUpdate(null, perBuffer);
				msg = obtainMessage(START);
				if (msg != null) {
					sendMessageDelayed(msg, INTERVAL_BUFFERING_PER_UPDATE);
				}
								
				break;
			}
		}
	};
	private boolean isPlayStarted=false;

	public VideoViewPlayer(VideoView rootView, Context context, Uri videoUri,
			StreamType streamType) {
		mMediaPlayer = null;
		mVideoView = rootView;
		mStopHandler = false;
		mUri = videoUri;
		errordata = null;
		mContext = context;
		this.mStreamType = streamType;

		initilizeMediaController();

	}
	public void SetErrorData(ErrorManagerData data){
		this.errordata = data;
	}
	public ErrorManagerData GetErrorData(){
		if(errordata != null && mVideoView != null){
			errordata.playposition = ""+mVideoView.getCurrentPosition(); 
		}
		return errordata;
	}
	public void setUri(Uri videoUri,StreamType type){
//		mPositionWhenPaused = -1;
		mMediaPlayer = null;
		errordata = null;
		mStopHandler = false;
		this.mUri = videoUri;
		this.mStreamType = type; 
		mVideoView.setVisibility(View.VISIBLE);
		initilizeMediaController();
		if(mUri.toString().contains(".wvm")||mUri.toString().contains("file:"))
		{
			prepareDrmManager(mUri.toString());
			acquireRights(mUri.toString());
		}
		else
		{
			openVideo();
		}
		
	}
	private void initilizeMediaController() {

		if(mVideoView == null){
			return;
		}

		if (!(mVideoView.getParent() instanceof RelativeLayout)) {
			return;
		}
		
		// As of now , only RelativeLayout as VideoView parent is supported.

		RelativeLayout parentVideoViewlayout = (RelativeLayout) mVideoView
				.getParent();

//		if (mStreamType == StreamType.VOD) {
			if(mMediaPlayerController == null){
				mMediaPlayerController = new MediaController2(mContext,(mStreamType == StreamType.VOD)?true:false);
				mMediaPlayerController.setCustomVideoView(this);
			}
			mMediaPlayerController.setContentEnabled((mStreamType == StreamType.VOD)?true:false);
			RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

			layout_params.addRule(RelativeLayout.ALIGN_BOTTOM,
					mVideoView.getId());

			if(parentVideoViewlayout.indexOfChild(mMediaPlayerController) == -1){
				parentVideoViewlayout.addView(mMediaPlayerController, layout_params);
			}

			mMediaPlayerController.setVisibility(View.GONE);
//		}else if(mStreamType == StreamType.LIVE){
//			if(mMediaPlayerController != null && parentVideoViewlayout.indexOfChild(mMediaPlayerController) != -1){
//				parentVideoViewlayout.removeView(mMediaPlayerController);
//			}
//		}

//		if(mCenterPlayButton == null){
//			mCenterPlayButton = new ImageView(mContext);
//		
//			mCenterPlayButton.setOnClickListener(mCenterPlayClickListener);
//
//			mCenterPlayButton.setImageResource(R.drawable.play_center_button);
//		
//			RelativeLayout.LayoutParams layout_params2 = new RelativeLayout.LayoutParams(
//				RelativeLayout.LayoutParams.WRAP_CONTENT,
//				RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//			layout_params2.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//			parentVideoViewlayout.addView(mCenterPlayButton, layout_params2);
//		}
//		mCenterPlayButton.setVisibility(View.GONE);

	}

	private PlayerStatusUpdate mPlayerStatusListener;
	public void setPlayerStatusUpdateListener(PlayerStatusUpdate listener){
		mPlayerStatusListener = listener;
		if(drmManager!=null)
		drmManager.setPlayerListener(mPlayerStatusListener);
	}
	public void openVideo() { 
		Log.d("PlayerScreen", "VideoViewPlayer openVideo Start");
		// For streams that we expect to be slow to start up, show a
		// progress spinner until playback starts.
		mAutoStartCount = 0;
		if (mUri == null || mVideoView == null) {
			return;
		}

		String scheme = mUri.getScheme();
		if(scheme == null){
			return;
		}
		if (scheme.equalsIgnoreCase("rtsp")) {
			mStreamProtocol = StreamProtocol.RTSP;
		} else if (scheme.equalsIgnoreCase("http")) {
			mStreamProtocol = StreamProtocol.HTTP_PROGRESSIVEPLAY;
		}
		if(mVideoView != null){
			try{
				mVideoView.stopPlayback();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
//		mVideoView.setVisibility(View.GONE);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		//mVideoView.setVideoURI(mUri);
		mVideoView.setVideoPath(mUri.toString());
		
		
		mVideoView.setOnPreparedListener(this);

		mBufferProgressHandler.sendEmptyMessage(START);

//		showProgressBar(true);

		// make the video view handle keys for seeking and pausing
		mVideoView.requestFocus();

		mCurrentState = STATE_PREPARING;
		Log.d("PlayerScreen", "VideoViewPlayer openVideo end");
	}

	public void showProgressBar(boolean visibility) {

		if (mProgressBar == null) {
			return;
		} 

		if (visibility) {
			mProgressBar.setVisibility(View.VISIBLE);
			
			return;
		}

		mProgressBar.setVisibility(View.GONE);
		//Analytics.endTimedEvent(Analytics.PlayerBuffering);

	}

	OnClickListener mCenterPlayClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (mVideoView == null) {
				return;
			}

//			mCenterPlayButton.setVisibility(View.GONE);

			showProgressBar(true);

			if (mStreamType == StreamType.LIVE) {
				mBufferProgressHandler.sendEmptyMessage(START);
			}
				Log.d("PlayerScreen","onBufferingUpdate form mCenterPlayClickListener");
			onBufferingUpdate(null, 0);

			mVideoView.setVisibility(View.VISIBLE);

			if (mStreamType == StreamType.VOD) {

				// mVideoView.setVideoURI(mUri);

				if (mPositionWhenPaused > 0) {
					mVideoView.seekTo(mPositionWhenPaused);
					mPositionWhenPaused = -1;
				}

				if (mWasPlayingWhenPaused) {
					// mMediaController.show(0);
				}
			}

			mCurrentState = STATE_PREPARING;
			mVideoView.start();

		}
	};

	private Dialog mSplashScreen = null;
	private boolean mSplashScreenDismissed = false;

	/**
	 * call this method on Activity onPause
	 */
	
	public void onPause() {
		Log.d("PlayerScreen", "VideoViewPlayer onPause Start");
		
		if(mVideoView == null){
			return;
		}
		mControlResumed = false;
		mCurrentState = STATE_PAUSED;
		mPositionWhenPaused = mVideoView.getCurrentPosition();
		mWasPlayingWhenPaused = mVideoView.isPlaying();
		// mVideoView.stopPlayback();

		if(mMediaPlayerController != null){
			mMediaPlayerController.setVisibility(View.INVISIBLE);
		}
		
		if (mBufferProgressHandler != null) {
			mBufferProgressHandler.removeMessages(START);
		}

		if (!mWasPlayingWhenPaused) {
			// player is already in pause state , no need to do anything.

			switch (mStreamType) {

			case LIVE:

				// to stop the video view reload the video automatically.
				mVideoView.setVisibility(View.GONE);

				break;

			case VOD:

				mVideoView.setVisibility(View.GONE);

				break;
			}

			return;
		}

		switch (mStreamType) {

		case LIVE:

			mVideoView.setVisibility(View.GONE);
			mVideoView.stopPlayback();

			break;

		case VOD:

			mVideoView.setVisibility(View.GONE);
			mVideoView.pause();

			break;
		}
		Log.d("PlayerScreen", "VideoViewPlayer onPause end"+ "  "+mPositionWhenPaused);
	}

	/**
	 * call this method on Activity onResume
	 */

	public void onResume() {
		Log.d("PlayerScreen", "VideoViewPlayer onResume Start");
				
		if(mVideoView == null){
			return;
		}
		mControlResumed = true;

		if (mCurrentState == STATE_PREPARING) {		
			mVideoView.setVisibility(View.VISIBLE);
				Log.d("PlayerScreen","onBufferingUpdate form onResume");
			onBufferingUpdate(null, 0);
				Log.d("PlayerScreen", "VideoViewPlayer onResume STATE_PREPARING End");
			return;
		}
//		mCenterPlayButton.setVisibility(View.GONE);

		if(mCurrentState == STATE_PAUSED){
			showProgressBar(true);
		}
		if (mStreamType == StreamType.LIVE) {
			mBufferProgressHandler.sendEmptyMessage(START);
		}
			Log.d("PlayerScreen","onBufferingUpdate form onResume1");
		onBufferingUpdate(null, 0);

		mVideoView.setVisibility(View.VISIBLE);

		if (mStreamType == StreamType.VOD) {

			// mVideoView.setVideoURI(mUri);

			if (mPositionWhenPaused > 0) {
				mVideoView.seekTo(mPositionWhenPaused);
				mPositionWhenPaused = -1;
			}

			if (mWasPlayingWhenPaused) {
				// mMediaController.show(0);
			}
		}

		mCurrentState = STATE_PREPARING;
		mVideoView.start();
//		if(mSplashScreenDismissed){
//		}else{
//			mCenterPlayButton.setVisibility(View.VISIBLE);
//			mCurrentState = STATE_IDLE;
//		}
		mSplashScreenDismissed = false;
		Log.d("PlayerScreen", "VideoViewPlayer onResume End");
	}

	public boolean onError(MediaPlayer player, int arg1, int arg2) {
		Log.d("PlayerScreen", "VideoViewPlayer onError End");
		mAutoStartCount++;
		if(mAutoStartCount < 3){
			int value = 0;
			if(player != null){
//				player.reset();
				player.seekTo(mPositionWhenPaused);
				player.start();
			}
			else{
				value = 1;
				mVideoView.seekTo(mPositionWhenPaused);
				mVideoView.start();
			}
			if(mPlayerStatusListener != null){
				mPlayerStatusListener.playerStatusUpdate("Retrying "+mAutoStartCount+" form position "+mPositionWhenPaused+" with error "+arg1+" status "+value);
			}
			Util.showToast(mContext, "Retrying "+mAutoStartCount, Util.TOAST_TYPE_INFO);
			return true;
		}
		
		if (mPlayerListener != null) {
			boolean ret = mPlayerListener.onError(player, arg1, arg2);
			mPlayerListener = null;
			return ret;
		}
		return false;
	}

	public void onCompletion(MediaPlayer mp) {
		Log.d("PlayerScreen", "VideoViewPlayer onCompletion End");
		if (mPlayerListener != null) {
			Analytics.stoppedAt();
			mPlayerListener.onCompletion(mp);
			//mixPanelVideoTimeCalculationOnCompletion();
			Analytics.mixPanelVideoTimeCalculationOnCompletion();
			mPlayerListener.onStateChanged(PlayerListener.STATE_COMPLETED, 	0);
		}
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d("PlayerScreen", "VideoViewPlayer onPrepared End");
		mMediaPlayer = mp;
			Log.d("PlayerScreen", "VideoViewPlayer onPrepared");
		if(mVideoView == null){
			return;
		}
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play onPrepared :: ");
		}
		if(mMediaPlayerController!= null){
			mMediaPlayerController.setMediaPlayer(mp);
		}
//		showProgressBar(false);
		if(!mControlResumed){
			// If activity is not visible.
//			if(mSplashScreen != null && mSplashScreen.isShowing()){
//			}else{
//				mp.stop();			
//				return;
//			}
		}
		// Don't start until ready to play. The arg of seekTo(arg) is the start
		// point in
		// milliseconds from the beginning. In this example we start playing 1/5
		// of
		// the way through the video if the player can do forward seeks on the
		// video.

		
		
		mCurrentState = STATE_PREPARED;
		Log.d("PlayerScreen", "VideoViewPlayer onPrepared");
		mp.setOnSeekCompleteListener(this);
		mp.setOnBufferingUpdateListener(this);
		mp.setOnInfoListener(this);
//		mp.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
//			
//			@Override
//			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//				onBufferingUpdate(mp, 100);
//			}
//		});

		
			Log.d("PlayerScreen","onBufferingUpdate form onPrepared");
		onBufferingUpdate(mp, mVideoView.getBufferPercentage());

		mVideoView.start();

		if (mMediaPlayerController != null) {
			mMediaPlayerController.setMediaPlayer(mVideoView);
		}
		if (mVideoView.canSeekForward() && mPositionWhenPaused > 0) {
			mVideoView.seekTo(mPositionWhenPaused);
		}
	}
	public int getmPositionWhenPaused() {
		return mPositionWhenPaused;
	}
	
	public void deregisteronBufferingUpdate(){
		mStopHandler = true;
		if(mMediaPlayer != null){
			mMediaPlayer.setOnBufferingUpdateListener(null);
		}
		if(mPlayerStatusListener != null){
			mPlayerStatusListener.playerStatusUpdate("Play total duration :: "+mVideoView.getDuration());
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			if(retriever != null) {
				try {
					retriever.setDataSource(mUri.toString());
				}
				catch (IllegalArgumentException ex) {
					Log.e(TAG, "deregisteronBufferingUpdate IllegalArgumentException");
				}
				catch (Exception e) {
					Log.e(TAG, "deregisteronBufferingUpdate Exception");
				}
			}

		}
	}
	public boolean isControlResumed() {
		return mControlResumed;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d("PlayerScreen", "VideoViewPlayer onSeekComplete");
		
			
		if (mPlayerListener != null) {
			mPlayerListener.onSeekComplete(mp);

		}
		boolean dismissdialog = false;
		if(mVideoView != null && mStreamProtocol == StreamProtocol.RTSP && mVideoView.isPlaying() && mVideoView.getCurrentPosition() > (mPositionWhenPaused + 500)){
			dismissdialog = true;
		}
		if(mStreamProtocol != StreamProtocol.RTSP){
			dismissdialog = true;
		}
		if(dismissdialog){
			showProgressBar(false);
		}

		if (mMediaPlayerController != null) {
			mMediaPlayerController.setEnabled(true);
		}

	}

	public void setProgressBarView(ProgressBar progressBar) {
		this.mProgressBar = progressBar;
		showProgressBar(false);
	}
	public void setSplashScreenView(Dialog progressBar) {
		this.mSplashScreen = progressBar;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int perBuffer) {

//		Log.d("VideoViewPlayer", "onBufferingUpdate: "+perBuffer);

		if (mPlayerListener != null) {
			mPlayerListener.onBufferingUpdate(arg0, perBuffer);
//			return;
		}

		if(mProgressBar != null && mProgressBar.getVisibility() != View.VISIBLE){
			return;
		}
		switch (mStreamProtocol) {
		case RTSP:
			boolean dismissdialog = false;
			if(perBuffer > 99){
				dismissdialog = true;
			}
			if(mVideoView != null && mVideoView.isPlaying() && mVideoView.getCurrentPosition() > (mPositionWhenPaused + 500)){
				dismissdialog = true;
			}
			if(dismissdialog){
				showProgressBar(false);
				mCurrentState = STATE_PREPARED;
				
				
			}
//			if (perBuffer > 100) {
//				showProgressBar(false);
//				mCurrentState = STATE_PREPARED;
//			}
//			
//			if (perBuffer == 0) {
//				showProgressBar(true);
//			}

			break;

		default:
			break;
		}

	}

	/**
	 * call this method on Activity onTouchEvent
	 */

	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:

			if (mCurrentState < STATE_PREPARED) {
				return true;
			}
			
			if (mMediaPlayerController != null) {
				mMediaPlayerController.doShowHideControl();
			}
			return true;

		}
		return false;
	}

	public void setPlayerListener(PlayerListener mPlayerListener) {
		this.mPlayerListener = mPlayerListener;
		if(mMediaPlayerController != null){
			mMediaPlayerController.setPlayerListener(mPlayerListener);
		}
	}
	public void closeSession(){
		try {
			if (mVideoView != null) {
				mVideoView.stopPlayback();
				mVideoView.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			Log.e("PlayerScreen", "VideoViewPlayer stopPlayback exception");
			e.printStackTrace();
		}
	}
	public boolean IsMediaControllerVisible(){
		if(mMediaPlayerController != null){
			return mMediaPlayerController.isShowing();
		}
		return false;
	}
	public void hideMediaController(){
		if(mMediaPlayerController != null){
			mMediaPlayerController.hide();
		}
	}
	public void playerInFullScreen(boolean value){
		if(mMediaPlayerController != null){
			mMediaPlayerController.playerInFullScreen(value);
		}
	}
	public void showMediaController(){
		if(mMediaPlayerController != null){
			mMediaPlayerController.show();
		}
	}
	public void setSplashScreenDismissed(boolean value){
		mSplashScreenDismissed = value;
	}
	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		mPositionWhenPaused = mVideoView.getCurrentPosition();
		if (mPlayerListener != null) {
			boolean ret = mPlayerListener.onInfo(arg0, arg1, arg2);
		}
		return false;
	}
	public void acquireRights(String url){
		int rightStatus= drmManager.checkRightsStatus(url);
		if(rightStatus!=DrmStore.RightsStatus.RIGHTS_VALID)
		{
			int status=drmManager.acquireRights(url);
			if(status!=0)
			{
				if(ApplicationSettings.ENABLE_LOG_DRM_ERRORS)
				{
					Crashlytics.logException(new DRMException("acquireRights failed, status:"+status + " \n url:"+url));
				}
				//mixPanelUnableToPlayVideo(Analytics.ACQUIRE_RIGHTS_FAILED);
				Analytics.mixPanelUnableToPlayVideo(Analytics.ACQUIRE_RIGHTS_FAILED);
				Util.showToast(mContext, "Acquire Rights Failed", Util.TOAST_TYPE_INFO);
				//closeSession();
				if(mPlayerListener!=null)
				{
					mPlayerListener.onDrmError();
				}
			}
		}
		else
		{
			startPlayer(true);
		}
		
	}
	public interface OnLicenseExpiry {
		public void licenseExpired();
	}
	
	private OnLicenseExpiry onLicenseExpiryListener=null;
	
	public void setOnLicenseExpiryListener(
			OnLicenseExpiry onLicenseExpiryListener) {
		this.onLicenseExpiryListener = onLicenseExpiryListener;
	}
	
	private void prepareDrmManager(String url){
		
		drmManager = new WidevineDrm(mContext);
		
		
		drmManager.logBuffer.append("Asset Uri: " + url + "\n");
		drmManager.logBuffer.append("Drm Server: " + WidevineDrm.Settings.DRM_SERVER_URI + "\n");
		drmManager.logBuffer.append("Device Id: " + WidevineDrm.Settings.DEVICE_ID + "\n");
		drmManager.logBuffer.append("Portal Name: " + WidevineDrm.Settings.PORTAL_NAME + "\n");

		
        // Set log update listener
        WidevineDrm.WidevineDrmLogEventListener drmLogListener =
            new WidevineDrm.WidevineDrmLogEventListener() {

            public void logUpdated(int status,int value) {
                
            	updateLogs(status,value);
            }
        };
		
        drmManager.setLogListener(drmLogListener);
        
        drmManager.registerPortal(WidevineDrm.Settings.PORTAL_NAME);
        
		}
	private void startPlayer(final boolean status){
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {
			
			@Override
			public void run() {
				if(status){
					openVideo();
				}
				else
				{
					if(mPlayerListener!=null)
					{
						mPlayerListener.onDrmError();
					}
				}
			}
		});
	}
	protected void updateLogs(int status,int value) {
		// TODO Auto-generated method stub
		if(!iPlayerStarted)
		{
			if(status==0 && value== DrmInfoEvent.TYPE_RIGHTS_INSTALLED)
			{
				iPlayerStarted=true;
				startPlayer(true);
			}
			if(status!=0 ){
				iPlayerStarted=true;
				String errMsg = "Error while playing";
								
				switch (value) {
				case DrmErrorEvent.TYPE_NO_INTERNET_CONNECTION:
					errMsg="No Internet Connection";
					break;
				case DrmErrorEvent.TYPE_NOT_SUPPORTED:
					errMsg="Device Not Supported";
					break;
				case DrmErrorEvent.TYPE_OUT_OF_MEMORY:
					errMsg="Out of Memory";
					break;
				case DrmErrorEvent.TYPE_PROCESS_DRM_INFO_FAILED:
					errMsg="Process DRM Info failed";
					break;
				case DrmErrorEvent.TYPE_REMOVE_ALL_RIGHTS_FAILED:
					errMsg="Remove All Rights failed";
					break;
				case DrmErrorEvent.TYPE_RIGHTS_NOT_INSTALLED:
					errMsg="Rights not installed";
					break;
				case DrmErrorEvent.TYPE_RIGHTS_RENEWAL_NOT_ALLOWED:
					errMsg="Rights renewal not allowed";
					break;
			}
				//mixPanelUnableToPlayVideo(errMsg);
				Analytics.mixPanelUnableToPlayVideo(errMsg);
				Util.showToast(mContext,errMsg+" ("+status+")",Util.TOAST_TYPE_INFO);
				startPlayer(false);
				//drmManager.
			}
			
			if(status == Settings.WIDEVINE_AUTH_FAILED){
				//TODO Refresh purchase detail for content id.
				//mixPanelUnableToPlayVideo(Analytics.WIDEVINE_AUTH_FAILED);
				Analytics.mixPanelUnableToPlayVideo(Analytics.WIDEVINE_AUTH_FAILED);
			}
			if(status == 608 || status == 607){
				 iPlayerStarted=false;
				 if(onLicenseExpiryListener != null ){
					 onLicenseExpiryListener.licenseExpired();
				 }
			}
			if(drmManager!=null)
				drmManager.unRegisterLogListener();
		
		}
	}
	
	public void setmPositionWhenPaused(int mPositionWhenPaused) {
		this.mPositionWhenPaused = mPositionWhenPaused;
	}
}
