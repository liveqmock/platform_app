package com.apalya.myplex.utils;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class CircleImageLoader {
	private Context mContext;
	private ImageView mImageView;
	private String mRequestUrl; 
	public void loadImage(Context context,ImageView view,String RequestUrl){
		mContext = context;
		mImageView = view;
		mRequestUrl = RequestUrl;
		MyVolley.getImageLoader().get(RequestUrl, new ImageListener() {
			
			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onResponse(ImageContainer response, boolean isImmediate) {
				Bitmap bm;
				if(response == null){return;}
				bm = response.getBitmap();
				if(bm == null){return;}
				mImageView.setImageBitmap(bm);
//				Util.FitToRound(mContext,mImageView,bm);
			}
		});
	}
}
