package com.apalya.myplex.utils;

import com.apalya.myplex.data.CardDataPackagePriceDetailsItem;
import com.apalya.myplex.data.CardDataPackages;

import android.app.ProgressDialog;
import android.content.Context;

public class SubcriptionEngine {
	private Context mContext;
	private ProgressDialog mProgressDialog = null;
	private CardDataPackages mPackageitem;
	private int mSelectedOption;
	public SubcriptionEngine(Context context){
		this.mContext = context; 
	}
	public void doSubscription(CardDataPackages packageitem,int selectedOption){
		this.mPackageitem = packageitem;
		this.mSelectedOption = selectedOption;
		if(mPackageitem == null || mPackageitem.priceDetails == null || mPackageitem.priceDetails.size() < mSelectedOption){
			Util.showToast(mContext, "Error while subscribing", Util.TOAST_TYPE_ERROR);
			return;
		}
		CardDataPackagePriceDetailsItem priceItem = mPackageitem.priceDetails.get(mSelectedOption);
//		if(priceItem.paymentChannel)
	}
	public void showProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = ProgressDialog.show(mContext, "", "Loading...",true, false);
	}

	public void dismissProgressBar() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
}
