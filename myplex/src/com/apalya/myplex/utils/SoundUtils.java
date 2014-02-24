package com.apalya.myplex.utils;

import com.apalya.myplex.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class SoundUtils {
	Context mContext;
	int res;
	
	
	public SoundUtils(Context context, int res){
		this.mContext = context;
		this.res =res;
		
	}
	public  void playSound() {
        // TODO Auto-generated method stub      
        SoundPool pl = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        // 5 indicates the maximum number of simultaneous streams for this SoundPool object

        int waterSound = pl.load(mContext,res, 0);
        // is the audio file I have imported in my project as resource

        pl.setOnLoadCompleteListener(new OnLoadCompleteListener() {             
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                // The onLoadComplet method is called when a sound has completed loading.
                // TODO Auto-generated method stub
                soundPool.play(sampleId, 1f, 1f, 0, 0, 1);
                // second and third parameters indicates left and right value (range = 0.0 to 1.0)
            }
        });
    }
	

}
