package com.apalya.myplex.views;


import com.apalya.myplex.adapters.BaseListener;

import android.view.View;
import android.view.View.OnClickListener;

public class ItemExpandListener extends BaseListener implements OnClickListener{
	public interface ItemExpandListenerCallBackListener{
		public void OnItemExpand(View v);	
	}
	private ItemExpandListenerCallBackListener listener = null;
	public ItemExpandListener(ItemExpandListenerCallBackListener listener){
		super();
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		if(listener != null && getClickReady()){
			SetClickReady();
			listener.OnItemExpand(v);
		}
	}
}
