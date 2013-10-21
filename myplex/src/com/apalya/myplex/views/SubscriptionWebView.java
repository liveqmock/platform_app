package com.apalya.myplex.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SubscriptionWebView extends WebView{
	private Context mContext;
	private String TAG = "SubscriptionWebView";

	public SubscriptionWebView(Context context, AttributeSet attrs,
			int defStyle, boolean privateBrowsing) {
		super(context, attrs, defStyle, privateBrowsing);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public SubscriptionWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public SubscriptionWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public SubscriptionWebView(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
	}
	public void launchSubcriptionPage(String url){
		setVerticalScrollBarEnabled(true);
		setHorizontalScrollBarEnabled(false);
		setWebViewClient(new SubWebViewClient());
		getSettings().setJavaScriptEnabled(true);
		loadUrl(url);
	}
	private class SubWebViewClient extends WebViewClient {
		boolean closed= false;

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i(TAG, "OVERRIDE "+closed+" "+url);
			view.loadUrl(url);
//			if(!closed && url.startsWith(CALLBACK_URL)){
//				Intent i= new Intent();
//				i.setData(Uri.parse(url));
//				setResult(url.contains("?denied=")? RESULT_CANCELED : RESULT_OK, i);
//				finish();
//				return true;
//			}
			return false;
		}
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.e(TAG, "ONRECEIVEDERROR "+failingUrl + " " +description);
//			closed= true;
//			view.loadData("  ", "text/plain", "utf-8");
//			dofinish(description);
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
