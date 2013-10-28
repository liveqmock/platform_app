package com.apalya.myplex;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.widget.Toast;

import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.InsertionResult;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardResponseData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FetchCardField;
import com.apalya.myplex.utils.FetchCardField.FetchComplete;

public class SubscriptionView extends Activity {
	private static final String TAG="SubscriptionView";
	WebView mWebView= null;
	FbWebViewClient fbwebviewclient= null;
	private String callbackUrl = new String();
	private ProgressDialog mProgressDialog = null;
//	http://api.beta.myplex.in/user/v2/billing/callback/evergent/
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		String url = new String();
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
	private void dofinish(final int response){
		if(response == ConsumerApi.SUBSCRIPTIONSUCCESS){
			Toast.makeText(SubscriptionView.this, "Subscription: Success", Toast.LENGTH_SHORT).show();
			FetchCardField fetch = new FetchCardField();
			fetch.Fetch(myplexapplication.getCardExplorerData().cardDataToSubscribe, ConsumerApi.FIELD_CURRENTUSERDATA, new FetchComplete() {
				
				@Override
				public void response(CardResponseData data) {
					if(data == null){
						closeSession(response);
					}
					if(data.results == null){closeSession(response);}
					if(data.results.size() == 0){closeSession(response);}
					CardData subscribedData = myplexapplication.getCardExplorerData().cardDataToSubscribe;
					subscribedData.currentUserData =  data.results.get(0).currentUserData;
					List<CardData> dataToSave = new ArrayList<CardData>();
					dataToSave.add(subscribedData);
					myplexapplication.getCacheHolder().UpdataDataAsync(dataToSave, new InsertionResult() {
						
						@Override
						public void updateComplete(Boolean updateStatus) {
							closeSession(response);
							Toast.makeText(SubscriptionView.this, "Subscription Info updated", Toast.LENGTH_SHORT).show();
						}
					});					
				}
			});
		}else{
			Toast.makeText(SubscriptionView.this, "Subscription: Cancelled", Toast.LENGTH_SHORT).show();
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
					List<String> params = new ArrayList<String>();
					while (token.hasMoreTokens()) {
						params.add(token.nextToken());
				    }
					for(String param:params){
						if(param.equalsIgnoreCase("status")){
							status = getTokenValue(param);
						}else if(param.equalsIgnoreCase("message")){
							message = getTokenValue(param);
						}
					}
					if(status.equalsIgnoreCase("FAILURE")){
						Toast.makeText(SubscriptionView.this, "Subscription: "+ status, Toast.LENGTH_SHORT).show();
						dofinish(ConsumerApi.SUBSCRIPTIONERROR);
					}else{
						dofinish(ConsumerApi.SUBSCRIPTIONSUCCESS);
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
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(this,"", "Loading...", true,false);
	}
	public void dismissProgressBar(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
	}
}

