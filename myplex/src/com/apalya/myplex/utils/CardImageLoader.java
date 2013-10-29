package com.apalya.myplex.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardImageView;
import com.apalya.myplex.views.CardView;

public class CardImageLoader {
	private static final int FADE_IN_TIME_MS = 450;
	private int mCardId;
	private String requestUrl;
	private CardImageView mView;
	private Context mContext;
	public CardImageLoader(int Id,Context cxt){
		this.mCardId = Id;
		this.mContext = cxt;
	}
	public void loadImage(CardImageView View){
		mView = View;
		requestUrl = View.mImageUrl;
		mCardId = View.mCardId;
		MyVolley.getImageLoader().get(View.mImageUrl, new ImageListener() {
			
			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onResponse(ImageContainer response, boolean isImmediate) {
				if(response == null){ mView.setImageResource(R.drawable.placeholder); return;}
				if(response.getBitmap() == null){ mView.setImageResource(R.drawable.placeholder); return;}
				if(requestUrl.equalsIgnoreCase(mView.mImageUrl)&& mCardId == mView.mCardId){
					TransitionDrawable td = new TransitionDrawable(new Drawable[]{
			                new ColorDrawable(android.R.color.transparent),
			                new BitmapDrawable(mContext.getResources(), response.getBitmap())
			        });

					mView.setImageDrawable(td);
			        td.startTransition(FADE_IN_TIME_MS);
//					mView.setImageBitmap(response.getBitmap());
//					mView.setImageDrawable(new BitmapDrawable(response.getBitmap()));
				}else{
					Log.w("CardView","View mismatch");
				}
			}
		});
	}
}
