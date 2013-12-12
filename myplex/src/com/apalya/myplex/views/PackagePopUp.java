package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.SignUpActivity;
import com.apalya.myplex.SubscriptionView;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataCertifiedRatingsItem;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardDataPromotionDetailsItem;
import com.apalya.myplex.data.MsisdnData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.Blur;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MsisdnRetrivalEngine;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.SubcriptionEngine;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.Blur.BlurResponse;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.flurry.android.monolithic.sdk.impl.ca;

public class PackagePopUp {
	private String TAG  = "PackagePopUp";
	private PopupWindow mFilterMenuPopupWindow = null;
	private List<PopupWindow> mFilterMenuPopupWindowList = new ArrayList<PopupWindow>();
	private Context mContext;
	private LayoutInflater mInflater;
	private View mBackground;
	private RelativeLayout mPopupBackground;
	private SubcriptionEngine mSubscriptionEngine;
	private MsisdnRetrivalEngine mMsisdnRetrivalEngine;
	public PackagePopUp(Context cxt,View background){
		this.mContext = cxt;
		this.mBackground = background;
		this.mInflater = LayoutInflater.from(mContext);
		mSubscriptionEngine = new SubcriptionEngine(mContext);
		mMsisdnRetrivalEngine = new MsisdnRetrivalEngine(mContext);
		String email=myplexapplication.getUserProfileInstance().getUserEmail();
		if(email.equalsIgnoreCase("NA") || email.equalsIgnoreCase(""))
		{	
			final String account=Util.getGoogleAccountName(mContext);
			String password="myplexnew";
			Map<String, String> params = new HashMap<String, String>();
			params.put("email",account);
			params.put("password", password);
			params.put("password2", password);
			params.put("profile", "work");
			params.put("clientKey",myplexapplication.getDevDetailsInstance().getClientKey());
			RegisterUserReq(mContext.getString(R.string.signuppath), params);
		}
	}


	private void dismissFilterMenuPopupWindow() {
		if (mFilterMenuPopupWindow != null) {
			mFilterMenuPopupWindowList.remove(mFilterMenuPopupWindow);
			mFilterMenuPopupWindow.dismiss();
			mFilterMenuPopupWindow = null;
		}
	}
	private Bitmap mOrginalBitmap;
	private Blur mBlurEngine;
	private void addBlur(){
		if(mBackground == null){return;}
		if(mPopupBackground == null){return;}
		try {
			mBackground.setDrawingCacheEnabled(true);
			mOrginalBitmap = mBackground.getDrawingCache();
			if(mBlurEngine != null){
				mBlurEngine.abort();
			}
			mBlurEngine = new Blur();
			mBlurEngine.fastblur(mContext, mOrginalBitmap, 12, new BlurResponse() {

				@Override
				public void BlurredBitmap(Bitmap b) {
					mOrginalBitmap.recycle();
					mOrginalBitmap  = null;
					Drawable d = new BitmapDrawable(b); 
					mPopupBackground.setBackgroundDrawable(d);
					mBackground.setDrawingCacheEnabled(false);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void showPopup(View v,View anchorView) {
		dismissFilterMenuPopupWindow();
		mFilterMenuPopupWindow = new PopupWindow(v,LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		mFilterMenuPopupWindowList.add(mFilterMenuPopupWindow);
		mFilterMenuPopupWindow.setOutsideTouchable(true);
		mFilterMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mFilterMenuPopupWindow.showAsDropDown(anchorView);
	}
	public void showPackDialog(CardData data,View anchorView) {
		View v = mInflater.inflate(R.layout.purchasepopup,null);
		mPopupBackground = (RelativeLayout)v;
		fillPackData(data,v);
		LinearLayout susblayout = (LinearLayout) v.findViewById(R.id.purchasepopup_packslayout);
		addBlur();
		addPack(data,susblayout);
		Util.showAdultToast(mContext.getString(R.string.adultwarning),data,mContext);
		showPopup(v,anchorView);
	}
	
	private void addPack(CardData data,LinearLayout parentlayout) {
		if(data.packages == null){return;}
		for(CardDataPackages packageitem:data.packages){
			if(packageitem.priceDetails != null && packageitem.priceDetails.size() > 0){
				if(packageitem.priceDetails.size() == 1 && packageitem.priceDetails.get(0)!=null && packageitem.priceDetails.get(0).paymentChannel.equalsIgnoreCase("INAPP"))
				{
					Log.d(TAG, "not filling inapp item");
					continue;
				}
				createPackItem(packageitem.priceDetails.get(0),packageitem,parentlayout);
			}
		}
	}
	private LinearLayout mLastPaymentModeLayout;
	private OnClickListener mPackClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {

			if(v.getTag() instanceof LinearLayout){
				if(mLastPaymentModeLayout != null){
					mLastPaymentModeLayout.removeAllViews();
				}
				LinearLayout subLayout = (LinearLayout)v.getTag();if(subLayout == null){return;}
				subLayout.setOrientation(LinearLayout.VERTICAL);
				mLastPaymentModeLayout = new LinearLayout(mContext);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
				params.leftMargin = ((int)mContext.getResources().getDimension(R.dimen.margin_gap_36));
				params.topMargin = ((int)mContext.getResources().getDimension(R.dimen.margin_gap_8));
				mLastPaymentModeLayout.setLayoutParams(params);
				mLastPaymentModeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
				mLastPaymentModeLayout.setOrientation(LinearLayout.VERTICAL);

				LayoutTransition transition = new LayoutTransition();
				transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
				mLastPaymentModeLayout.setLayoutTransition(transition);


				subLayout.addView(mLastPaymentModeLayout);

				deSelectViews();
				v.setAlpha(1.0f);
				
				CardDataPackages packageitem = (CardDataPackages)subLayout.getTag();
				if(packageitem == null){return;}
				if(packageitem.priceDetails == null){return;}
				int count = 0;
				for(CardDataPackagePriceDetailsItem priceItem:packageitem.priceDetails){
					if(count == 0){
						TextView heading = (TextView)mInflater.inflate(R.layout.pricepopmodeheading,null);
						heading.setTypeface(FontUtil.Roboto_Medium);
						mLastPaymentModeLayout.addView(heading);
					}
					MsisdnData msisdnData = (MsisdnData) Util.loadObject(myplexapplication.getApplicationConfig().msisdnPath);
					if(msisdnData != null && priceItem.paymentChannel.equalsIgnoreCase("OP") && (!priceItem.name.equalsIgnoreCase(msisdnData.operator)))
					{
						Log.i(TAG, priceItem.name+ "==" +msisdnData.operator);
						continue;
					}
					if(priceItem.paymentChannel.equalsIgnoreCase("INAPP"))
					{
						continue;
					}
					View paymentModeItem = mInflater.inflate(R.layout.paymentmodeitemlayout,null);
					
					final RadioButton paymentModeText = (RadioButton)paymentModeItem.findViewById(R.id.paymentmodetext);
					paymentModeText.setTypeface(FontUtil.Roboto_Medium);
					paymentModeText.setText(priceItem.name);
					mLastPaymentModeLayout.addView(paymentModeItem);
					Util.showFeedback(paymentModeItem);
					paymentModeText.setId(count);
					count++;
					paymentModeText.setTag(packageitem);
					paymentModeText.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							try {
								
								String email = myplexapplication.getUserProfileInstance().getUserEmail();
								if(email.equalsIgnoreCase("NA") || email.equalsIgnoreCase(""))
								{
									Util.showToast(mContext, "please login to subcribe", Util.TOAST_TYPE_INFO);
									return;
								}
								
								if (arg0.getTag() instanceof CardDataPackages) {
									CardDataPackages packageitem = (CardDataPackages) arg0.getTag();
									int id = arg0.getId();
									mSubscriptionEngine.doSubscription(packageitem, id);
									paymentModeText.setChecked(false);
									dismissFilterMenuPopupWindow();
								}
							}catch (Exception e) {
									// TODO: handle exception
							}
						}
					});
				}
			}
		}
	};
	private void deSelectViews(){
		for(View v:mPackViewList){
			v.setAlpha(0.5f);
		}
	}
	List<View> mPackViewList = new ArrayList<View>();
	private void createPackItem(CardDataPackagePriceDetailsItem priceDetailItem,CardDataPackages packageitem,LinearLayout parentLayout){

		LinearLayout packLayout = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		packLayout.setLayoutParams(params);
		packLayout.setOrientation(LinearLayout.HORIZONTAL);
		packLayout.setTag(packageitem);
		LayoutTransition transition = new LayoutTransition();
		transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
		packLayout.setLayoutTransition(transition);

		View v = mInflater.inflate(R.layout.purchasepackitem, null);
		LinearLayout promotionalLayout = (LinearLayout)v.findViewById(R.id.purchasepackItem1_offer);
		if(packageitem.promotionDetails == null ){ promotionalLayout.setVisibility(View.INVISIBLE);}
		if(packageitem.promotionDetails == null || packageitem.promotionDetails.size() == 0){ 
			promotionalLayout.setVisibility(View.INVISIBLE);
			}
		else{
			CardDataPromotionDetailsItem  promotionItem = packageitem.promotionDetails.get(0); 
			if(promotionItem.amount != null){
				promotionalLayout.setVisibility(View.VISIBLE);
				int promotionAmount = 0;
				try {
					promotionAmount = (int) Float.parseFloat(promotionItem.amount);
				} catch (Exception e) {
					// TODO: handle exception
				}
				promotionAmount = (int) ((promotionAmount * priceDetailItem.price) / 100);
				TextView offerAmount = (TextView)v.findViewById(R.id.purchasepackItem1_offer_text);
				offerAmount.setTypeface(FontUtil.Roboto_Medium);
				offerAmount.setText(" "+promotionAmount+"%");
			}
		}

		TextView amount = (TextView)v.findViewById(R.id.purchasepackItem1_price);
		amount.setTypeface(FontUtil.Roboto_Medium);
		amount.setText(mContext.getResources().getString(R.string.price_rupeecode)+" "+priceDetailItem.price);
		TextView quality = (TextView)v.findViewById(R.id.purchasepackItem1_quality);
		quality.setTypeface(FontUtil.Roboto_Medium);
		quality.setText(packageitem.contentType);
		TextView type = (TextView)v.findViewById(R.id.purchasepackItem1_type);
		type.setTypeface(FontUtil.Roboto_Medium);
		type.setText(packageitem.commercialModel);
		addSpace(parentLayout, (int)mContext.getResources().getDimension(R.dimen.margin_gap_16));
		v.setTag(packLayout);
		v.setOnClickListener(mPackClickListener);
		mPackViewList.add(v);
		packLayout.addView(v);
		parentLayout.addView(packLayout);
	}
	private void addSpace(ViewGroup v,int space){
		Space gap = new Space(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,space);
		gap.setLayoutParams(params);
		v.addView(gap);
	}
	private void fillPackData(CardData data,View v){
		TextView title = (TextView)v.findViewById(R.id.purchasepopup_contentname);
		title.setTypeface(FontUtil.Roboto_Light);
		if(data.generalInfo != null){
			title.setText(data.generalInfo.title.toLowerCase());
		}
		RatingBar rating = (RatingBar)v.findViewById(R.id.purchasepopup_setRating);
		if(data.userReviews != null){
			rating.setRating(data.userReviews.averageRating);
		}
		TextView parentRating = (TextView)v.findViewById(R.id.purchasepopup_parentalRating);
		parentRating.setTypeface(FontUtil.Roboto_Medium);
		if(data.content != null && data.content.certifiedRatings != null){
			for(CardDataCertifiedRatingsItem item:data.content.certifiedRatings.values){
				parentRating.setText(item.rating);
				break;
			}
		}
		TextView duration = (TextView)v.findViewById(R.id.purchasepopup_duration);
		duration.setTypeface(FontUtil.Roboto_Medium);
		if(data.content != null){
			duration.setText(data.content.duration);
		}
		TextView releaseDate = (TextView)v.findViewById(R.id.purchasepopup_releaseDate);
		releaseDate.setTypeface(FontUtil.Roboto_Medium);
		if(data.content != null){
			releaseDate.setText(data.content.releaseDate);
		}
		TextView description = (TextView)v.findViewById(R.id.purchasepopup_packsDescription);
		description.setTypeface(FontUtil.Roboto_Regular);
		if(data.generalInfo != null){
			description.setText(data.generalInfo.description);
		}
	}
	private void RegisterUserReq(String contextPath, final Map<String,String> bodyParams) {

		Map<String,String> attribs=new HashMap<String, String>();
		attribs.put("Duration", "");
		Analytics.trackEvent(Analytics.loginSignUp,attribs,true);
		
		RequestQueue queue = MyVolley.getRequestQueue();

		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				RegisterUserSuccessListener(),
				RegisterUserErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		myReq.setShouldCache(false);
		queue.add(myReq);
	}
	protected Listener<String> RegisterUserSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				
				Analytics.endTimedEvent(Analytics.loginSignUp);
				
				Log.d(TAG,"Response: "+response);
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Success");
						Analytics.trackEvent(Analytics.loginSignUp,attribs);
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "########################################################");
						Log.d(TAG, "---------------------------------------------------------");

						final String account=Util.getGoogleAccountName(mContext);
						
						myplexapplication.getUserProfileInstance().setName(account);

						myplexapplication.getUserProfileInstance().setUserEmail(account);
						
						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.userprofilename),account);
						
						Crashlytics.setUserEmail(account);
						String userIdSha1=Util.sha1Hash(account);
						FlurryAgent.setUserId(userIdSha1);
						Crashlytics.setUserName(userIdSha1);
						Crashlytics.setUserIdentifier(userIdSha1);
						
						//						Util.launchActivity(MainActivity.class,SignUpActivity.this , null);
					}
					else
					{
						Map<String,String> attribs=new HashMap<String, String>();
						attribs.put("Status", "Failed");
						attribs.put("Msg", jsonResponse.getString("code"));
						Analytics.trackEvent(Analytics.loginSignUp,attribs);
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
	protected ErrorListener RegisterUserErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Analytics.endTimedEvent(Analytics.loginSignUp);
				Map<String,String> attribs=new HashMap<String, String>();
				attribs.put("Status", "Failed");
				attribs.put("Msg", error.toString());
				Analytics.trackEvent(Analytics.loginSignUp,attribs);
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}
}
