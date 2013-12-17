package com.apalya.myplex.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.SubscriptionView;
import com.apalya.myplex.cache.InsertionResult;
import com.apalya.myplex.data.BaseReponseData;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.MsisdnData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.FetchCardField.FetchComplete;
import com.apalya.myplex.utils.MsisdnRetrivalEngine.MsisdnRetrivalEngineListener;
import com.apalya.myplex.views.CustomDialog;
import com.apalya.myplex.views.JazzyViewPager.TransitionEffect;
import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SubcriptionEngine {
	public static final String TAG = "SubcriptionEngine";
	private Context mContext;
	private ProgressDialog mProgressDialog = null;
	private int mSelectedOption;
	private MsisdnRetrivalEngine mMsisdnRetrivalEngine;
	private CardDataPackagePriceDetailsItem mSelectedPriceItem;
	private CardDataPackages mSelectedPackageItem;
	private CustomDialog mAlbumDialog;
	public SubcriptionEngine(Context context){
		this.mContext = context;
	}
	public void doSubscription(CardDataPackages packageitem,int selectedOption){
		this.mSelectedPackageItem = packageitem;
		mMsisdnRetrivalEngine = new MsisdnRetrivalEngine(mContext);
		this.mSelectedOption = selectedOption;
		if(mSelectedPackageItem == null || mSelectedPackageItem.priceDetails == null || mSelectedPackageItem.priceDetails.size() < mSelectedOption){
			Util.showToast(mContext, "Error while subscribing", Util.TOAST_TYPE_ERROR);
			return;
		}
		try {
			mSelectedPriceItem = mSelectedPackageItem.priceDetails.get(mSelectedOption);
			Log.e(TAG, "processing payment channel "+mSelectedPriceItem.paymentChannel +" webbased = "+mSelectedPriceItem.webBased);
			if(mSelectedPriceItem.paymentChannel != null && mSelectedPriceItem.paymentChannel.equalsIgnoreCase("OP")){
				if(mSelectedPriceItem.webBased){
					launchWebBasedSubscription();
				}else{
					if(mSelectedPriceItem.doubleConfirmation){
						showConfirmationDialog();
					}else{
						doOperatorBilling();
					}
				}
//				sendFlurryMessage(packageitem,priceItem);
			}else if(mSelectedPriceItem.paymentChannel != null && (mSelectedPriceItem.paymentChannel.equalsIgnoreCase("CC")||mSelectedPriceItem.paymentChannel.equalsIgnoreCase("DC")||mSelectedPriceItem.paymentChannel.equalsIgnoreCase("NB"))){
				launchWebBasedSubscription();
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
	private void showConfirmationDialog(){
		mAlbumDialog = new CustomDialog(mContext);
		mAlbumDialog.setContentView(R.layout.subscriptionconfirmationdialog);
		TextView textView = (TextView)mAlbumDialog.findViewById(R.id.subscription_confirmationtextview);
		//subscription_confirmationtextview
		String msg = (String) textView.getText();
		msg = msg +" "+ mSelectedPriceItem.name +" Network pack for "+ " Rs." + mSelectedPriceItem.price +" only valid for "+mSelectedPackageItem.duration+" days"; 
		textView.setText(msg);
		Button ok = (Button)mAlbumDialog.findViewById(R.id.subscription_ok_button);
		Button cancel = (Button)mAlbumDialog.findViewById(R.id.subscription_cancel_button);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mAlbumDialog.dismiss();				
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mAlbumDialog.dismiss();	
				doOperatorBilling();
			}
		});
		mAlbumDialog.setCancelable(true);
		mAlbumDialog.show(); 
	}
	private void doOperatorBilling(){
		showProgressBar();
		Log.e(TAG, "doOperatorBilling");
		mMsisdnRetrivalEngine.getMsisdnData(new MsisdnRetrivalEngineListener() {
			
			@Override
			public void onMsisdnData(MsisdnData data) {
				mMsisdnRetrivalEngine.deRegisterCallBacks();
				if(data == null){
					Util.showToast(mContext, "Subscription failed", Util.TOAST_TYPE_ERROR);
					dismissProgressBar();
					return;
				}
				Log.e(TAG, "onMsisdnData msisdn "+data.msisdn+" operator "+data.operator);
				sendSubscriptionRequest(data);
			}
		});
	}
	private void sendSubscriptionRequest(final MsisdnData data){
		Log.e(TAG, "sendSubscriptionRequest");
		RequestQueue queue = MyVolley.getRequestQueue();
		String requestUrl = ConsumerApi.getSusbcriptionRequest(mSelectedPriceItem.paymentChannel, mSelectedPackageItem.packageId);
		StringRequest myReg = new StringRequest(Method.POST,requestUrl, onlineRequestSuccessListener(), onlineRequestErrorListener()){
			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				
		        Map<String, String> params = new HashMap<String, String>();
		        params.put("clientKey", ConsumerApi.DEBUGCLIENTKEY);
		        params.put("mobile", data.msisdn);
		        params.put("operator", data.operator);
		        params.put("packageId", mSelectedPackageItem.packageId);
		        params.put("paymentChannel", mSelectedPriceItem.paymentChannel);
		        return params;
			}
		};
		Log.e(TAG, "request: "+requestUrl);
		myReg.setShouldCache(false);
		myReg.setRetryPolicy(new DefaultRetryPolicy(15 * 1000, 1, 1.0f));
		queue.add(myReg);
	}
	
	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.e(TAG, "onlineRequestSuccessListener "+response);
				BaseReponseData responseSet  = null;
				try {
					responseSet  =(BaseReponseData) Util.fromJson(response, BaseReponseData.class);
				} catch (Exception e) {
					// TODO: handle exception
				}
				if(responseSet != null && responseSet.code >= 200 && responseSet.code < 300){
					Log.e(TAG, "onlineRequestSuccessListener success");
					postSubscriptionSuccess();	
				}else{
					Util.showToast(mContext, "Subscription failed", Util.TOAST_TYPE_ERROR);
					dismissProgressBar();
				}
				
			}
		};
	}
	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Subscriptionresponse "+error);
				Util.showToast(mContext, "Subscription failed", Util.TOAST_TYPE_ERROR);
				dismissProgressBar();
			}
		};
	}
	private void postSubscriptionSuccess(){

		Util.showToast(mContext, "Subscription: Success",Util.TOAST_TYPE_INFO);
//		Toast.makeText(SubscriptionView.this, "Subscription: Success", Toast.LENGTH_SHORT).show();
		FetchCardField fetch = new FetchCardField();
		fetch.Fetch(myplexapplication.getCardExplorerData().cardDataToSubscribe, ConsumerApi.FIELD_CURRENTUSERDATA, new FetchComplete() {
			
			@Override
			public void response(CardResponseData data) {
				dismissProgressBar();
				if (data == null || data.results == null || data.results.size() == 0) {
					return;
				}
				CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
				subscribedData.currentUserData =  data.results.get(0).currentUserData;
				
				
				Map<String,String> params=new HashMap<String, String>();
				/*params.put("CardId",subscribedData._id);
				params.put("CardName", subscribedData.generalInfo.title);
				params.put("Status", "Purchase Success");
				Analytics.trackEvent(Analytics.PackagesPurchase,params);*/
				
				params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);
				params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);
				params.put(Analytics.PAY_COMMERCIAL_TYPE_PROPERTY, Analytics.PAY_COMMERCIAL_TYPES.Buy.toString());
				params.put(Analytics.PAY_STATUS_PROPERTY, Analytics.PAY_CONTENT_STATUS_TYPES.Success.toString());				
				Analytics.trackEvent(Analytics.EVENT_PAY,params);
				
				List<CardData> dataToSave = new ArrayList<CardData>();
				dataToSave.add(subscribedData);
				myplexapplication.getCacheHolder().UpdataDataAsync(dataToSave, new InsertionResult() {
					
					@Override
					public void updateComplete(Boolean updateStatus) {
						Util.showToast(mContext, "refresh your screen to see purchases",Util.TOAST_TYPE_INFO);
//						Toast.makeText(SubscriptionView.this, "Subscription Info updated", Toast.LENGTH_SHORT).show();
					}
				});					
			}
		});
	}
	private void launchWebBasedSubscription(){
		Log.e(TAG, "launchWebBasedSubscription");
		String requestUrl = ConsumerApi.getSusbcriptionRequest(mSelectedPriceItem.paymentChannel, mSelectedPackageItem.packageId);
		Intent i = new Intent(mContext,SubscriptionView.class);
		Bundle b = new Bundle();
		b.putString("url", requestUrl);
		i.putExtras(b);
		sendFlurryMessage();
		((Activity) mContext).startActivityForResult(i, ConsumerApi.SUBSCRIPTIONREQUEST);
	}
	private void sendFlurryMessage(){
		FlurryAgent.onStartSession(mContext, "X6WWX57TJQM54CVZRB3K");
		Map<String,String> params=new HashMap<String, String>();
		/*
		params.put("PackageId",mSelectedPackageItem.packageId);
		params.put("PackageName", mSelectedPackageItem.packageName);
		params.put("PaymentChannel", mSelectedPriceItem.paymentChannel);
		params.put("Action", "Purchase");
		*/
		
		params.put(Analytics.PAY_PACKAGE_ID,mSelectedPackageItem.packageId);
		params.put(Analytics.PAY_PACKAGE_NAME, mSelectedPackageItem.packageName);
		params.put(Analytics.PAY_PACKAGE_CHANNEL, mSelectedPriceItem.paymentChannel);
		params.put(Analytics.PAY_STATUS_PROPERTY, Analytics.PAY_COMMERCIAL_TYPES.Buy.toString());
		Analytics.trackEvent(Analytics.PAY_PACKAGE_PURCHASE_STATUS,params);
		
		FlurryAgent.onEndSession(mContext);
	}
	public void showProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(mContext, "", "Loading...",true, false);
	}

	public void dismissProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
}
