package com.apalya.myplex;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.apalya.myplex.utils.ConsumerApi;

public class SubscriptionView extends Activity {
	private static final String TAG="SubscriptionView";
	WebView mWebView= null;
	FbWebViewClient fbwebviewclient= null;
	private String callbackUrl = new String();
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
	private void dofinish(int response){
		setResult(response);
		finish();
	}
	private void setUpWebView(String url){
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(fbwebviewclient= new FbWebViewClient());
		WebSettings webSettings = mWebView.getSettings();
	    webSettings.setJavaScriptEnabled(true);
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
						dofinish(ConsumerApi.SUBSCRIPTIONERROR);
					}else{
						dofinish(ConsumerApi.SUBSCRIPTIONSUCCESS);
					}
					Toast.makeText(SubscriptionView.this, message, Toast.LENGTH_SHORT).show();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				dofinish(ConsumerApi.SUBSCRIPTIONSUCCESS);
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
		}
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon){
			super.onPageStarted(view, url, favicon);
			Log.d(TAG, "PageStarted " + url);
		}
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			Log.d(TAG, "PageFinished " + url);
		}   
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		    handler.proceed(); // Ignore SSL certificate errors
		}
	}
}

