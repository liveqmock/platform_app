package com.apalya.myplex.data;


import com.apalya.myplex.utils.MyVolley;

import android.app.Application;

public class myplexapplication extends Application {
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		init();
	}

	private void init() {
		//MyVolley.init(this);
	}
}
