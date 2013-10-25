package com.apalya.myplex.media;

import android.media.MediaPlayer;
import android.widget.ImageView;

public interface PlayerListener {

	public void onSeekComplete(MediaPlayer mp) ;
	
	public void onBufferingUpdate(MediaPlayer mp, int perBuffer);
	
	public boolean onError(MediaPlayer mp, int arg1, int arg2) ;
	
	public void onCompletion(MediaPlayer mp) ;
	
	public void onFullScreen(boolean value);
}
