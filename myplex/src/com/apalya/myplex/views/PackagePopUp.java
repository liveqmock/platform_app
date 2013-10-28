package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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

import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.SubscriptionView;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataCertifiedRatingsItem;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.data.CardDataPromotionDetailsItem;
import com.apalya.myplex.utils.Analytics;
import com.apalya.myplex.utils.Blur;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.FontUtil;
import com.apalya.myplex.utils.Util;
import com.apalya.myplex.utils.Blur.BlurResponse;
import com.flurry.android.FlurryAgent;

public class PackagePopUp {
	private String TAG  = "PackagePopUp";
	private PopupWindow mFilterMenuPopupWindow = null;
	private List<PopupWindow> mFilterMenuPopupWindowList = new ArrayList<PopupWindow>();
	private Context mContext;
	private LayoutInflater mInflater;
	private View mBackground;
	private RelativeLayout mPopupBackground;
	public PackagePopUp(Context cxt,View background){
		this.mContext = cxt;
		this.mBackground = background;
		this.mInflater = LayoutInflater.from(mContext);
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
		mPaymentModeHeading = (TextView)v.findViewById(R.id.purchasepopup_selectedpaymentmodeheading);
		mPaymentModeHeading.setTypeface(FontUtil.Roboto_Medium);
		mPaymentModeHeading.setVisibility(View.INVISIBLE);
		mPaymentModeGroup = (LinearLayout)v.findViewById(R.id.purchasepopup_selectedpaymentmodegroup);
		mPaymentModeGroup.setVisibility(View.INVISIBLE);
		LayoutTransition transition = new LayoutTransition();
		transition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
		mPaymentModeGroup.setLayoutTransition(transition);
		addBlur();
		addPack(data,susblayout);
		showPopup(v,anchorView);
	}
	private void addPack(CardData data,LinearLayout parentlayout) {
		if(data.packages == null){return;}
		for(CardDataPackages packageitem:data.packages){
			if(packageitem.priceDetails != null && packageitem.priceDetails.size() > 0){
				createPackItem(packageitem.priceDetails.get(0),packageitem,parentlayout);
			}
		}
	}
	private TextView mPaymentModeHeading;
	private LinearLayout mPaymentModeGroup;
	private OnClickListener mPackClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v.getTag() instanceof CardDataPackages){
				deSelectViews();
				v.setAlpha(1.0f);
				CardDataPackages packageitem = (CardDataPackages)v.getTag();
				if(packageitem == null){return;}
				if(packageitem.priceDetails == null){return;}
				mPaymentModeHeading.setVisibility(View.VISIBLE);
				mPaymentModeGroup.setVisibility(View.VISIBLE);
				mPaymentModeGroup.removeAllViews();
				int count = 0;
				for(CardDataPackagePriceDetailsItem priceItem:packageitem.priceDetails){
					View paymentModeItem = mInflater.inflate(R.layout.paymentmodeitemlayout,null);
					TextView paymentModeText = (TextView)paymentModeItem.findViewById(R.id.paymentmodetext);
					paymentModeText.setTypeface(FontUtil.Roboto_Medium);
					paymentModeText.setText(priceItem.name);
					mPaymentModeGroup.addView(paymentModeItem);
					Util.showFeedback(paymentModeItem);
					paymentModeItem.setId(count);
					count++;
					paymentModeItem.setTag(packageitem);
					paymentModeItem.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							try {
								if(arg0.getTag() instanceof CardDataPackages){
									CardDataPackages packageitem = (CardDataPackages)arg0.getTag();
									int id = arg0.getId();
									CardDataPackagePriceDetailsItem priceItem = packageitem.priceDetails.get(id);
									String requestUrl = ConsumerApi.getSusbcriptionRequesr(priceItem.paymentChannel, packageitem.packageId);
									Log.e(TAG, "RequestURL = "+requestUrl);
									Intent i = new Intent(mContext,SubscriptionView.class);
									Bundle b = new Bundle();
									b.putString("url", requestUrl);
									i.putExtras(b);
									FlurryAgent.onStartSession(mContext, "X6WWX57TJQM54CVZRB3K");
									Map<String,String> params=new HashMap<String, String>();
									params.put("PackageId",packageitem.packageId);
									params.put("PackageName", packageitem.packageName);
									params.put("PaymentChannel", priceItem.paymentChannel);
									params.put("Action", "Purchase");
									Analytics.trackEvent(Analytics.PackagesPurchase,params);
									FlurryAgent.onEndSession(mContext);
									((Activity) mContext).startActivityForResult(i, ConsumerApi.SUBSCRIPTIONREQUEST);
									dismissFilterMenuPopupWindow();
								}
							} catch (Exception e) {
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
		View v = mInflater.inflate(R.layout.purchasepackitem, null);
		LinearLayout promotionalLayout = (LinearLayout)v.findViewById(R.id.purchasepackItem1_offer);
		if(packageitem.promotionDetails == null ){ promotionalLayout.setVisibility(View.INVISIBLE);}
		if(packageitem.promotionDetails.size() == 0){ promotionalLayout.setVisibility(View.INVISIBLE);}
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
		v.setTag(packageitem);
		v.setOnClickListener(mPackClickListener);
		mPackViewList.add(v);
		parentLayout.addView(v);
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
			title.setText(data.generalInfo.title);
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

}
