package com.apalya.myplex.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.apalya.myplex.R;
import com.apalya.myplex.data.MsisdnData;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.utils.MsisdnRetrivalEngine.MsisdnRetrivalEngineListener;
import com.apalya.myplex.utils.UserProfileUpdateUtil.UserProfileUpdateCallback;



public class SyncPurchasesUtil {
	public static final String TAG = "SyncPurchasesUtil";

	private SyncPurchasesCallback mListener;
	
	private static final String [] OPERATORS_NAME= {"airtel","vodafone"};
	
	public interface SyncPurchasesCallback{
		public void onComplete(boolean value);
	}
	
	public void setListener(SyncPurchasesCallback mListener) {
		this.mListener = mListener;
	}
	
	public boolean syncPurchases(int type, final Context context){
		
		    
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		
		if(mTelephonyMgr == null){
			return false;			
		}
		
		if(mTelephonyMgr.getSimState() != TelephonyManager.SIM_STATE_READY){
			return false;
		}
		
		String operatorName = mTelephonyMgr.getNetworkOperatorName();
		
		if(TextUtils.isEmpty(operatorName))  return false;
		
		boolean doPurchaseSync = false;
		
		final MsisdnRetrivalEngine msisdnRetrivalEngine = new MsisdnRetrivalEngine(context);
		
		for (int i = 0; i < OPERATORS_NAME.length; i++) {
			if(operatorName.toLowerCase().contains(OPERATORS_NAME[i])){
				doPurchaseSync = true;
				if(i == 0){
					// Operator is Airtel
					msisdnRetrivalEngine.setUrl(ConsumerApi.AIRTEL_MSISDN_RETRIEVER_URL);
				}
				break;
			}
		}

		if(!doPurchaseSync) return false;
		
		// Skip if mobile data is disabled
		if(!Util.isMobileDataEnabled(context)) {			
			return false;
		}
		
		
		msisdnRetrivalEngine.getMsisdnData(new MsisdnRetrivalEngineListener() {
			
			@Override
			public void onMsisdnData(MsisdnData data) {
				msisdnRetrivalEngine.deRegisterCallBacks();
				
				if(data == null || TextUtils.isEmpty(data.msisdn)){
					if(mListener != null){
						mListener.onComplete(false);
					}
					return;
				}
				
				UserProfileUpdateUtil userProfileUpdateUtil = new UserProfileUpdateUtil(context);
				userProfileUpdateUtil.setListener(new UserProfileUpdateCallback() {
					
					@Override
					public void onComplete(boolean status, String message) {
					
						if(TextUtils.isEmpty(message)){
							message = status ? "profile update error:"+"user profile updated":"profile update error2:"+context.getString(R.string.syncing_purchases_failed_setting_msg);
						}
						
						if(status){
							Util.showToast(context, message, Util.TOAST_TYPE_ERROR);
						}
						
						if(mListener != null){
							mListener.onComplete(status);
						}
					}
				});
				
				userProfileUpdateUtil.updateProfile(data.msisdn);
				
				
				
				Log.e(TAG, "onMsisdnData msisdn "+data.msisdn+" operator "+data.operator);
				
			}
		});
		
		return true;
	}
	
}
