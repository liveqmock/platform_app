/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apalya.myplex.media;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.Util;
import com.google.analytics.tracking.android.EasyTracker;

public class MediaController2 extends LinearLayout {

    private VideoView  mPlayer;
    private Context             mContext;    
    private View                mRoot;    
    private SeekBar         mProgress;
    private TextView            mEndTime, mCurrentTime;
    private boolean             mShowing;
    private boolean             mDragging;
    private static final int    sDefaultTimeout = 10000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private boolean             mUseFastForward;
    private boolean             mFromXml;
    private boolean             mListenersSet;
    private View.OnClickListener mNextListener, mPrevListener;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    private RelativeLayout         mPauseButton;
    private ImageView         mPauseButtonImage;
    private ImageView         mFfwdButton;
    private ImageView         mRewButton;
    private ImageView         mNextButton;
    private ImageView         mPrevButton;
    private RelativeLayout 			mMuteButton;
    private ImageView 			mMuteButtonImage;
    private RelativeLayout 			mFullScreenTooggle;
    private ImageView 			mFullScreenTooggleImage;
    private MediaPlayer   	  mMediaPlayer = null;
    private boolean   mMuteEnabled = false;
    private VideoViewPlayer mCustomVideoView = null;
    private boolean mContentEnabled = false;
    private PlayerListener mPlayerListener;

    public MediaController2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;       
        setAnchorView(null);
        
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }
    public void setMediaPlayer(MediaPlayer mp){
    	mMediaPlayer = mp;
    }
    public void setCustomVideoView(VideoViewPlayer v){
    	mCustomVideoView = v;
    }
    public void setPlayerListener(PlayerListener mPlayerListener){
    	this.mPlayerListener = mPlayerListener;
    }
    public void setContentEnabled(boolean contentEnabled){
    	mContentEnabled = contentEnabled;
    	setAnchorView(null);
    }
    public MediaController2(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward=false;
    	mContentEnabled = useFastForward;
        setAnchorView(null);
        
    }

    public MediaController2(Context context) {
        super(context);
        mContext = context;  
        mUseFastForward=false;
        initFloatingWindow();
        setAnchorView(null);
        
    }


    private void initFloatingWindow() {
    	
//        mWindowManager = (WindowManager)mContext.getSystemService("window");
//        mWindow = PolicyManager.makeNewWindow(mContext);
//        mWindow.setWindowManager(mWindowManager, null, null);
//        mWindow.requestFeature(Window.FEATURE_NO_TITLE);
//        mDecor = mWindow.getDecorView();
//        mDecor.setOnTouchListener(mTouchListener);
//        mWindow.setContentView(this);
//        mWindow.setBackgroundDrawableResource(android.R.color.transparent);
//        
//        // While the media controller is up, the volume control keys should
//        // affect the media stream type
//        mWindow.setVolumeControlStream(AudioManager.STREAM_MUSIC);

    	
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
//        requestFocus();
    }
    
 
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                }
            }
            return false;
        }
    };
    
    public void setMediaPlayer(VideoView player) {
        mPlayer = player;
        updatePausePlay();
    }

    
    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {
        

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        removeAllViews();
        View v = makeControllerView();
        
        addView(v,frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mContentEnabled){
        	mRoot = inflate.inflate(R.layout.media_controller, null);
        }else{
        	mRoot = inflate.inflate(R.layout.media_controller_live, null);
        }
        initControllerView(mRoot);

        return mRoot;
    }
    public void playerInFullScreen(boolean value){
    	if(mFullScreenTooggleImage== null){return;}
    	if(value){
    		mFullScreenTooggleImage.setImageResource(R.drawable.player_collapse);	
    	}else{
    		mFullScreenTooggleImage.setImageResource(R.drawable.player_expand);
    	}
    	
    }
    private boolean mPlayerFullScreen = false;
    private void initControllerView(View v) {
    	mPlayerFullScreen = false;
    	if(mContentEnabled){
    		mPauseButton = (RelativeLayout) v.findViewById(R.id.playpause);
    		mPauseButtonImage = (ImageView)v.findViewById(R.id.playpauseimage);
    		Util.showFeedback(mPauseButton);
    	}
        if (mPauseButton != null) {
        	mPauseButton.setVisibility(View.VISIBLE);
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        mFullScreenTooggle = (RelativeLayout) v.findViewById(R.id.playerfullscreen);
        Util.showFeedback(mFullScreenTooggle);
        mFullScreenTooggleImage = (ImageView) v.findViewById(R.id.playerfullscreenimage);
        if(mFullScreenTooggle != null){
        	mFullScreenTooggle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mPlayerListener != null){
						mPlayerFullScreen = !mPlayerFullScreen;
						mPlayerListener.onFullScreen(mPlayerFullScreen);
						playerInFullScreen(mPlayerFullScreen);
					}
				}
			});
        }
        playerInFullScreen(false);
        ImageView stopButton = (ImageView)v.findViewById(R.id.playerstop);
        if(stopButton != null){
        	stopButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mPlayer != null){
						mPlayer.stopPlayback();
						if(mCustomVideoView != null){
							mCustomVideoView.onCompletion(mMediaPlayer);
						}
						updatePlayerState(PlayerListener.STATE_STOP,0);
					}
				}
			});
        }
        mMuteButton = (RelativeLayout)v.findViewById(R.id.playervolume);
        Util.showFeedback(mMuteButton);
        mMuteButtonImage = (ImageView)v.findViewById(R.id.playervolumeimage);
        if(mMuteButton != null){
        	mMuteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
//						AudioManager audioManager=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
//						int current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//					    //If you want to player is mute ,then set_volume variable is zero.Otherwise you may supply some value.
//						int set_volume=0;
//						if(current_volume == 0){
//							 set_volume = 1;	
//						}
//						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,set_volume, 0);
						
						if(!mMuteEnabled){
//							Toast toast = Toast.makeText(mContext, "Muted", Toast.LENGTH_LONG);
//							toast.show();
							Util.showToast(mContext,"Muted",Util.TOAST_TYPE_INFO);
							mMuteButtonImage.setImageResource(R.drawable.player_icon_volume_mute);
							mMediaPlayer.setVolume(0,0);
							mMuteEnabled = true;
						}else{
//							Toast toast = Toast.makeText(mContext, "Unmuted", Toast.LENGTH_LONG);
//							toast.show();
							Util.showToast(mContext,"Unmuted",Util.TOAST_TYPE_INFO);
							mMuteButtonImage.setImageResource(R.drawable.player_icon_volume_max);
							mMediaPlayer.setVolume(1,1);
							mMuteEnabled = false;
						}
						
						
					} catch (Exception e) {
						e.printStackTrace();
						// TODO: handle exception
					}
				}
			});
        }
//
//        mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
//        if (mFfwdButton != null) {
//            mFfwdButton.setOnClickListener(mFfwdListener);
//            if (!mFromXml) {
//                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
//            }
//        }
//
//        mRewButton = (ImageButton) v.findViewById(R.id.rew);
//        if (mRewButton != null) {
//            mRewButton.setOnClickListener(mRewListener);
//            if (!mFromXml) {
//                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
//            }
//        }
//
//        // By default these are hidden. They will be enabled when setPrevNextListeners() is called 
//        mNextButton = (ImageButton) v.findViewById(R.id.next);
//        if (mNextButton != null && !mFromXml && !mListenersSet) {
//            mNextButton.setVisibility(View.GONE);
//        }
//        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
//        if (mPrevButton != null && !mFromXml && !mListenersSet) {
//            mPrevButton.setVisibility(View.GONE);
//        }

        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            SeekBar seeker = (SeekBar) mProgress;
            seeker.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setMax(1000);
        }

        if(mContentEnabled){
        	mEndTime = (TextView) v.findViewById(R.id.playertotaltime);
        	mCurrentTime = (TextView) v.findViewById(R.id.playerexpiredtime);
        }
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }
    
    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     * @param timeout The timeout in milliseconds. Use 0 to show
     * the controller until hide() is called.
     */
    public void show(int timeout) {

    	if(mPlayer == null){
    		return;
    	}
    	
    	
        if (!mShowing ) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            setVisibility(View.VISIBLE);
            mShowing = true;
        }
        updatePausePlay();
        
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }
    
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
    	
        
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                setVisibility(View.INVISIBLE);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0 && (
                keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK ||
                keyCode ==  KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                keyCode ==  KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            show(sDefaultTimeout);
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            return true;
        } else if (keyCode ==  KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            updatePlayerState(PlayerListener.STATE_STOP,0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            hide();

            return true;
        } else {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if(!isEnabled()){
        	return;
        }
        
        if (mPlayer.isPlaying()) {
        	if(mPauseButtonImage != null)
        		mPauseButtonImage.setImageResource(R.drawable.player_icon_pause);            
        } else {
        	if(mPauseButtonImage != null)
        		mPauseButtonImage.setImageResource(R.drawable.player_icon_play);
        }
    }
    
    public void doShowHideControl(){
    	if(mShowing){
    		hide();
    	}else {
    		show();
    	}
    }
    
    private long totalTime  = 0;
    private long startTime  = 0;
    private void doPauseResume() {
    	if (mPlayer.isPlaying()) {
    		mPlayer.pause();  
    		Analytics.pausedAt();
    		Analytics.gaStopPauseMediaTime(Analytics.ACTION_TYPES.pause.toString(),(mPlayer.getCurrentPosition()/1000));
    		updatePlayerState(PlayerListener.STATE_PAUSED,mPlayer.getCurrentPosition());
    	} else {
    		mPlayer.start();
    		Analytics.resumedAt();
    		
    		updatePlayerState(PlayerListener.STATE_PLAYING,mPlayer.getCurrentPosition());
    	}
    	updatePausePlay();
    	        	
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
//            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
//                return true;
//            }

            
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
//            setProgress();
//            updatePausePlay();
//            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
            
            int progress = bar.getProgress();
            
            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo( (int) newposition);
            setEnabled(false);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));
            
        }
    };

    
    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private View.OnClickListener mRewListener = new View.OnClickListener() { 
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();
            
            if (mNextButton != null && !mFromXml) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface MediaPlayerControl {
        void    start();
        void    pause();
        int     getDuration();
        int     getCurrentPosition();
        void    seekTo(int pos);
        boolean isPlaying();
        int     getBufferPercentage();
        boolean canPause();
        boolean canSeekBackward();
        boolean canSeekForward();
    }
    /**
     *  @param state 
     *  This method is the callback method for the player state.
     */
    public void updatePlayerState(int state,int position)
    {    
    	if(mPlayerListener!= null){
    		int pos = mPlayer.getCurrentPosition()/1000;
    		mPlayerListener.onStateChanged(state,pos);
    	}
    	
    }
    public void resumePlay(int ellapseTime){
    	 long pos = 1000L * ellapseTime ;
//         mProgress.setProgress( (int) pos);
         mPlayer.seekTo( (int)pos);
    }
}
