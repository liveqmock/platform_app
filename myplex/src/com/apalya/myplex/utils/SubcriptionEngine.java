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

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

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
import android.widget.Toast;

public class SubcriptionEngine {
	public static final String TAG = "SubcriptionEngine";
	private Context mContext;
	private ProgressDialog mProgressDialog = null;
	private int mSelectedOption;
	private MsisdnRetrivalEngine mMsisdnRetrivalEngine;
	private CardDataPackagePriceDetailsItem mSelectedPriceItem;
	private CardDataPackages mSelectedPackageItem;
	private CustomDialog mAlbumDialog;
	private String couponCode = "";	
	public String getCouponCode() {
		return couponCode;
	}
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
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
//				
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
		CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
		String contentName = subscribedData.generalInfo.title;
		msg = msg +" "+ mSelectedPackageItem.contentType + " "  +contentName + " pack for "+ "Rs." + mSelectedPriceItem.price;
		textView.setText(msg);
		final Button ok = (Button)mAlbumDialog.findViewById(R.id.subscription_ok_button);
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
				ok.setEnabled(false);
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
	
	private String lastRequestUrl = null;
	
	private void sendSubscriptionRequest(final MsisdnData data){		
		Log.e(TAG, "sendSubscriptionRequest");
		RequestQueue queue = MyVolley.getRequestQueue();
		if(lastRequestUrl != null){			
			return;
		}
		String requestUrl = ConsumerApi.getSusbcriptionRequest(mSelectedPriceItem.paymentChannel, mSelectedPackageItem.packageId);
		lastRequestUrl = requestUrl;
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
		myReg.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, 0f));
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
					Log.e(TAG, "request: "+e.toString());
				}
				if(responseSet != null && responseSet.code >= 200 && responseSet.code < 300){
					Log.e(TAG, "onlineRequestSuccessListener success");
					postSubscriptionSuccess();	
				}else{
					Util.showToast(mContext, "Subscription failed", Util.TOAST_TYPE_ERROR);
					mixpanelSubscriptionFailed(response);// Added for mixpanel analytics					
					dismissProgressBar();
				}
				
			}
		};
	}
	
	// Added for mixpanel analytics
	private void mixpanelSubscriptionFailed(String error) {
		CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);
		params.put(Analytics.PAY_CONTENT_PRICE, mSelectedPriceItem.price+"");
		params.put(Analytics.PAY_PURCHASE_TYPE, mSelectedPackageItem.commercialModel); //Rental or buy
		params.put(Analytics.PAYMENT_METHOD, mSelectedPriceItem.paymentChannel); //cc or dc
		params.put(Analytics.CONTENT_QUALITY, mSelectedPackageItem.contentType); //SD or HD
		String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type); //movie or livetv
		params.put(Analytics.CONTENT_TYPE_PROPERTY, ctype);//movie or livetv
		params.put(Analytics.REASON_FAILURE,error);
		String event = Analytics.EVENT_SUBSCRIPTION_FAILURE;
		Analytics.trackEvent(event,params);
	}
	
	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Subscriptionresponse "+error);
				Util.showToast(mContext, "Subscription failed", Util.TOAST_TYPE_ERROR);
				mixpanelSubscriptionFailed(error.toString());// Added for mixpanel analytics
				dismissProgressBar();
			}
		};
	}
	
	private void mixPanelSubscriptionSuccess() {
		String transactionId = "Vodafone999";
		CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
		if(subscribedData != null) {
			Map<String,String> params=new HashMap<String, String>();
			params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);
			params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);
			params.put(Analytics.PAY_CONTENT_PRICE, mSelectedPriceItem.price+"");
			params.put(Analytics.PAY_PURCHASE_TYPE, mSelectedPackageItem.commercialModel); //Rental or buy
			params.put(Analytics.PAYMENT_METHOD, mSelectedPriceItem.paymentChannel); //cc or dc
			params.put(Analytics.CONTENT_QUALITY, mSelectedPackageItem.contentType); //SD or HD
			String ctype2 = Analytics.movieOrLivetv(subscribedData.generalInfo.type); //movie or livetv
			String event = Analytics.EVENT_PAID_FOR_CONTENT;
			Analytics.trackEvent(event,params);
			Analytics.trackCharge(mSelectedPriceItem.price);
			if(Analytics.CONSTANT_LIVETV.equals(ctype2)) {
				Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_PURCHASED_FOR,mSelectedPriceItem.price);
			}
			if(Analytics.CONSTANT_MOVIES.equals(ctype2)) {
				Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_MOVIES_PURCHASED_FOR,mSelectedPriceItem.price);
			}
			Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,mSelectedPriceItem.price);
			Analytics.createTransactionGA(transactionId, mSelectedPackageItem.commercialModel,new Double(mSelectedPriceItem.price), 0.0,0.0);
			Analytics.createItemGA(transactionId, subscribedData.generalInfo.title,subscribedData._id, ctype2, new Double(mSelectedPriceItem.price), 1L);
		}
	}
	
	//invoked only for operator billing.not for webbased-subscription
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
				
				mixPanelSubscriptionSuccess();
				
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
		String requestUrl = "";
		if(couponCode.length()<1)
			requestUrl = ConsumerApi.getSusbcriptionRequest(mSelectedPriceItem.paymentChannel, mSelectedPackageItem.packageId);
		else
			requestUrl = ConsumerApi.getSusbcriptionRequest(mSelectedPriceItem.paymentChannel, mSelectedPackageItem.packageId,couponCode);
		Intent i = new Intent(mContext,SubscriptionView.class);
		CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
		String commercialModel = mSelectedPackageItem.commercialModel; //Rental or Buy
		String ctype = Analytics.movieOrLivetv(mSelectedPackageItem.contentType); //Movie or LiveTv
		String paymentMode = mSelectedPriceItem.name; //creditcard or debitcard etc
		Bundle b = new Bundle();
		String contentType =  mSelectedPackageItem.contentType;
		b.putString("url", requestUrl);
		b.putString("contentname", subscribedData.generalInfo.title);
		b.putString("contentid", subscribedData.generalInfo._id);
		b.putDouble("contentprice", mSelectedPriceItem.price);
		b.putString("commercialModel", commercialModel); //Rental or Buy
		b.putString("paymentMode", paymentMode); 
		b.putString("contentType", contentType);//SD or HD
		b.putString("ctype", ctype);//LiveTv or Movie
		b.putString("packageId", mSelectedPackageItem.packageId); 
		Analytics.mixPanelPaymentOptionsSelected(subscribedData.generalInfo._id, subscribedData.generalInfo.title, paymentMode, mSelectedPriceItem.price+"");
		String cCode = getCouponCode(); //couponCode
		if(cCode != null && cCode.length() > 0  ) {
			b.putString("couponCode", cCode);
			b.putDouble("priceAfterCoupon", Analytics.priceTobecharged);
		}
		//Analytics.priceTobecharged = 0.0;
		i.putExtras(b);
		//sendMixPanelMessage();
		((Activity) mContext).startActivityForResult(i, ConsumerApi.SUBSCRIPTIONREQUEST);
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
