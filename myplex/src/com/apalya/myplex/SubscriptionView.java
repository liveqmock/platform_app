package com.apalya.myplex;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.apalya.myplex.cache.InsertionResult;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.UserProfile;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.AlertDialogUtil;
import com.apalya.myplex.utils.AlertDialogUtil.NoticeDialogListener;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FetchCardField;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.FetchCardField.FetchComplete;
import com.apalya.myplex.utils.Util;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class SubscriptionView extends Activity implements AlertDialogUtil.NoticeDialogListener {
	private static final String TAG="SubscriptionView";
	WebView mWebView= null;
	FbWebViewClient fbwebviewclient= null;
	private String callbackUrl = new String();
	private ProgressDialog mProgressDialog = null;

	private String url;
	public String status;
	private boolean isProgressDialogCancelable=false;
	

	String contentName = null;
	String contentType = null; //SD or HD
	String contentId = null;
	Double contentPrice = null;
	String ctype = null;
	String commercialModel = null;
	String paymentModel = null;
	String transactionid = null;
	String couponCode = null;
	double priceTobecharged = 0.0;

//	http://api.beta.myplex.in/user/v2/billing/callback/evergent/
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(getResources().getBoolean(R.bool.isTablet))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		url = new String();
		try {
			Bundle b = this.getIntent().getExtras();
			url = b.getString("url");

			isProgressDialogCancelable= b.getBoolean("isProgressDialogCancelable", false);

			contentName = b.getString("contentname");
			contentId = b.getString("contentid");
			contentPrice = b.getDouble("contentprice");
			ctype = b.getString("ctype");
			commercialModel = b.getString("commercialModel");
			paymentModel =  b.getString("paymentMode");
			contentType = b.getString("contentType");
			couponCode = b.getString("couponCode");
			priceTobecharged = b.getDouble("priceAfterCoupon");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		callbackUrl = ConsumerApi.SCHEME + ConsumerApi.DOMAIN
				+ ConsumerApi.SLASH + ConsumerApi.USER_CONTEXT
				+ ConsumerApi.SLASH + ConsumerApi.BILLING_TAG
				+ "/callback/evergent/";
		setContentView(R.layout.layout_webview);
		mWebView= (WebView)findViewById(R.id.webview);
		
		try{		
			setUpWebView(url);
		}catch(Exception e){
			e.printStackTrace();
			dofinish(ConsumerApi.SUBSCRIPTIONERROR);
		}
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		super.onStop();
	}
	
	private void mixPanelPaySuccess(CardData subscribedData) {
			if(subscribedData == null ) return;
			if(subscribedData.generalInfo == null ) return;
			Map<String,String> params=new HashMap<String, String>();
			String str = "analytics";
			String value = commercialModel+":"+contentType;
			SharedPrefUtils.writeToSharedPref(this, "subscribedData._id"+str, value);//storing rental/buy &SD/HD info for analytics
			params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);//1
			params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);//2
			String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type); //movie or livetv //3
			params.put(Analytics.CONTENT_TYPE_PROPERTY, ctype); //4
			params.put(Analytics.PAY_PURCHASE_TYPE, commercialModel); //Rental or buy //5
			params.put(Analytics.PAYMENT_METHOD, paymentModel); //cc or dc //6
			params.put(Analytics.CONTENT_QUALITY, contentType); //SD or HD //7
			params.put(Analytics.USER_ID,Analytics.getUserEmail());//8
			String event = Analytics.EVENT_PAID_FOR_CONTENT;
			//String event = Analytics.EVENT_PAID_FOR + Analytics.EMPTY_SPACE+subscribedData.generalInfo.title;
			if("live tv".equals(ctype)) {
				Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_PURCHASED_FOR,contentPrice);
				Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,contentPrice);
				Analytics.trackEvent(event,params);
				Analytics.createTransactionGA(transactionid, paymentModel,contentPrice, 0.0,0.0);
				Analytics.createItemGA(transactionid, contentName,contentId, ctype, contentPrice, 1L);
				return;
			}
			
			if(couponCode != null && couponCode.length() > 0) { //coupon is applied
				params.put(Analytics.COUPON_USED,"TRUE");
				params.put(Analytics.COUPON_DISCOUNT,Analytics.couponDiscountINR+"");
				if(priceTobecharged > 0) {
					params.put(Analytics.PAY_CONTENT_PRICE,priceTobecharged+""); //8
					Analytics.trackEvent(event,params);
					Analytics.trackCharge(priceTobecharged);
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_MOVIES_PURCHASED_FOR,priceTobecharged);
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged);
					Analytics.createTransactionGA(transactionid, paymentModel,contentPrice, 0.0,0.0);
					Analytics.createItemGA(transactionid, contentName,contentId, ctype, contentPrice, 1L);
					return;
				}	
				if(priceTobecharged == 0) {
					event = Analytics.EVENT_SUBSCRIBED_FREE + Analytics.EMPTY_SPACE+subscribedData.generalInfo.title;
					Analytics.trackEvent(event,params);
					return;
				}
			}
			else { //regular price
				params.put(Analytics.COUPON_USED,"FALSE"); //regular price
				params.put(Analytics.COUPON_DISCOUNT,0.0+"");
				params.put(Analytics.PAY_CONTENT_PRICE, contentPrice.toString()); //8
				Analytics.trackEvent(event,params);
				Analytics.trackCharge(contentPrice);
				Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_MOVIES_PURCHASED_FOR,contentPrice);
				Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged);
				Analytics.createTransactionGA(transactionid, paymentModel,contentPrice, 0.0,0.0);
				Analytics.createItemGA(transactionid, contentName,contentId, ctype, contentPrice, 1L);
			}
				
	}
	
	private void dofinish(final int response){
		if(response == ConsumerApi.SUBSCRIPTIONSUCCESS){
			Util.showToast(SubscriptionView.this, "Subscription: Success",Util.TOAST_TYPE_INFO);
//			Toast.makeText(SubscriptionView.this, "Subscription: Success", Toast.LENGTH_SHORT).show();
			FetchCardField fetch = new FetchCardField();
					
			
			fetch.Fetch(myplexapplication.getCardExplorerData().cardDataToSubscribe, ConsumerApi.FIELD_CURRENTUSERDATA, new FetchComplete() {
				
				@Override
				public void response(CardResponseData data) {
					if (data == null || data.results == null || data.results.size() == 0) {
						closeSession(response);
						return;
					}
					CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
					subscribedData.currentUserData =  data.results.get(0).currentUserData;
					mixPanelPaySuccess(subscribedData);					
					
					List<CardData> dataToSave = new ArrayList<CardData>();
					dataToSave.add(subscribedData);
					myplexapplication.getCacheHolder().UpdataDataAsync(dataToSave, new InsertionResult() {
						
						@Override
						public void updateComplete(Boolean updateStatus) {
							closeSession(response);
							Util.showToast(SubscriptionView.this, "Subscription Info updated",Util.TOAST_TYPE_INFO);
//							Toast.makeText(SubscriptionView.this, "Subscription Info updated", Toast.LENGTH_SHORT).show();
						}
					});					
				}
			});
		}else{
			Util.showToast(SubscriptionView.this, "Subscription: Cancelled",Util.TOAST_TYPE_ERROR);
//			Toast.makeText(SubscriptionView.this, "Subscription: Cancelled", Toast.LENGTH_SHORT).show();
			mixPanelPayFailure("Subscription: Cancelled");
			closeSession(response);
		}
	}
	
	private void mixPanelPayFailure(String error) {
		CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
		Map<String,String> params=new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);
		params.put(Analytics.PAY_CONTENT_PRICE, contentPrice.toString());
		params.put(Analytics.PAY_PURCHASE_TYPE, commercialModel); //Rental or buy
		params.put(Analytics.PAYMENT_METHOD, paymentModel); //cc or dc
		params.put(Analytics.CONTENT_QUALITY, contentType); //SD or HD
		String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type); //movie or livetv
		//String event = Analytics.EVENT_SUBSCRIPTION_FAILURE + Analytics.EMPTY_SPACE+subscribedData.generalInfo.title;
		String event = Analytics.EVENT_SUBSCRIPTION_FAILURE;
		params.put(Analytics.REASON_FAILURE,error);
		Analytics.trackEvent(event,params);
	}
	private void closeSession(int response){
		setResult(response);
		finish();
		dismissProgressBar();	
	}
	private void setUpWebView(String url){
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(fbwebviewclient= new FbWebViewClient());
		mWebView.setWebChromeClient(new CustomChromeClient());
		WebSettings webSettings = mWebView.getSettings();
	    webSettings.setJavaScriptEnabled(true);
	    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	    webSettings.setLoadsImagesAutomatically(true);
		mWebView.loadUrl(url);
	}
	private String getTokenValue(String token){
		String returnValue = new String();
		StringTokenizer subtoken = new StringTokenizer(token,"=");
		while (subtoken.hasMoreTokens()) {
			returnValue = subtoken.nextToken(); 
	    }
		return returnValue;
	}
//	status=FAILURE&message=expired+card
	private class FbWebViewClient extends WebViewClient {
		boolean closed= false;

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i(TAG, "OVERRIDE "+closed+" "+url);
			if(url.contains(callbackUrl)){
				try {
					String status = new String();
					String message = new String();
					URL aURL = new URL(url);
					String query = aURL.getQuery();
					StringTokenizer token = new StringTokenizer(query,"&");
					HashMap<String, String> paramMap = new HashMap<String, String>();
					while (token.hasMoreTokens()) {
						String tokenName = token.nextToken();
						try {
							String key = tokenName.split("=")[0];
							String value = tokenName.split("=")[1];
							paramMap.put(key, value);
						} catch (Exception e) {
							e.printStackTrace();
						}
				    }
					String statusString = "status";
					String messageString = "message";
					String transactionString = "transactionId";
					
					if(paramMap.containsKey(statusString))
						status = paramMap.get(statusString);
					if(paramMap.containsKey(messageString))
						message = paramMap.get(messageString);
					if(paramMap.containsKey(transactionString)) { //the python server must return transaction-id
						transactionid = paramMap.get(transactionString);
					}else {
						transactionid = contentName +"transactionId";
					}
					
					if(status.equalsIgnoreCase("SUCCESS")){
						dofinish(ConsumerApi.SUBSCRIPTIONSUCCESS);
					}else if(status.equalsIgnoreCase("ERR_IN_PROGRESS")){
						Log.d(TAG,"error is progress");
						AlertDialogUtil.showAlert(SubscriptionView.this,getResources().getString(R.string.transaction_server_error)
								,getResources().getString(R.string.retry)
								, getResources().getString(R.string.cancel)
								, SubscriptionView.this);
					}else{
						Util.showToast(SubscriptionView.this, "Subscription: "+ status,Util.TOAST_TYPE_ERROR);
						dofinish(ConsumerApi.SUBSCRIPTIONERROR);
					}
					
				} catch (MalformedURLException e) {
					dofinish(ConsumerApi.SUBSCRIPTIONERROR);
					mixPanelPayFailure(e.toString());
					e.printStackTrace();
				}
				return true;
			}
			view.loadUrl(url);
			return false;
		}
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.e(TAG, "ONRECEIVEDERROR "+failingUrl + " " +description);
			closed= true;
			dofinish(ConsumerApi.SUBSCRIPTIONERROR);
			mixPanelPayFailure(description);
			 dismissProgressBar();
		}
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon){
			super.onPageStarted(view, url, favicon);
			Log.d(TAG, "PageStarted " + url);
			showProgressBar();
		}
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			Log.d(TAG, "PageFinished " + url);
			 dismissProgressBar();
		}   
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		    handler.proceed(); // Ignore SSL certificate errors
		    dismissProgressBar();
		}
	}
	
	private class CustomChromeClient extends WebChromeClient {

		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				final android.webkit.JsResult result) {
			Log.d(TAG, "onJsAlert " + url);
			AlertDialog.Builder builder = new AlertDialog.Builder(
		                SubscriptionView.this);
		        builder.setMessage(message)
		                .setNeutralButton("OK", new OnClickListener() {
		                    @Override
		                    public void onClick(DialogInterface arg0, int arg1) {
		                        arg0.dismiss();
		                    }
		                }).show();
		        result.cancel();
		        return true;
		        }

		@Override
		public boolean onJsConfirm(WebView view, String url, String message,
				final JsResult result) {
			Log.d(TAG, "onJsConfirm " + url);
			AlertDialog.Builder builder = new AlertDialog.Builder(
		                SubscriptionView.this);
		        builder.setMessage(message)
		                .setNeutralButton("OK", new OnClickListener() {
		                    @Override
		                    public void onClick(DialogInterface arg0, int arg1) {
		                        arg0.dismiss();
		                    }
		                }).show();
		        result.cancel();
		        return true;
		        }

		@Override
		public boolean onJsPrompt(WebView view, String url, String message,
				String defaultValue, final JsPromptResult result) {
			Log.d(TAG, "onJsPrompt " + url);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SubscriptionView.this);
			builder.setMessage(message)
					.setNeutralButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					}).show();
			result.cancel();
			return true;
		};
	}
	
	@Override
	public void onBackPressed() {
		
		AlertDialogUtil.showAlert(SubscriptionView.this,getResources().getString(R.string.transaction_cancel_msg)
				,getResources().getString(R.string.transaction_cancel_no)
				,getResources().getString(R.string.transaction_cancel_yes)
				
				, new NoticeDialogListener() {
					
					public void onDialogOption2Click() {
					
						dofinish(ConsumerApi.SUBSCRIPTIONERROR);
					}
					
					public void onDialogOption1Click() {
					
						
					}
				});
		
		
	}
	
	public void showProgressBar(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
		}
		
		if(isProgressDialogCancelable){
			findViewById(R.id.customactionbar_progressBar).setVisibility(View.VISIBLE);
			return;
		}
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if(isProgressDialogCancelable){
					finish();
				}
				
			}
		};
		mProgressDialog = ProgressDialog.show(this,"", "Loading...", true,isProgressDialogCancelable,onCancelListener);
		mProgressDialog.setCanceledOnTouchOutside(false);
	}
	public void dismissProgressBar(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
		}
		if(isProgressDialogCancelable){
			findViewById(R.id.customactionbar_progressBar).setVisibility(View.GONE);
			return;
		}
	}
	@Override
	public void onDialogOption1Click() 
	{
		mWebView.loadUrl(url+"&force=true");
	}
	@Override
	public void onDialogOption2Click() 
	{			
		dofinish(ConsumerApi.SUBSCRIPTIONERROR);
	}

}

