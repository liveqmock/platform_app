package com.apalya.myplex.adapters;

import android.view.View;
import android.view.View.OnClickListener;

public class CardItemClickListener extends BaseListener implements OnClickListener{

	@Override
	public void onClick(View v) {
		if(getClickReady()){
			SetClickReady();
			onDelayedClick(v);
		}
	}
	public void onDelayedClick(View v){
		
	}
}
