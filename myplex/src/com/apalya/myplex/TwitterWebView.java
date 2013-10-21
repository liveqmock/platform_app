package com.apalya.myplex;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.apalya.myplex.utils.Twitter11;

public class TwitterWebView extends Activity {
	private static final String TAG="TwitterWebView";
	private String CALLBACK_URL;
	WebView mWebView= null;
	FbWebViewClient fbwebviewclient= null;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Uri uri= getIntent().getData();
		CALLBACK_URL= getIntent().getStringExtra("callback");
		setContentView(R.layout.layout_webview);
		mWebView= (WebView)findViewById(R.id.webview);
		try{		
			setUpWebView(uri);
		}catch(Exception e){
			e.printStackTrace();
			dofinish("Twitter error: "+e.getMessage());
		}
	}
	private void dofinish(String msg){
		setResult(RESULT_OK, new Intent().putExtra(Twitter11.COM_REPLY, msg));
		finish();
	}
	private void setUpWebView(Uri uri){
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(fbwebviewclient= new FbWebViewClient());
		WebSettings webSettings = mWebView.getSettings();
	    webSettings.setJavaScriptEnabled(true);
		mWebView.loadUrl(uri.toString());
	}
	private class FbWebViewClient extends WebViewClient {
		boolean closed= false;

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i(TAG, "OVERRIDE "+closed+" "+url);
			if(!closed && url.startsWith(CALLBACK_URL)){
				Intent i= new Intent();
				i.setData(Uri.parse(url));
				setResult(url.contains("?denied=")? RESULT_CANCELED : RESULT_OK, i);
				finish();
				return true;
			}
			return false;
		}
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.e(TAG, "ONRECEIVEDERROR "+failingUrl + " " +description);
			closed= true;
			view.loadData("  ", "text/plain", "utf-8");
			dofinish(description);
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
	}
}

