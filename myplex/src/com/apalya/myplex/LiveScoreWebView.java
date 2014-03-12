package com.apalya.myplex;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.apalya.myplex.utils.AlertDialogUtil;
import com.flurry.android.FlurryAgent;

public class LiveScoreWebView extends Activity implements
		AlertDialogUtil.NoticeDialogListener {
	private WebView liveWebView;
	private String url;
	private boolean isProgressDialogCancelable;
	private String TAG = getClass().getSimpleName();
	private FbWebViewClient webviewclient;
	private ProgressDialog mProgressDialog = null;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.layout_webview);
		liveWebView = (WebView) findViewById(R.id.webview);
//		liveWebView.getSettings().setJavaScriptEnabled(true);
		url = new String();
		try {
			Bundle b = this.getIntent().getExtras();
			url = b.getString("url");
			isProgressDialogCancelable = b.getBoolean(
					"isProgressDialogCancelable", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(R.layout.ab_live_score_layout);
		progressBar = (ProgressBar) getActionBar().getCustomView().findViewById(R.id.livescore_progressBar);
		setUpWebView(url.trim());
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, "X6WWX57TJQM54CVZRB3K");
	}

	@Override
	protected void onStop() {
		FlurryAgent.onEndSession(this);
		super.onStop();
	}

	private void dofinish() {
		setResult(Activity.RESULT_OK);
		finish();
		dismissProgressBar();
	}

	private void setUpWebView(String url) {
		liveWebView.setVerticalScrollBarEnabled(false);
		liveWebView.setHorizontalScrollBarEnabled(false);
		liveWebView.setWebViewClient(webviewclient = new FbWebViewClient());
		liveWebView.setWebChromeClient(new CustomChromeClient());
		WebSettings webSettings = liveWebView.getSettings();
//		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setLoadsImagesAutomatically(true);
		liveWebView.loadUrl(url);
	}

	private class FbWebViewClient extends WebViewClient {
		boolean closed = false;

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i(TAG, "OVERRIDE " + closed + " " + url);
			view.loadUrl(url);
			return false;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.e(TAG, "ONRECEIVEDERROR " + failingUrl + " " + description);
			closed = true;
			dofinish();
			dismissProgressBar();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
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
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
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
					LiveScoreWebView.this);
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
					LiveScoreWebView.this);
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
					LiveScoreWebView.this);
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

	public void showProgressBar() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}

		if (isProgressDialogCancelable) {
			progressBar.setVisibility(View.VISIBLE);
			return;
		}
		OnCancelListener onCancelListener = new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (isProgressDialogCancelable) {
					finish();
				}

			}
		};
		mProgressDialog = ProgressDialog.show(this, "", "Loading...", true,
				isProgressDialogCancelable, onCancelListener);
		mProgressDialog.setCanceledOnTouchOutside(false);
	}

	public void dismissProgressBar() {
		progressBar.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onDialogOption1Click() {
		liveWebView.loadUrl(url + "&force=true");
	}

	@Override
	public void onDialogOption2Click() {
		dofinish();
	}

}
