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
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
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
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.AlertDialogUtil;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FetchCardField;
import com.apalya.myplex.utils.FetchCardField.FetchComplete;
import com.apalya.myplex.utils.Util;
import com.flurry.android.FlurryAgent;

public class SubscriptionView extends Activity implements AlertDialogUtil.NoticeDialogListener {
	private static final String TAG="SubscriptionView";
	WebView mWebView= null;
	FbWebViewClient fbwebviewclient= null;
	private String callbackUrl = new String();
	private ProgressDialog mProgressDialog = null;
	private String url;
	public String status;
	
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
		} catch (Exception e) {
			// TODO: handle exception
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
			dofinish(ConsumerApi.SUBSCRIPTIONERROR);
		}
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FlurryAgent.onStartSession(this, "X6WWX57TJQM54CVZRB3K");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		FlurryAgent.onEndSession(this);
		super.onStop();
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
					
					/*
					Map<String,String> params=new HashMap<String, String>();
					params.put("CardId",subscribedData._id);
					params.put("CardName", subscribedData.generalInfo.title);
					params.put("Status", "Purchase Success");
					Analytics.trackEvent(Analytics.PackagesPurchase,params);
					*/	
					//???
					Map<String,String> params=new HashMap<String, String>();
					params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);
					params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);
					params.put(Analytics.PAY_STATUS_PROPERTY, Analytics.PAY_CONTENT_STATUS_TYPES.Success.toString());
					Analytics.trackEvent(Analytics.EVENT_PAY,params);
					
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
			closeSession(response);
		}
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
					
					if(paramMap.containsKey(statusString))
						status = paramMap.get(statusString);
					if(paramMap.containsKey(messageString))
						message = paramMap.get(messageString);
					
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
		super.onBackPressed();
	}
	
	public void showProgressBar(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(this,"", "Loading...", true,false);
	}
	public void dismissProgressBar(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
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

