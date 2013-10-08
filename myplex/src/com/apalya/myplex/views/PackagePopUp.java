package com.apalya.myplex.views;

import java.util.ArrayList;
import java.util.List;

import com.apalya.myplex.R;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDataCertifiedRatingsItem;
import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;
import com.apalya.myplex.utils.FontUtil;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.Space;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class PackagePopUp {
	private PopupWindow mFilterMenuPopupWindow = null;
	private List<PopupWindow> mFilterMenuPopupWindowList = new ArrayList<PopupWindow>();
	private Context mContext;
	private LayoutInflater mInflater;
	public PackagePopUp(Context cxt){
		mContext = cxt;
		mInflater = LayoutInflater.from(mContext);
	}
	private void dismissFilterMenuPopupWindow() {
		if (mFilterMenuPopupWindow != null) {
			mFilterMenuPopupWindowList.remove(mFilterMenuPopupWindow);
			mFilterMenuPopupWindow.dismiss();
			mFilterMenuPopupWindow = null;
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
		fillPackData(data,v);
		LinearLayout susblayout = (LinearLayout) v.findViewById(R.id.purchasepopup_packslayout);
		addPack(data,susblayout);
		showPopup(v,anchorView);
	}
	private void addPack(CardData data,LinearLayout parentlayout) {
		if(data.packages == null){return;}
		for(CardDataPackages packageitem:data.packages){
			if(packageitem.priceDetails != null){
				for(CardDataPackagePriceDetailsItem priceDetailItem:packageitem.priceDetails){
					createPackItem(priceDetailItem,packageitem,parentlayout);
				}
			}
		}
	}
	private void createPackItem(CardDataPackagePriceDetailsItem priceDetailItem,CardDataPackages packageitem,LinearLayout parentLayout){
		View v = mInflater.inflate(R.layout.purchasepackitem, null);
		LinearLayout promotionalLayout = (LinearLayout)v.findViewById(R.id.purchasepackItem1_offer);
		promotionalLayout.setVisibility(View.INVISIBLE);
		TextView amount = (TextView)v.findViewById(R.id.purchasepackItem1_price);
		amount.setTypeface(FontUtil.Roboto_Medium);
		amount.setText("Rs "+priceDetailItem.price);
		TextView quality = (TextView)v.findViewById(R.id.purchasepackItem1_quality);
		quality.setTypeface(FontUtil.Roboto_Medium);
		quality.setText(packageitem.contentType);
		TextView type = (TextView)v.findViewById(R.id.purchasepackItem1_type);
		type.setTypeface(FontUtil.Roboto_Medium);
		type.setText(packageitem.commercialModel);
		addSpace(parentLayout, (int)mContext.getResources().getDimension(R.dimen.margin_gap_16));
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
