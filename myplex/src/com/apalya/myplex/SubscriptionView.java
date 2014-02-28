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
import com.apalya.myplex.utils.BundleUpdateHelper;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FetchCardField;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.FetchCardField.FetchComplete;
import com.apalya.myplex.utils.Util;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

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
	//double priceTobecharged = 0.0;
	double priceTobecharged2 = 0.0;
	String packageId = null;

//	http://api.beta.myplex.in/user/v2/billing/callback/evergent/
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Analytics.createScreenGA(Analytics.SCREEN_SUBSCRIPTION_VIEW);
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
			priceTobecharged2 = b.getDouble("priceAfterCoupon");
			packageId = b.getString("packageId");

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
	
	private Map<String,String> mixpanelProperties(CardData subscribedData,Map<String,String> params){
		params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);//1
		params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);//2
		String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type); //movie or livetv //3
		params.put(Analytics.CONTENT_TYPE_PROPERTY, ctype); //4
		params.put(Analytics.PAY_PURCHASE_TYPE, commercialModel); //Rental or buy //5
		params.put(Analytics.PAYMENT_METHOD, paymentModel); //cc or dc //6
		if(Analytics.CONSTANT_LIVETV.equalsIgnoreCase(ctype)) {
			params.put(Analytics.CONTENT_QUALITY, "not applicable");
		}
		else if(Analytics.CONSTANT_MOVIES.equalsIgnoreCase(ctype)){
			params.put(Analytics.CONTENT_QUALITY, contentType); //SD or HD //7
			String str = Analytics.ANALYTICS;
			String value = commercialModel+":"+contentType;
			String key = subscribedData._id+ Analytics.ANALYTICS;
			SharedPrefUtils.writeToSharedPref(myplexapplication.getAppContext(), key, value);//storing rental/buy &SD/HD info for analytics
		}
		params.put(Analytics.USER_ID,Analytics.getUserEmail());//8
		
		//studio
		if(subscribedData.content != null && subscribedData.content.language != null && subscribedData.content.language.size()>0) {
			params.put(Analytics.LANGUAGE,subscribedData.content.language.get(0));
		}
		else {
			params.put(Analytics.LANGUAGE,Analytics.UNAVAILABLE);
		}
		params.put(Analytics.PAYMENT_METHOD, paymentModel); 
		
		return params;
	}
	
	private void mixPanelPaySuccess(CardData subscribedData) {
		if(subscribedData == null ) return;
		if(subscribedData.generalInfo == null ) return;
		String event = Analytics.EVENT_PAID_FOR_CONTENT;
		String str =  Analytics.ANALYTICS;
		String value = commercialModel+":"+contentType;
		//This data will be used to capture storing rental/buy &SD/HD info while playing video
		SharedPrefUtils.writeToSharedPref(this, "subscribedData._id"+str, value);
		
		String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type);
		
		Map<String,String> paramslive = new HashMap<String, String>();
		paramslive = mixpanelProperties(subscribedData, paramslive);
		
		if(isCouponApplied()) { //discounted through coupon
			if(priceTobecharged2 > 0) {
				paramslive.put(Analytics.COUPON_USED,"TRUE");
				paramslive.put(Analytics.PAY_CONTENT_PRICE,priceTobecharged2+""); //8
				paramslive.put(Analytics.COUPON_DISCOUNT,Analytics.couponDiscountINR+"");
				Analytics.priceTobecharged = 0;
				Analytics.couponDiscountINR = 0;
			}
			if(priceTobecharged2 == 0) {
				//subscribed free
				freeSubscription(subscribedData);
			}
		}
		else{ //regular price
			paramslive.put(Analytics.COUPON_USED,Analytics.FALSE);
			paramslive.put(Analytics.COUPON_DISCOUNT,0+"");//set to zero
			paramslive.put(Analytics.PAY_CONTENT_PRICE,contentPrice+""); //8
		}
		
		if("live tv".equals(ctype)) {
			Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_PURCHASED_FOR,priceTobecharged2); //mixpanel people
			Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged2); //mixpanel people
		} 
		else if("tvshow".equals(ctype)) {
			Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_SHOW_PURCHASED_FOR,priceTobecharged2); //mixpanel people
			Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged2); //mixpanel people
		} 
		else if("movies".equals(ctype)) {
			Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_MOVIES_PURCHASED_FOR,priceTobecharged2); //people
			Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged2); //people
		}
		Analytics.trackEvent(event,paramslive);
		Analytics.trackCharge(priceTobecharged2);
		Analytics.createTransactionGA(transactionid, paymentModel,priceTobecharged2, 0.0,0.0); //GA transaction
		Analytics.createItemGA(transactionid, contentName,packageId, ctype+" "+commercialModel, priceTobecharged2, 1L); //GA transaction */
		
	}

	//dead code can be removed
	private void mixPanelPaySuccess2(CardData subscribedData) {
			if(subscribedData == null ) return;
			if(subscribedData.generalInfo == null ) return;
			
			String str =Analytics.ANALYTICS;
			String value = commercialModel+":"+contentType;
			//This data will be used to capture storing rental/buy &SD/HD info while playing video
			SharedPrefUtils.writeToSharedPref(this, "subscribedData._id"+str, value);
			
			String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type);
						
			if("live tv".equals(ctype)) {
				String eventlive = Analytics.EVENT_PAID_FOR_CONTENT;
				Map<String,String> paramslive = new HashMap<String, String>();
				paramslive = mixpanelProperties(subscribedData, paramslive);
				
				if(isCouponApplied()) { //discounted through coupon
					if(priceTobecharged2 > 0) {
						paramslive.put(Analytics.COUPON_USED,"TRUE");
						paramslive.put(Analytics.PAY_CONTENT_PRICE,priceTobecharged2+""); //8
						paramslive.put(Analytics.COUPON_DISCOUNT,Analytics.couponDiscountINR+"");
						Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_PURCHASED_FOR,priceTobecharged2); //mixpanel people
						Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged2); //mixpanel people
						Analytics.trackEvent(eventlive,paramslive);
						Analytics.trackCharge(priceTobecharged2);
						Analytics.createTransactionGA(transactionid, paymentModel,priceTobecharged2, 0.0,0.0); //GA transaction
						Analytics.createItemGA(transactionid, contentName,packageId, ctype+" "+commercialModel, priceTobecharged2, 1L); //GA transaction
						Analytics.priceTobecharged = 0;
						Analytics.couponDiscountINR = 0;
						return;
					}
					if(priceTobecharged2 == 0) {
						//subscribed free
						freeSubscription(subscribedData);
					}
				}
				else{ //regular price
					paramslive.put(Analytics.COUPON_USED,"FALSE");
					paramslive.put(Analytics.COUPON_DISCOUNT,0+"");//set to zero
					paramslive.put(Analytics.PAY_CONTENT_PRICE,contentPrice+""); //8
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_PURCHASED_FOR,contentPrice); //mixpanel people
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,contentPrice); //mixpanel people
					Analytics.trackEvent(eventlive,paramslive);
					Analytics.trackCharge(priceTobecharged2);
					Analytics.createTransactionGA(transactionid, paymentModel,contentPrice, 0.0,0.0); //GA transaction
					Analytics.createItemGA(transactionid, contentName,packageId, ctype+" "+commercialModel, contentPrice, 1L); //GA transaction
					return;
				}
				
				}//live-tv end
			
			if("tvshow".equals(ctype)) {
				String eventlive = Analytics.EVENT_PAID_FOR_CONTENT;
				Map<String,String> paramslive = new HashMap<String, String>();
				paramslive = mixpanelProperties(subscribedData, paramslive);
				
				if(isCouponApplied()) { //discounted through coupon
					if(priceTobecharged2 > 0) {
						paramslive.put(Analytics.COUPON_USED,"TRUE");
						paramslive.put(Analytics.PAY_CONTENT_PRICE,priceTobecharged2+""); //8
						paramslive.put(Analytics.COUPON_DISCOUNT,Analytics.couponDiscountINR+"");
						Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_SHOW_PURCHASED_FOR,priceTobecharged2); //mixpanel people
						Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged2); //mixpanel people
						Analytics.trackEvent(eventlive,paramslive);
						Analytics.trackCharge(priceTobecharged2);
						Analytics.createTransactionGA(transactionid, paymentModel,priceTobecharged2, 0.0,0.0); //GA transaction
						Analytics.createItemGA(transactionid, contentName,packageId, ctype+" "+commercialModel, priceTobecharged2, 1L); //GA transaction
						Analytics.priceTobecharged = 0;
						Analytics.couponDiscountINR = 0;
						return;
					}
					if(priceTobecharged2 == 0) {
						//subscribed free
						freeSubscription(subscribedData);
					}
				}
				else{ //regular price
					paramslive.put(Analytics.COUPON_USED,"FALSE");
					paramslive.put(Analytics.COUPON_DISCOUNT,0+"");//set to zero
					paramslive.put(Analytics.PAY_CONTENT_PRICE,contentPrice+""); //8
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_LIVETV_SHOW_PURCHASED_FOR,contentPrice); //mixpanel people
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,contentPrice); //mixpanel people
					Analytics.trackEvent(eventlive,paramslive);
					Analytics.trackCharge(priceTobecharged2);
					Analytics.createTransactionGA(transactionid, paymentModel,contentPrice, 0.0,0.0); //GA transaction
					Analytics.createItemGA(transactionid, contentName,packageId, ctype+" "+commercialModel, contentPrice, 1L); //GA transaction
					return;
				}
				
				}//live-tv end

			if("movies".equals(ctype)) {
				String event = Analytics.EVENT_PAID_FOR_CONTENT;
				Map<String,String> paramsMovies = new HashMap<String, String>();
				paramsMovies = mixpanelProperties(subscribedData, paramsMovies); //common properties
				
				if(couponCode != null && couponCode.length() > 0) { ////discounted through coupon
				
					if(priceTobecharged2 > 0) {
						paramsMovies.put(Analytics.COUPON_USED,"TRUE");
						paramsMovies.put(Analytics.COUPON_DISCOUNT,Analytics.couponDiscountINR+"");
						paramsMovies.put(Analytics.PAY_CONTENT_PRICE,priceTobecharged2+""); //8
						Analytics.trackEvent(event,paramsMovies);
						Analytics.trackCharge(priceTobecharged2);
						Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_MOVIES_PURCHASED_FOR,priceTobecharged2); //people
						Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,priceTobecharged2); //people
						Analytics.createTransactionGA(transactionid, paymentModel,priceTobecharged2, 0.0,0.0); //GA transaction
						Analytics.createItemGA(transactionid, contentName,packageId, ctype+" "+commercialModel, priceTobecharged2, 1L);//GA transaction
						Analytics.priceTobecharged = 0;
						Analytics.couponDiscountINR = 0;
						return;
					}	
					if(priceTobecharged2 == 0) {
						//event = Analytics.EVENT_SUBSCRIBED_FREE + Analytics.EMPTY_SPACE+subscribedData.generalInfo.title;
						//Analytics.trackEvent(event,params);
						freeSubscription(subscribedData);
						return;
					}
				}
				else { //regular price
					paramsMovies.put(Analytics.COUPON_USED,"FALSE"); //regular price
					paramsMovies.put(Analytics.COUPON_DISCOUNT,0.0+"");
					paramsMovies.put(Analytics.PAY_CONTENT_PRICE, contentPrice.toString()); //8
					Analytics.trackEvent(event,paramsMovies);
					Analytics.trackCharge(contentPrice);
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_MOVIES_PURCHASED_FOR,contentPrice);
					Analytics.getMixpanelPeople().increment(Analytics.PEOPLE_TOTAL_PURCHASES,contentPrice);
					Analytics.createTransactionGA(transactionid, paymentModel,contentPrice, 0.0,0.0);
					Analytics.createItemGA(transactionid, contentName,packageId, ctype+" "+commercialModel, contentPrice, 1L);//packageid as sku
					return;
				}
				
				}//end-of-movies
			}
				
	private void freeSubscription(CardData subscribedData) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(Analytics.CONTENT_ID_PROPERTY, subscribedData._id);//1
		params.put(Analytics.CONTENT_NAME_PROPERTY, subscribedData.generalInfo.title);//2
		String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type); //movie or livetv 
		params.put(Analytics.CONTENT_TYPE_PROPERTY, ctype); //4
		params.put(Analytics.PAY_PURCHASE_TYPE, commercialModel); //Rental or buy //5
		params.put(Analytics.CONTENT_QUALITY, contentType); //SD or HD //7
		params.put(Analytics.USER_ID,Analytics.getUserEmail());//8
		//studio
		//language
		String event = Analytics.EVENT_SUBSCRIBED_FREE;
		Analytics.trackEvent(event, params);
		MixpanelAPI.People people = Analytics.getMixpanelPeople();
		
		if("live tv".equals(ctype)) {
			people.set(Analytics.PEOPLE_FREE_TV_SUBSCRIPTIONS, 1);
		}else if("tvshow".equals(ctype)) {
			people.set(Analytics.PEOPLE_FREE_TV_SHOW_SUBSCRIPTIONS, 1);
		}
		else if("movies".equals(ctype)) {
			if("Rental".equalsIgnoreCase(commercialModel)) {
				people.set(Analytics.PEOPLE_FREE_MOVIE_RENTALS, 1);
			}
			else if("Buy".equalsIgnoreCase(commercialModel)) {
				people.set(Analytics.PEOPLE_FREE_DOWNLOADS_TO_OWN, 1);
			}
			
		}
	}
	
	private boolean isCouponApplied() {
		if(couponCode != null && couponCode.length() > 0) return true;
		return false;
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
					
					String requestUrl = ConsumerApi.getBundleUrl(subscribedData._id);
					/*BundleUpdateHelper helper = new BundleUpdateHelper(requestUrl, dataToSave);
					helper.getConnectIds(new InsertionResult() {						
						@Override
						public void updateComplete(Boolean updateStatus) {							
							closeSession(response);
							Util.showToast(SubscriptionView.this, "Subscription Info updated",Util.TOAST_TYPE_INFO);
//							Toast.makeText(SubscriptionView.this, "Subscription Info updated", Toast.LENGTH_SHORT).show();
						}
					});
					helper.updatecurrentUserData();*/					
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
		String ctype = Analytics.movieOrLivetv(subscribedData.generalInfo.type); 
		params.put(Analytics.CONTENT_TYPE_PROPERTY, ctype);//movie or livetv
		params.put(Analytics.PAY_PURCHASE_TYPE, commercialModel); //Rental or buy
		params.put(Analytics.CONTENT_QUALITY, contentType); //SD or HD
		params.put(Analytics.PAY_CONTENT_PRICE, contentPrice.toString());
		//params.put(Analytics.PAYMENT_METHOD, paymentModel); //cc or dc
		String event = Analytics.EVENT_SUBSCRIPTION_FAILURE;
		params.put(Analytics.REASON_FAILURE,error);
		params.put(Analytics.USER_ID,Analytics.getUserEmail());
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

