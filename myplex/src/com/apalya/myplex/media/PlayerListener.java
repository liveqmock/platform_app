package com.apalya.myplex.media;

import android.media.MediaPlayer;
import android.widget.ImageView;

public interface PlayerListener {
	// Variables added for Player states
	int STATE_PLAYING = 3;
	int STATE_PAUSED = 4;
	int STATE_PLAYBACK_COMPLETED = 5;
	int STATE_STOP = 6;
	int STATE_RESUME = 7;
	int STATE_SUSPEND_UNSUPPORTED = 8;
	int STATE_STARTED = 9;
	int STATE_COMPLETED = 10;

	public void onSeekComplete(MediaPlayer mp) ;

	public void onBufferingUpdate(MediaPlayer mp, int perBuffer);

	public boolean onError(MediaPlayer mp, int arg1, int arg2) ;

	public boolean onInfo(MediaPlayer mp, int arg1, int arg2) ;

	public void onCompletion(MediaPlayer mp) ;

	public void onFullScreen(boolean value);

	public void onDrmError();

	public void onStateChanged(int state, int pos);
}
