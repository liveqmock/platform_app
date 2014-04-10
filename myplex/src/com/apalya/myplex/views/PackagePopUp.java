package com.apalya.myplex.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataCertifiedRatingsItem;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardDataPromotionDetailsItem;
import com.apalya.myplex.data.CouponResponseData;
import com.apalya.myplex.data.MsisdnData;
import com.apalya.myplex.data.ResultsData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.Blur;
import com.apalya.myplex.utils.Blur.BlurResponse;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.MsisdnRetrivalEngine;
import com.apalya.myplex.utils.MyVolley;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.SubcriptionEngine;
import com.apalya.myplex.utils.Util;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;


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
	private Button applyCouponBtn;
	private EditText couponCodeEt;
	private String packages [] ;
//	private String couponCode;
	private ProgressDialog mProgressDialog;
	private ImageView clearCoupon;
//	private boolean isCouponcodeApplied = false;
	private HashMap<String, String> coupanCodes= new HashMap<String, String>(1);
	private static String COUPON_CODE ="";
	private ScrollView pkgScrollView;
	private RelativeLayout couponLayout;
	CardData cardDataAnalytics;
	
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
		
		//Analytics.mixPanelPaymentOptionsPresented();
	}
	
	/*private void mixPanelPaymentOptionsPresented() {
		Map<String,String> params=new HashMap<String, String>();
		int selected = myplexapplication.getCardExplorerData().currentSelectedCard;
		CardData  mData = myplexapplication.getCardExplorerData().mMasterEntries.get(selected);
		params.put(Analytics.CONTENT_ID_PROPERTY, mData._id);
		params.put(Analytics.CONTENT_NAME_PROPERTY, mData.generalInfo.title);
		Analytics.trackEvent(Analytics.EVENT_PAYMENT_OPTIONS_PRESENTED,params);
	}*/
	
	
	private void dismissFilterMenuPopupWindow() {
		if (mFilterMenuPopupWindow != null) {
			mFilterMenuPopupWindowList.remove(mFilterMenuPopupWindow);
			mFilterMenuPopupWindow.dismiss();
			mFilterMenuPopupWindow = null;
		}
	}
	private Bitmap mOrginalBitmap;
	private Blur mBlurEngine;
	private LinearLayout susblayout;
	private TextView couponMessage;
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
		pkgScrollView = (ScrollView)v.findViewById(R.id.pkg_scrollView);			
		mPopupBackground = (RelativeLayout)v;
		fillPackData(data,v);
		 susblayout = (LinearLayout) v.findViewById(R.id.purchasepopup_packslayout);
		addBlur();
		addPack(data,susblayout);
		Util.showAdultToast(mContext.getString(R.string.adultwarning),data,mContext);
		setUpCouponFunctionality(v,data);
//		if(isCouponAvailable(data))
//		else
//			hideCouponFunctionality(v);
		showPopup(v,anchorView);
		cardDataAnalytics = data;
	}
	
	private void setUpCouponFunctionality(View view,CardData data) {	
		couponLayout = (RelativeLayout)view.findViewById(R.id.couponLayout);
		couponLayout.setVisibility(View.VISIBLE);
		couponMessage  = (TextView) view.findViewById(R.id.coupon_applied_message);
		applyCouponBtn = (Button) view.findViewById(R.id.applyCouponBtn);
		couponCodeEt   =  (EditText) view.findViewById(R.id.couponCodeET);		
		clearCoupon = (ImageView)view.findViewById(R.id.clar_coupon);
		clearCoupon.setOnClickListener(new ClearListener());
		couponCodeEt.addTextChangedListener(new CouponCodeWatcher());
		packages = new String[data.packages.size()];
		for(int i=0;i<data.packages.size();i++)
		{
			packages[i] = data.packages.get(i).packageId;	
		}								
		applyCouponBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				applyCouponBtn.setEnabled(false);
				showProgressBar();
//				applyCouponBtn.setVisibility(View.GONE);
				String validateCouponUrl = ConsumerApi.checkCouponCode(COUPON_CODE, packages);
				
							
				RequestQueue queue = MyVolley.getRequestQueue();
				StringRequest applyCouponRequest = new StringRequest(Request.Method.GET, validateCouponUrl, new Listener<String>() {
				@Override
				public void onResponse(String response) {						
					Log.d(TAG, response);					
					String message =null,error = null;
					try {
						ResultsData resultsData  =(ResultsData) Util.fromJson(response, ResultsData.class);
						if(resultsData.code  == 200){
							if(resultsData.results!=null){
								List<CouponResponseData> values = resultsData.results.values;
								for(CouponResponseData coupon : resultsData.results.values){
									if(coupon.status.equalsIgnoreCase("SUCCESS")){
										//mixPanelCouponEntered();
										//Analytics.mixPanelCouponEntered(COUPON_CODE);
										Analytics.priceTobecharged = coupon.priceTobeCharged;//for analytics
										applyCoupon(coupon.packageId,coupon.priceTobeCharged);
										coupanCodes.put(coupon.packageId, COUPON_CODE);
										if(!coupon.message.equalsIgnoreCase(""))
											message = coupon.message;
									}else{
										// ERROR										
											error = coupon.errors.get(0);
											Analytics.mixPanelCouponFailure(COUPON_CODE, error, cardDataAnalytics);
									}
								}
							}
						}
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (JsonParseException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}						
						
						/*
								Log.d(TAG, "Response  = " + response);
								
								JSONObject object;
								try {
									object = new JSONObject(response);
									if (object.getInt("code") == 200 && object.getString("status").equalsIgnoreCase("SUCCESS")) 
									{
										JSONObject result = new JSONObject(object.getString("results"));
										JSONArray coupons = result.getJSONArray("values");
										for(int i=0;i<coupons.length();i++){
											JSONObject coupon = coupons.getJSONObject(i);
											if(coupon.getString("status").equalsIgnoreCase("SUCCESS")){
												isCouponcodeApplied = true;
												if(coupon.getString("status").equalsIgnoreCase("SUCCESS")){
													applyCoupon(coupon.getString("packageId"),coupon.getDouble("priceTobeCharged"));
													coupanCodes.put("packageId", "coupancode");
												}
												Log.d(TAG, coupon.toString());
												if(!coupon.isNull("message"))
													message = coupon.getString("message");
											}else{
												JSONArray errors = coupon.getJSONArray("errors");
												if(message.length()<1)
													message = (String) errors.get(0);
											}
										}
									}else{
										isCouponcodeApplied = false;
										message = " Invalid Coupon Code";
									}
								} catch (JSONException e) {
									e.printStackTrace();
									isCouponcodeApplied = false;
								}
								couponMessage.setText(message);
								*/
								dismissProgressBar();
								couponMessage.setVisibility(View.VISIBLE);
								if(message!=null)
									couponMessage.setText(message);	
								else 
									couponMessage.setText(error);	
							}

					
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(TAG,"error  ="+error.getMessage());
					}
				});
				applyCouponRequest.setShouldCache(false);
				queue.add(applyCouponRequest);
			}
		});
		
		
	}


	private boolean isCouponAvailable(CardData data) {
		for(CardDataPackages pkg: data.packages){
			if(pkg.couponFlag){
				Log.d(TAG,"cupon available?"+pkg.couponFlag);
				return true;
			}
		}
		return false;
	}


	private void addPack(CardData data,LinearLayout parentlayout) {
		if(data.packages == null){return;}
		List<String> qualityList = new ArrayList<String>(); //analytics
		List<String> purchaseOptionsList = new ArrayList<String>(); //analytics
		for(CardDataPackages packageitem:data.packages){
			if(packageitem.priceDetails != null && packageitem.priceDetails.size() > 0){
				if(packageitem.priceDetails.size() == 1 && packageitem.priceDetails.get(0)!=null && packageitem.priceDetails.get(0).paymentChannel.equalsIgnoreCase("INAPP"))
				{
					Log.d(TAG, "not filling inapp item");
					continue;
				}
				createPackItem(packageitem.priceDetails.get(0),packageitem,parentlayout);
				qualityList.add(packageitem.contentType);
				purchaseOptionsList.add(packageitem.commercialModel);
				Log.d(TAG, packageitem.contentType+"  "+ packageitem.commercialModel);
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
				Analytics.mixPanelPaymentOptionsPresented2(cardDataAnalytics,packageitem);//
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
									if(coupanCodes.containsKey(packageitem.packageId)){
										mSubscriptionEngine.setCouponCode(COUPON_CODE);
									}									
									mSubscriptionEngine.doSubscription(packageitem, id);
									paymentModeText.setChecked(false);
									dismissFilterMenuPopupWindow();
								}
							}catch (Exception e) {
									Log.e(TAG, e.toString());
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
		amount.setText(/*mContext.getResources().getString(R.string.price_rupeecode)+*/""+priceDetailItem.price);
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
		 pkgScrollView.scrollTo(0, description.getBottom());	
	}
	private void RegisterUserReq(String contextPath, final Map<String,String> bodyParams) {

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
				
						
				Log.d(TAG,"Response: "+response);
				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
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
						
						Crashlytics.setUserName(userIdSha1);
						Crashlytics.setUserIdentifier(userIdSha1);
						
						//						Util.launchActivity(MainActivity.class,SignUpActivity.this , null);
					}
					else
					{
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
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				Log.d(TAG,"Error: "+error.toString());
				Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
		};
	}
	private class CouponCodeWatcher implements TextWatcher{
		
		@Override
		public void afterTextChanged(Editable s) {
			if(s.length() >0){				
				applyCouponBtn.setEnabled(true);
			}else{
				applyCouponBtn.setVisibility(View.GONE);
				applyCouponBtn.setEnabled(false);
				clearCoupon.setVisibility(View.VISIBLE);
				Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
				applyCouponBtn.startAnimation(anim);
			}
			
			COUPON_CODE = s.toString().trim();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			pkgScrollView.smoothScrollTo(0, couponCodeEt.getBottom());
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count){	
			if(before == 0){
				applyCouponBtn.setVisibility(View.VISIBLE);
				clearCoupon.setVisibility(View.VISIBLE);
				Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
				applyCouponBtn.startAnimation(anim);
			}/*if(start==1  && count == 0){
				applyCouponBtn.setVisibility(View.INVISIBLE);
				clearCoupon.setVisibility(View.INVISIBLE);
				Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
				applyCouponBtn.startAnimation(anim);
			}*/
		}
		
	}

	private void applyCoupon(String packageId, double priceToBeCharged) {
	for (int i = 0; i < susblayout.getChildCount(); i++) {
			Log.d(TAG, "i =" + i);
			View view = susblayout.getChildAt(i);
			CardDataPackages dataPackages = (CardDataPackages) view.getTag();
			if (dataPackages != null) {
				if (dataPackages.packageId != null) {
					if (dataPackages.packageId.equalsIgnoreCase(packageId)) {
						Log.d(TAG, "found");
						showDiscountAnimation(view, priceToBeCharged);
						for (CardDataPackagePriceDetailsItem item : dataPackages.priceDetails) {
							float price = item.price;
							susblayout.setTag(item);
						}
						break;
					}
				}
			}
			// Log.d(TAG,""+dataPackages.packageId);
		}
	}
	
	


	private void showDiscountAnimation(View view,double priceTobecharged) {
		
		LinearLayout layout = (LinearLayout)view;
		layout.setOnClickListener(mPackClickListener);
		for(int i=0;i<layout.getChildCount();i++){
			LinearLayout layout2 = (LinearLayout)layout.getChildAt(i);
			for(int j=0;j<layout2.getChildCount();j++){
				if(layout2.getChildAt(j).getId() == R.id.purchasepackItem1_price){
					LayoutTransition transition = new LayoutTransition();
					transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
					layout2.setLayoutTransition(transition);
					TextView new_price = (TextView) layout2.findViewById(R.id.purchasepackItem1_price);
					Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.shake);
					new_price.startAnimation(anim);
					String oldPrice = new_price.getText().toString().trim();
					double couponPrice = Double.parseDouble(oldPrice) - priceTobecharged; //for analytics
					Analytics.couponDiscountINR = couponPrice; 
					Analytics.mixPanelCouponEntered(COUPON_CODE,couponPrice+"",cardDataAnalytics );
					StrikeTextView oldprice = (StrikeTextView) layout2.findViewById(R.id.purchasepackItem1_price_before_coupon);
					oldprice.setVisibility(View.VISIBLE);
//					oldprice.setPaintFlags(oldprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					oldprice.setText(oldPrice);					
					new_price.setText(" "+priceTobecharged);
					//Analytics.priceTobecharged = priceTobecharged; //to capture the price after coupon in Subscription view
					Util.showToast(mContext, "coupon applied", Util.TOAST_TYPE_INFO);
					break;
				}				
			}
		}
	
//	 couponCodeEt.setText("");	
	}
	public void showProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(mContext, "","Applying coupon...", true, false);
	}

	public void dismissProgressBar() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	private class ClearListener implements OnClickListener{
		@Override
		public void onClick(View v) {			
			couponCodeEt.getText().clear();		
			couponMessage.setText("");
			couponMessage.setVisibility(View.GONE);
		}
		
	}
}
