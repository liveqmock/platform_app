package com.apalya.myplex.views;

import java.util.Random;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
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
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.media.VideoViewExtn;
import com.apalya.myplex.media.VideoViewPlayer;
import com.apalya.myplex.media.VideoViewPlayer.StreamType;
import com.apalya.myplex.utils.MediaUtil;
import com.apalya.myplex.utils.MediaUtil.MediaUtilEventListener;
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
	private PlayerFullScreen mPlayerFullScreen;

	public void setFullScreenListener(PlayerFullScreen mListener){
		this.mPlayerFullScreen = mListener;
	}
	public interface PlayerFullScreen{
		public void playerInFullScreen(boolean value);
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
		return v;
	}

	private OnClickListener mPlayerClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			FetchUrl();
			mVideoViewParent.setOnClickListener(null);
			// TODO Auto-generated method stub

		}
	};

	public void closePlayer() {
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
					Toast.makeText(mContext, "Failed in fetching the url.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (url == null) {
					closePlayer();
					Toast.makeText(mContext, "No url to play.",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Uri uri = Uri
						.parse("rtsp://59.162.166.216:554/AAJTAK_QVGA.sdp");
				uri = Uri
						.parse("rtsp://46.249.213.87:554/playlists/bollywood-action_qcif.hpl.3gp");
				 uri = Uri.parse(url);
				// Toast.makeText(getContext(), "URL:"+url,
				// Toast.LENGTH_SHORT).show();
				VideoViewPlayer.StreamType streamType = StreamType.VOD;
				if (mVideoViewPlayer == null) {
					mVideoViewPlayer = new VideoViewPlayer(mVideoView,
							mContext, uri, streamType);
					mVideoViewPlayer.openVideo();
				} else {
					mVideoViewPlayer.setUri(uri, streamType);
				}
				mVideoViewPlayer.hideMediaController();
				mVideoView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent event) {
						mVideoViewPlayer.onTouchEvent(event);
						return false;
					}
				});
				mVideoViewPlayer.setPlayerListener(CardVideoPlayer.this);
			}
		});
		MediaUtil.getVideoUrl(mData._id, "low");
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
			if(!mContext.getResources().getBoolean(R.bool.isTablet)){
				((MainBaseOptions) mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int arg1, int arg2) {
		closePlayer();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		closePlayer();
	}

	@Override
	public void onFullScreen(boolean value) {
		if(mContext.getResources().getBoolean(R.bool.isTablet)){
			if (value) {
				playInLandscape();
				// ((MainBaseOptions)mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				playInPortrait();
				// ((MainBaseOptions)mContext).setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}

	public void playInLandscape() {
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
