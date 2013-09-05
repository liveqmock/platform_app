package com.apalya.myplex.data;

import android.graphics.Rect;

public class CardData {
	public String title;
	public String imageUrl;
	public int resId;
	public Rect ImageRect;
	public CardData(String title,String imageUrl,int resId){
		this.imageUrl = imageUrl;
		this.title = title;
		this.resId = resId;
	}
}