package com.apalya.myplex.adapters;

import java.util.Timer;
import java.util.TimerTask;

public class BaseListener {
	private Timer mT = null;
	private boolean mClickReady = true; 
	private boolean mTaskScheduled = false; 
	private int mTimeOut = (3*1000);
	public BaseListener(){
		mT = new Timer();
	}
	public void SetClickReady(){
		mClickReady = false;
		try{
			if(!mTaskScheduled){
				mTaskScheduled = true;
				mT.schedule(new TimerTask() {
					@Override
					public void run() {
						mClickReady = true;		
						mTaskScheduled = false;
					}
				}, mTimeOut);
			}
		}catch (Exception e) {
			mClickReady = true;
			e.printStackTrace();
		}
	}
	public boolean getClickReady(){
		return mClickReady;
	}
}
