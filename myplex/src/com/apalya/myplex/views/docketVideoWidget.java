package com.apalya.myplex.views;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardDetailMediaData;
import com.apalya.myplex.data.myplexUtils;
import com.apalya.myplex.media.PlayerListener;
import com.apalya.myplex.media.VideoView;
import com.apalya.myplex.media.VideoViewPlayer;
import com.apalya.myplex.media.VideoViewPlayer.StreamType;
import com.apalya.myplex.utils.MyVolley;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class docketVideoWidget implements PlayerListener{
	private Context mContext;
	private LayoutInflater mInflater;
	private RelativeLayout mProgressBar;
	private FadeInNetworkImageView mPreviewImage;
	private VideoView mVideoView;
	private ImageView mPlay;
	private boolean mPlaying = false;
	private CardDetailMediaData mMetaData;
	VideoViewPlayer mVideoViewPlayer;
	private int mPerBuffer = 0;
	private PlayerListener mPlayerListener;
	public docketVideoWidget(Context cxt) {
		this.mContext = cxt;
		mInflater = LayoutInflater.from(mContext);
	}

	public View CreateView(CardDetailMediaData media) {
		mMetaData = media;
		View v = mInflater.inflate(R.layout.cardmediasubitemvideo, null);
		int width , height = 100;
		
		width = myplexUtils.mScreenWidth - (int)(mContext.getResources().getDimension(R.dimen.margin_gap_8) +mContext.getResources().getDimension(R.dimen.margin_gap_24));
		height = (width * 9)/16; 
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
		mPreviewImage = (FadeInNetworkImageView) v.findViewById(R.id.cardmediasubitemvideo_imagepreview);
		mPreviewImage.setLayoutParams(params);
		mVideoView = (VideoView)v.findViewById(R.id.cardmediasubitemvideo_videopreview);
		mVideoView.setLayoutParams(params);
		mPlay = (ImageView) v.findViewById(R.id.cardmediasubitemvideo_play);
		mProgressBar = (RelativeLayout) v.findViewById(R.id.cardmediasubitemvideo_progressbarLayout);
		
		mPreviewImage.setImageUrl(media.mThumbnailUrl, MyVolley.getImageLoader());
		myplexUtils.showFeedback(mPlay);

		mPlay.setOnClickListener(mPlayListener);
		
		return v;
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
			mVideoViewPlayer.setPlayerListener(docketVideoWidget.this);	
		}
	};

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

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

}
