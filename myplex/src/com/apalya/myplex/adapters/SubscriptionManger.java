package com.apalya.myplex.adapters;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.utils.ConsumerApi;
import com.apalya.myplex.utils.MyVolley;

public class SubscriptionManger {

	public static final String TAG = "SubscriptionManger";
	
	public void doSubscription(String contentId, String packageId)
	{
		
	}
	
	private void getBillingModes(String contentId){
		contentId.replace(" ", "%20");
		String url = ConsumerApi.getBillingMode(contentId);
		RequestQueue queue = MyVolley.getRequestQueue();
		StringRequest myReg = new StringRequest(Method.POST, url, billingModesSuccess(), billingModesError());
		myReg.setShouldCache(true);
		queue.add(myReg);
	}

	private ErrorListener billingModesError() {
		// TODO Auto-generated method stub
		return null;
	}

	private Listener<String> billingModesSuccess() {
		// TODO Auto-generated method stub
		return null;
	}
}
