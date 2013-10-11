package com.apalya.myplex.utils;

import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.apalya.myplex.data.CardImageView;
import com.apalya.myplex.views.CardView;

public class CardImageLoader {
	private int mCardId;
	private CardView mParent;
	private String requestUrl;
	private CardImageView mView;
	public CardImageLoader(CardView parent,int Id){
		this.mParent = parent;
		this.mCardId = Id;
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
				if(response == null){return;}
				if(response.getBitmap() == null){return;}
				if(requestUrl.equalsIgnoreCase(mView.mImageUrl)&& mCardId == mView.mCardId){
					mView.setImageBitmap(response.getBitmap());
//					mView.setImageDrawable(new BitmapDrawable(response.getBitmap()));
				}else{
					Log.w("CardView","View mismatch");
				}
			}
		});
	}
}
