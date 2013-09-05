package com.apalya.myplex.adapters;

import android.view.View;
import android.view.View.OnClickListener;

public class OpenListener extends BaseListener implements OnClickListener{
	public interface OpenCallBackListener{
		public void OnOpenAction(View v);	
	}
	private OpenCallBackListener listener = null;
	public OpenListener(OpenCallBackListener listener){
		super();
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		if(listener != null && getClickReady()){
			SetClickReady();
			listener.OnOpenAction(v);
		}
	}
}