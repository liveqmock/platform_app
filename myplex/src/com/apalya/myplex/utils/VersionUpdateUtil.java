package com.apalya.myplex.utils;

import java.io.IOException;
import java.util.Date;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.R;
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.data.VersionUpdateData;
import com.apalya.myplex.utils.AlertDialogUtil.ButtonDialogListener;
import com.apalya.myplex.utils.AlertDialogUtil.NoticeDialogListener;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class VersionUpdateUtil {

	private static long savedSystemTime;
	private static long DAY_MILLIS = 86400000;

	private static String TAG = "VersionUpdateUtil";
	private Context mContext;
	private VersionUpdateCallbackListener mListener;	
	private String key;
	private long current_time;

	public interface VersionUpdateCallbackListener {
		public boolean showUpgradeDialog();
	}

	public VersionUpdateUtil(Context context,
			VersionUpdateCallbackListener listener) {

		this.mListener = listener;
		this.mContext = context;
	}

	public void checkIfUpgradeAvailable(String url, String key) {

		if (!ApplicationSettings.ENABLE_APP_UPDATE_CHECK) {
			return;
		}

		if (url == null || key == null || mListener == null) {
			return;
		}

		Date date = new Date(System.currentTimeMillis()); // or simply new
															// Date();

		this.key = key;
		// converting it back to a milliseconds representation:
		current_time = date.getTime();

		if(savedSystemTime == 0)
			savedSystemTime = SharedPrefUtils.getLongFromSharedPreference(mContext,
				key);

		if (savedSystemTime == 0) {
			SharedPrefUtils.writeToSharedPref(mContext, key, current_time);
			savedSystemTime = SharedPrefUtils.getLongFromSharedPreference(
					mContext, key);
		}

		if ((current_time < (savedSystemTime + DAY_MILLIS))) {
			Log.d(TAG, "Ignore version check");
			return;
		}

		Log.d(TAG, "RequestUrl=" + url);

		if (url == null)
			return;
		
		RequestQueue queue = MyVolley.getRequestQueue();

		StringRequest myReg = new StringRequest(Method.GET, url,
				onlineRequestSuccessListener(), onlineRequestErrorListener());
		myReg.setShouldCache(false);
		queue.add(myReg);
	}

	private Response.Listener<String> onlineRequestSuccessListener() {
		return new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {

				Log.i(TAG, " Version update response : " + response);
				if (response == null) {
					onVersionCheck(false, null);
					return;
				}

				VersionUpdateData responseData = null;
				try {
					responseData = (VersionUpdateData) Util.fromJson(response,
							VersionUpdateData.class);
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (responseData == null || responseData.status == null) {
					onVersionCheck(false, null);
					return;
				}

				if (responseData.status.equalsIgnoreCase("SUCCESS")) {
					onVersionCheck(true, responseData);
				}

			}
		};
	}

	private Response.ErrorListener onlineRequestErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i(TAG, " Version update error  response : " + error);
				if (error != null && error.networkResponse != null) {
					Log.e(TAG, "$$$  " + error.networkResponse.statusCode);
				}

				onVersionCheck(false, null);

			}
		};
	}

	private void onVersionCheck(boolean value,
			final VersionUpdateData responseData) {

		if (!value)
			return;

		PackageManager packageManager = mContext.getPackageManager();
		PackageInfo packageInfo = null;

		try {
			packageInfo = packageManager.getPackageInfo(
					mContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (key != null) {
			SharedPrefUtils.writeToSharedPref(mContext, key, current_time);
			Log.d(TAG, "Update time saved");
		}

		if (responseData == null || responseData.app == null
				|| !responseData.app.validate()) {
			Log.d(TAG, "Ignore version upgrade bad response");
			return;
		}

		if (responseData.app.version <= packageInfo.versionCode) {
			Log.d(TAG, "Install version is already updated");
			return;
		}

		if (mListener != null && !mListener.showUpgradeDialog()) {
			return;
		}

		final Intent appIntent = new Intent(Intent.ACTION_VIEW);

		if (!TextUtils.isEmpty(responseData.app.type)
				&& (responseData.app.type.equalsIgnoreCase("Mandatory"))
				&& responseData.app.message != null) {
			AlertDialogUtil.showNeutralAlert(mContext,
					responseData.app.message, mContext.getResources()
							.getString(R.string.upgrade_now),
					new ButtonDialogListener() {

						@Override
						public void onDialogOptionClick() {

							if (responseData.app.link != null) {
								appIntent.setData(Uri
										.parse(responseData.app.link));
								mContext.startActivity(appIntent);
							}

						}

					});
		} else if (!TextUtils.isEmpty(responseData.app.type)
				&& responseData.app.message != null) {
			AlertDialogUtil.showAlert(mContext, responseData.app.message,
					mContext.getResources().getString(R.string.later), mContext
							.getResources().getString(R.string.upgrade_now)

					, new NoticeDialogListener() {

						public void onDialogOption2Click() {

							if (responseData.app.link != null) {
								appIntent.setData(Uri
										.parse(responseData.app.link));
								mContext.startActivity(appIntent);
							}
						}

						public void onDialogOption1Click() {
						}

					});
		}

	}

}
