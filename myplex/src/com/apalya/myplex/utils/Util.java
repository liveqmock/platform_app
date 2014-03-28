package com.apalya.myplex.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.MainActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.R.color;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.ApplicationConfig;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.CardDownloadData;
import com.apalya.myplex.data.CardDownloadedDataList;
import com.apalya.myplex.data.CardExplorerData;
import com.apalya.myplex.data.FetchDownloadData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.tablet.MultiPaneActivity;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.analytics.tracking.android.EasyTracker;

public class Util {

	public final static int TOAST_TYPE_INFO = 1;
	public final static int TOAST_TYPE_ERROR = 2;
	public final static String downloadStoragePath="/sdcard/Android/data/com.apalya.myplex/files/";
	public static KeyRenewListener keyRenewListener;
	
	
	public static void showToast(Context context,String msg,int type){
		if(context == null){return;}
		try {
			/*Toast toast = new Toast(context);
			LayoutInflater inflate = LayoutInflater.from(context);
			View v = inflate.inflate(R.layout.toastlayout, null);
			TextView header = (TextView)v.findViewById(R.id.toast_type);
			header.setTypeface(FontUtil.ss_symbolicons_line);
			TextView message = (TextView)v.findViewById(R.id.toast_text);
			message.setTypeface(FontUtil.Roboto_Medium);
			if(type == TOAST_TYPE_ERROR){
				header.setText(R.string.toast_warning);
			}
			message.setText(msg);
			toast.setView(v);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.show();*/
			Toast.makeText(context, msg,type).show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void showToastAt(Context context,String msg,int type, int gravity, int xOffset, int yOffset){
		if(context == null){return;}
		try {
			Toast toast = new Toast(context);
			LayoutInflater inflate = LayoutInflater.from(context);
			View v = inflate.inflate(R.layout.toastlayout, null);
			TextView header = (TextView)v.findViewById(R.id.toast_type);
			header.setTypeface(FontUtil.ss_symbolicons_line);
			TextView message = (TextView)v.findViewById(R.id.toast_text);
			message.setTypeface(FontUtil.Roboto_Medium);
			if(type == TOAST_TYPE_ERROR){
				header.setText(R.string.toast_warning);
			}
			message.setText(msg);
			toast.setGravity(gravity, xOffset, yOffset);
			toast.setView(v);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static int getStatusBarHeight(Context context) {
		if(context == null){return 48;}
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		if (result > 100) {
			result = 48;
		}
		return result;
	}
	public static int dpToPx(Resources res, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				res.getDisplayMetrics());
	}
	private static ObjectMapper m = new ObjectMapper();
	private static JsonFactory jf = new JsonFactory();

	public static <T> Object fromJson(String jsonAsString, Class<T> pojoClass)
			throws JsonMappingException, JsonParseException, IOException {
		return m.readValue(jsonAsString, pojoClass);
	}

	public static <T> Object fromJson(FileReader fr, Class<T> pojoClass)
			throws JsonParseException, IOException {
		return m.readValue(fr, pojoClass);
	}

	public static String toJson(Object pojo, boolean prettyPrint)
			throws JsonMappingException, JsonGenerationException, IOException {
		StringWriter sw = new StringWriter();
		JsonGenerator jg = jf.createJsonGenerator(sw);
		if (prettyPrint) {
			jg.useDefaultPrettyPrinter();
		}
		m.writeValue(jg, pojo);
		return sw.toString();
	}

	public static void toJson(Object pojo, FileWriter fw, boolean prettyPrint)
			throws JsonMappingException, JsonGenerationException, IOException {
		JsonGenerator jg = jf.createJsonGenerator(fw);
		if (prettyPrint) {
			jg.useDefaultPrettyPrinter();
		}
		m.writeValue(jg, pojo);
	}

	public static void updateCardData(JSONObject obj,CardData data) {

	}

	public static void prepareDisplayinfo(Activity activity) {
		try {
			ConsumerApi.DOMAIN = activity.getString(R.string.config_domain_name);
			myplexapplication.getApplicationConfig().downloadCardsPath =  activity.getFilesDir()+"/"+"downloadlist.bin";
			myplexapplication.getApplicationConfig().msisdnPath =  activity.getFilesDir()+"/"+"msisdn.bin";
			myplexapplication.getApplicationConfig().lastViewedCardsPath = activity.getFilesDir()+"/"+"lastviewed.bin";
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			myplexapplication.getApplicationConfig().screenHeight = dm.heightPixels;
			myplexapplication.getApplicationConfig().screenWidth = dm.widthPixels;
			myplexapplication.getApplicationConfig().type = findDpi(dm.densityDpi);

			File internalPath = activity.getFilesDir();
			StringBuilder appDirectory = new StringBuilder();
			appDirectory.append(Environment.getExternalStorageDirectory().getAbsolutePath());
			appDirectory.append(File.separator).append("Android").append(File.separator).append("data").append(File.separator).append(activity.getPackageName());
			appDirectory.toString();
			//Replace internalPath with appDirectory to store in memory card.
			//Remember to add WRITE_EXTERNAL_STORAGE permission in Manifest file
			myplexapplication.getApplicationConfig().indexFilePath = ""+internalPath;

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static String findDpi(int density) {
		String value = ApplicationConfig.MDPI;
		switch (density) {
		case DisplayMetrics.DENSITY_LOW:
			value = ApplicationConfig.LDPI;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			value = ApplicationConfig.MDPI;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			value = ApplicationConfig.HDPI;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			value = ApplicationConfig.XHDPI;
			break;
		default:
			value = ApplicationConfig.XHDPI;
			break;
		}
		return value;
	}

	public static void showFeedback(View v) {
		if (v == null) {
			return;
		}
		v.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundColor(Color.parseColor("#54B5E9"));
					break;
				default:
					v.setBackgroundColor(Color.TRANSPARENT);
					break;
				}
				return false;
			}
		});
	}
	public static void showFeedbackOnSame(View v) {
		if (v == null) {
			return;
		}
		v.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundColor(color.searchtags_color);
					break;
				default:
					v.setBackgroundColor(Color.TRANSPARENT);
					break;
				}
				return false;
			}
		});
	}
	/**
	 * @param context used to check the device version and DownloadManager information
	 * @return true if the download manager is available
	 */
	public static boolean isDownloadManagerAvailable(Context context) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				return false;
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
			List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		} catch (Exception e) {
			return false;
		}
	}
	public static void launchActivity(
			Class<? extends Activity> nextActivityClass,
			Activity currentActivity, Map<String, String> extrasMap) {
		Intent launchIntent = new Intent(currentActivity, nextActivityClass);
		if (extrasMap != null && extrasMap.size() > 0) {
			Set<String> keys = extrasMap.keySet();
			for (String key : keys) {
				launchIntent.putExtra(key, extrasMap.get(key));
			}
		}
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		currentActivity.startActivity(launchIntent);
	}
	public static void showToast(CharSequence aMsg,Context mContext){

		Toast.makeText(mContext, 
				aMsg, 
				Toast.LENGTH_LONG).show();
	}
	public static boolean isFileExist(String fileName){
		// Get path for the file on external storage.  If external
	    // storage is not currently mounted this will fail.
	    File file = new File(downloadStoragePath, fileName);
	    if (file != null) {
	        return file.exists();
	    }
	    return false;

	}
	public static void removeDownload(long id,Context mContext){
		if(isDownloadManagerAvailable(mContext))
		{
			try{
			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
			manager.remove(id);
			Uri path=manager.getUriForDownloadedFile(id);
			if(path!=null)
			{
				File file = new File(path.toString());
			    if (file != null) {
			        file.delete();
			    }

			}
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static long getSpaceAvailable(){
		long bytes=0;
		File file = new File(Environment.getExternalStorageDirectory().getPath());
	    if (file != null) {
	    	long gb=1024L*1024L*1024L;
	    	bytes=file.getFreeSpace()/gb;
	    }
	    return bytes;
	}
	
	public static String startDownload(String aUrl,CardData aMovieData,Context mContext)
	{
		long downloadStartTime = System.currentTimeMillis();
		String key = aMovieData.generalInfo._id+Analytics.UNDERSCORE+Analytics.downLoadStartTime;
		SharedPrefUtils.writeToSharedPref(mContext, key, downloadStartTime);
		long lastDownloadId=-1L;
		String aMovieName=aMovieData.generalInfo.title.toLowerCase();
		String aFileName=aMovieData._id;
		//Analytics.mixPanelDownloadsMovie(aMovieName,aFileName);
		CardDownloadedDataList downloadlist =  null;
		try {
			downloadlist = (CardDownloadedDataList) Util.loadObject(myplexapplication.getApplicationConfig().downloadCardsPath);	
		} catch (Exception e) {
			// TODO: handle exception
		}
		if(isDownloadManagerAvailable(mContext))
		{
			try{
			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);


			//Uri uri=Uri.parse("http://46.137.243.190/wvm/armag_prod2.wvm");
			Uri uri=Uri.parse(aUrl);

			/*Environment
			.getDownloadCacheDirectory()
			.mkdirs();*/
			
			

			try{
			lastDownloadId=
					manager.enqueue(new DownloadManager.Request(uri)
					.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
							.setAllowedOverRoaming(false)
							.setTitle("myplex")
							.setDescription(aMovieName)
							.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
							.setDestinationInExternalFilesDir(mContext, "", aFileName+".wvm"));
			}catch(Throwable t){
				lastDownloadId=0;
				Log.d(TAG,"downl;oad failed");
				Util.showToast(mContext, "Some error occured during downloading.",Util.TOAST_TYPE_INFO);
				t.printStackTrace();
				Crashlytics.logException(t);
			}
			
			if(lastDownloadId>0)
			{
				Util.showToast(mContext, "Your download has been started, Please check download status in Downloads section.",Util.TOAST_TYPE_INFO);
			}
			else{
				Util.showToast(mContext, "Download Request has failed, Please check your WIFI is turned on.",Util.TOAST_TYPE_INFO);
			}
			
			}
			catch(IllegalArgumentException e)
			{
				e.printStackTrace();
				Util.showToast("Can only download HTTP/HTTPS links, please try again", mContext);
			}
		}
		else
		{
			//Download Manager is not available
		}
		if(downloadlist==null)
		{
			downloadlist= new CardDownloadedDataList();
			downloadlist.mDownloadedList=new HashMap<String, CardDownloadData>();
		}
		
		CardDownloadData downloadData= new CardDownloadData();
		downloadData.mDownloadId=lastDownloadId;
		downloadData.mDownloadPath=downloadStoragePath+aFileName+".wvm";
		//downloadData.mDownloadPath=mContext.getExternalFilesDir(null).getPath() +"/"+aMovieName+".wvm";
		downloadlist.mDownloadedList.put(aMovieData._id, downloadData);
		myplexapplication.mDownloadList=downloadlist;
		Util.saveObject(downloadlist, myplexapplication.getApplicationConfig().downloadCardsPath);
		
		return downloadData.mDownloadPath;
	}
	
	public static void InviteFriends(final Context mContext) {

		if(Session.getActiveSession()!=null)
		{
			if(Session.getActiveSession().isOpened())
			{
				Bundle params = new Bundle();

				params.putString("message", "Hey, Check out this cool app where we can watch movies and live tv shows.");
				params.putBoolean("new_style_message", true);
				/*params.putString("data",
					"{\"badge_of_awesomeness\":\"1\"," +
					"\"social_karma\":\"5\"}");*/
				WebDialog requestsDialog = (
						new WebDialog.RequestsDialogBuilder(mContext,
								Session.getActiveSession(),
								params))
								.setOnCompleteListener(new OnCompleteListener() {

									@Override
									public void onComplete(Bundle values,
											FacebookException error) {
										if (error != null) {
											if (error instanceof FacebookOperationCanceledException) {
												Toast.makeText(mContext, 
														"Request cancelled", 
														Toast.LENGTH_SHORT).show();
											} else {
												int numberofInvitees = 1;
												Analytics.mixPanelInviteFriends("facebook", numberofInvitees+"", "failure");

												Toast.makeText(mContext, 
														"Network Error", 
														Toast.LENGTH_SHORT).show();
											}
										} else {
											final String requestId = values.getString("request");
											if (requestId != null) {
												int numberofInvitees = 1;
												Analytics.mixPanelInviteFriends("facebook", numberofInvitees+"", "success");
												Toast.makeText(mContext, 
														"Request sent",  
														Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(mContext, 
														"Request cancelled", 
														Toast.LENGTH_SHORT).show();
											}
										}   
									}

								})
								.build();
				requestsDialog.show();
			}else
			{
				showToast("Please Login with Facebook to invite your friends!!!", mContext);
			}
		}
		else
		{
			showToast("Please Login with Facebook to invite your friends!!!", mContext);
		}
	}
	public static void animate(float fromX, float toX, final View v,
			final boolean showlist, int animationType,Context mContext) {
		if (v == null) {
			return;
		}
		AnimatorSet set = new AnimatorSet();
		if (animationType == 1) {
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_X, fromX, toX));
		} else {
			set.play(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromX, toX));
		}
		set.setDuration(mContext.getResources().getInteger(
				android.R.integer.config_longAnimTime));
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub

			}
		});
		set.start();
	}
	public static void shareData(Context mContext,int aType,String aPath,String aTitle){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		//sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
		String msg="Watching "+aTitle+" on myplex \n check it out on http://www.myplex.com/";
		sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
		if(aType==1)
		{
			Uri picUri = Uri.fromFile(new File(aPath));
			//Uri picUri = Uri.parse(aPath);
			sendIntent.setData(picUri);
			sendIntent.setType("image/*");
			sendIntent.putExtra(Intent.EXTRA_STREAM, picUri);
		}
		else if(aType==2)
		{
			//Uri picUri = Uri.fromFile(new File("/storage/sdcard0/DCIM/Camera/VID_20131002_163415.3gp"));
			Uri picUri = Uri.fromFile(new File(aPath));
			sendIntent.setData(picUri);
			sendIntent.setType("video/*");
			sendIntent.putExtra(Intent.EXTRA_STREAM, picUri);
		}
		else
		{
			sendIntent.setType("text/plain");	
		}
		
		Analytics.mixPanelSharedMyplexExperience();
		mContext.startActivity(Intent.createChooser(sendIntent,  mContext.getResources().getText(R.string.send_to)));
	}
	public static void FitToRound(Context mContext,ImageView View,Bitmap bm){
		try {
			Bitmap bitmap = bm;
			Bitmap bitmapRounded = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
			Canvas canvas = new Canvas(bitmapRounded);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
			canvas.drawRoundRect((new RectF(0.0f, 0.0f, bitmap.getWidth(), bitmap.getHeight())), 10, 10, paint);
			View.setImageBitmap(bitmapRounded);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public static void showDownloads(Context mContext) {
		try {
			Intent i = new Intent();
			i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
			mContext.startActivity(i);	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public static void launchMainActivity(Activity activity){
		try {
			if(activity.getResources().getBoolean(R.bool.isTablet)){
				activity.startActivity(new Intent(activity,MultiPaneActivity.class));	
			}
			else{
				activity.startActivity(new Intent(activity,MainActivity.class));
			}
		} catch (Exception e) {
			if(e != null){
				e.printStackTrace();
			}
		}
	}
	
	public static boolean onHandleExternalIntent(Activity activity) {
		
		if (activity.getIntent() == null)
			return false;
		
		Bundle bundle = activity.getIntent().getExtras();
		
		if (bundle == null)
			return false;
		
		Intent intent;
		
		if (activity.getResources().getBoolean(R.bool.isTablet)) {
			intent = new Intent(activity, MultiPaneActivity.class);
		} else {
			intent = new Intent(activity, MainActivity.class);
		}
		
		for (String key : bundle.keySet()) {			
			intent.putExtra(key, bundle.getString(key).trim());
		}

		launchMainActivity(activity, intent);
		return true;
	}
	

	public static void launchMainActivity(Activity activity, Intent intent){
		activity.startActivity(intent);
		
	}
	
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
	private static String TAG="GENERATE KEY";
	private static Context mContext;
	public static void genKeyRequest(Context context,String contextPath, final Map<String, String> bodyParams) {
		mContext=context;
		RequestQueue queue = MyVolley.getRequestQueue();

		String url=ConsumerApi.SCHEME+ConsumerApi.DOMAIN+ConsumerApi.SLASH+ConsumerApi.USER_CONTEXT+ConsumerApi.SLASH+contextPath;
		StringRequest myReq = new StringRequest(Method.POST,
				url,
				genKeyRegSuccessListener(),
				genKeyRegErrorListener()) {

			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				Map<String, String> params = new HashMap<String, String>();
				params=bodyParams;
				return params;
			};
		};
		Log.d(TAG,"Request sent ");
		queue.add(myReq);
	}

	protected static ErrorListener genKeyRegErrorListener() {
		return new Response.ErrorListener() {
			public void onErrorResponse(VolleyError error) {
				
				Log.d(TAG,"Error: "+error.toString());
				if(keyRenewListener!=null){
					keyRenewListener.onKeyRenewFailed(error.toString());
				}
				if(error.toString().indexOf("NoConnectionError")>0)
				{
					//Util.showToast(getString(R.string.interneterr),LoginActivity.this);
					//finish();
					//Util.launchActivity(MainActivity.class,LoginActivity.this , null);

				}
				else
				{
					//Util.showToast(error.toString(),LoginActivity.this);	
				}

				Log.d(TAG, "@@@@@@@@@@@@@@@ LOGIN ACTIVITY @@@@@@@@@@@@@@@@@@@@");
			}
		};
	}


	protected static Listener<String> genKeyRegSuccessListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"Response: "+response);
				//Analytics.endTimedEvent("NEW-CLIENT-KEY-GENERATION");

				try {	
					Log.d(TAG, "########################################################");
					JSONObject jsonResponse= new JSONObject(response);

					if(jsonResponse.getString("status").equalsIgnoreCase("SUCCESS"))
					{
						
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "expiresAt: "+jsonResponse.getString("expiresAt"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "clientKey: "+jsonResponse.getString("clientKey"));
						Log.d(TAG, "########################################################");
						(myplexapplication.getDevDetailsInstance()).setClientKey(jsonResponse.getString("clientKey"));
						(myplexapplication.getDevDetailsInstance()).setClientKeyExp(jsonResponse.getString("expiresAt"));
						Log.d(TAG, "---------------------------------------------------------");
						ConsumerApi.DEBUGCLIENTKEY=  jsonResponse.getString("clientKey");

						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkey), jsonResponse.getString("clientKey"));
						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkeyexp), jsonResponse.getString("expiresAt"));

						//Util.showToast(mContext,"Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),Util.TOAST_TYPE_ERROR);
						if(keyRenewListener!=null){
							keyRenewListener.onKeyRenewed();
						}
					}
					else
					{

						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkey), "");
						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkeyexp), "");

						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						
						//Util.showToast(mContext,"Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),Util.TOAST_TYPE_ERROR);
//						Util.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),mContext);
						if(keyRenewListener!=null){
							
							keyRenewListener.onKeyRenewFailed(jsonResponse.getString("message"));
						}
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	public static String getGoogleAccountName(Context context){
		String possibleEmail=null;
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts) {
		    if (emailPattern.matcher(account.name).matches()) {
		    	possibleEmail = account.name;
		        return possibleEmail;
		    }
		}
		return possibleEmail;
	}
	
	public static void serializeData(Context context) {
		try {
			String joinedDate;
			joinedDate=myplexapplication.getUserProfileInstance().joinedDate;
			String dir=context.getCacheDir().toString()+"/uprofile.dat";
			FileOutputStream fStream = new FileOutputStream(context.getCacheDir()+"/uprofile.dat");
			//FileOutputStream fStream = context.openFileOutput("downloaddetails.dat", Context.MODE_PRIVATE) ;
			ObjectOutputStream oStream = new ObjectOutputStream(fStream);
			/*oStream.writeObject(myplexapplication.getUserProfileInstance().getName());
			oStream.writeObject(myplexapplication.getUserProfileInstance().getProfilePic());*/
			oStream.writeObject(joinedDate);    
			List<CardData> cd=myplexapplication.getUserProfileInstance().lastVisitedCardData;
			List<String> cardIds=new ArrayList<String>();
			for(CardData data:cd)
				cardIds.add(data._id);
			oStream.writeObject(cardIds);
			oStream.flush();
			oStream.close();

			Log.v("Serialization success", "Success");
		} catch (Exception e) {
			Log.v("IO Exception", e.getMessage());
		}
	}  
	public static void deserializeData(Context context){
		File file=new File(context.getCacheDir(), "uprofile.dat");
		if(!file.exists())
			return;
		
		try {

			
			FileInputStream fint = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fint);
			/*String name =(String) ois.readObject();
			myplexapplication.getUserProfileInstance().setName(name);
			String pic =(String) ois.readObject();
			myplexapplication.getUserProfileInstance().setProfilePic(pic);*/
			myplexapplication.getUserProfileInstance().joinedDate =(String) ois.readObject();
			//myplexapplication.getUserProfileInstance().lastVisitedCardData=(List<CardData>)  ois.readObject();
			List<String> cardIds=new ArrayList<String>();
			cardIds=(List<String>) ois.readObject();
			ois.close();
			for(String id:cardIds)
			{
				CardData cd=new CardData();
				cd._id=id;
				myplexapplication.getUserProfileInstance().lastVisitedCardData.add(cd);
			}
			LastWatchedCardDetails lastWatchedData=new LastWatchedCardDetails();
			lastWatchedData.getLastWatchedCardDetails();
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	public static String sha1Hash( String toHash )
	{
	    String hash = null;
	    try
	    {
	        MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
	        byte[] bytes = toHash.getBytes("UTF-8");
	        digest.update(bytes, 0, bytes.length);
	        bytes = digest.digest();
	        StringBuilder sb = new StringBuilder();
	        for( byte b : bytes )
	        {
	            sb.append( String.format("%02X", b) );
	        }
	        hash = sb.toString();
	    }
	    catch( NoSuchAlgorithmException e )
	    {
	        e.printStackTrace();
	    }
	    catch( UnsupportedEncodingException e )
	    {
	        e.printStackTrace();
	    }
	    return hash;
	}
	public static boolean isUserOnline(Context mContext){
		ConnectivityManager cm =
		        (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		
		return isConnected;
	}
	
	public static boolean isWifiEnabled(Context mContext){
		
		ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm == null)
			return false; 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if(activeNetwork == null)
			return false;
		
		boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		return isWiFi;
	}
	
	public static boolean isNetworkAvailable(Context mContext)
	{
		ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm == null)
			return false;
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if(networkInfo !=null && networkInfo.isAvailable())
			return true;
		return false;
	}
	
	public static void saveObject(Object obj,String path) {		try {			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path))); 			oos.writeObject(obj); 			oos.flush(); 			oos.close();		} catch (Exception ex) {			if(ex != null){
//				Log.v("Util", ex.getMessage());				ex.printStackTrace();			}
		}	}	public static Object loadObject(String path) {		try {			File f = new File(path);			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));			Object o = ois.readObject();
			ois.close();			return o;		} catch (Exception ex) {			if(ex != null){
//				Log.v("Util", ex.getMessage());				ex.printStackTrace();			}
		}		return null;	}
	
	public static long dirSize(File dir) {
	    long result = 0;

	    try {
			Stack<File> dirlist= new Stack<File>();
			dirlist.clear();

			dirlist.push(dir);

			while(!dirlist.isEmpty())
			{
			    File dirCurrent = dirlist.pop();

			    File[] fileList = dirCurrent.listFiles();
			    for (int i = 0; i < fileList.length; i++) {

			        if(fileList[i].isDirectory())
			            dirlist.push(fileList[i]);
			        else
			            result += fileList[i].length();
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	    return result;
	}
	public static boolean isTokenValid(String clientKeyExp) {

		//Util.showToast(clientKeyExp);

		List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss'Z'"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
		
		Date convertedDate = new Date();
		for (SimpleDateFormat pattern : knownPatterns) {
		    try {
		    	convertedDate = pattern.parse(clientKeyExp);
		    	break;
		    } catch (ParseException pe) {
		    	pe.printStackTrace();
		    }catch (Exception e) {
			}
		}
		Date currentDate = new Date();
		if(convertedDate.compareTo(currentDate)>0)
		{
			//Util.showToast("Valid");
			return true;
		}
		else
		{
			//Util.showToast("Invalid");
			return false;
		}

	}

	public static void showAdultToast(String msg, CardData data, Context context) {
		if (context == null || data == null || data.content == null
				|| data.content.certifiedRatings == null
				|| data.content.certifiedRatings.values == null
				|| data.content.certifiedRatings.values.isEmpty()
				|| data.content.certifiedRatings.values.get(0).rating == null) {
			return;
		}
		if (data.content.certifiedRatings.values.get(0).rating
				.equalsIgnoreCase("A") 
				|| data.content.certifiedRatings.values.get(0).rating
						.equalsIgnoreCase("R")) {
			try {
				Toast toast = new Toast(context);
				LayoutInflater inflate = LayoutInflater.from(context);
				View v = inflate.inflate(R.layout.toastlayout, null);
				TextView header = (TextView) v.findViewById(R.id.toast_type);
				header.setTypeface(FontUtil.ss_symbolicons_line);
				TextView message = (TextView) v.findViewById(R.id.toast_text);
				message.setTypeface(FontUtil.Roboto_Medium);
				header.setText(R.string.toast_warning);
				message.setTextColor(Color.parseColor("#FF0000"));
				v.setBackgroundColor(Color.parseColor("#FFFFFF"));
				message.setText(msg);
				toast.setGravity(Gravity.TOP, 0, 100);
				toast.setView(v);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.show();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	public static void closeKeyBoard(Context context,View view){
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static String getInternetConnectivity(Context context) {
		String network_type = "";
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo active_network = manager.getActiveNetworkInfo();

		if (active_network == null) {
			return network_type;
		}
		switch (active_network.getType()) {
		case ConnectivityManager.TYPE_WIFI:
			network_type = "wifi";
			break;
		case ConnectivityManager.TYPE_MOBILE:
			if (active_network.getSubtype() > TelephonyManager.NETWORK_TYPE_EDGE) {
				network_type = "3G";
			}

			switch (active_network.getSubtype()) {
			case TelephonyManager.NETWORK_TYPE_GPRS:

			case TelephonyManager.NETWORK_TYPE_EDGE:

				network_type = "2G";
				break;

			}
		}
		return network_type;

	}
	
	/**
	 *  After watching a free content just show this Dialog
	 */
	public static  void showFacebookShareDialog(Context context) {
		mContext = context;
		Long lastwatchedTime  = SharedPrefUtils.getLongFromSharedPreference(mContext, mContext.getString(R.string.lastSharedTime));
		long difference  = lastwatchedTime -  System.currentTimeMillis();
			if( lastwatchedTime == 0 || ((difference /(1000*60*60*24) ))>= 1){
				SharedPrefUtils.writeToSharedPref(mContext, mContext.getString(R.string.lastSharedTime), System.currentTimeMillis());
			}else{
				return;
			}		
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra("com.facebook.platform.extra.TITLE","title");
		
		shareIntent.putExtra("com.facebook.platform.extra.DESCRIPTION","DESC   ");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "www.myplex.com");
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
		for (final ResolveInfo app : activityList) {
			if ((app.activityInfo.packageName).contains("com.facebook.katana")) {
				shareIntent.setPackage("com.facebook.katana");
				mContext.startActivity(shareIntent);
				break;
			}
		}
	}
	
	public static String getDate(String dateInString) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = format.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(date!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
			sdf.setTimeZone(TimeZone.getDefault());			
			return sdf.format(date).toString();
		}else 
			return null;
	}
	
	public static String getExpiry(String dateInString) {
		Log.d(TAG," got time ="+dateInString);
		String expiryMessage =  "";
		Date now  = new Date();
		long difference = 0;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = format.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(date!=null){
			difference = (date.getTime() - now.getTime() );
			long seconds = difference / 1000;
			long minutes = seconds / 60;
			long hours = minutes / 60;
			long days = hours / 24;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			Log.d(TAG," difference ="+difference+ "		sec"+seconds+"	min"+minutes+"	hours"+hours+"	days"+days);
			if(days > 365){
				Log.d(TAG,"Watch anytime");
				return "watch anytime";
			}else if(days>7){
				return "watch until "+calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
						+" "+calendar.get(Calendar.DAY_OF_MONTH);
			}else if(days >2){
				return "watch for next "+days+" days";
			}/*else if(days >0){
				return "watch for "+((int)(24*days)+(calendar.get(Calendar.HOUR)))+" hrs";
			}*/else if(days>=0 && hours>=2){
				return "watch for next "+(int)(hours)+ " hrs";
			}else if(hours<=2 && minutes>1){
				return "watch now (expires in "+minutes+" mins)";
			}else if(minutes<=1 && seconds >1 ){
				return "watch in "+seconds+" secs";
			}else{
				return "watch now";
			}
		}		
		return expiryMessage;
	}
	
	public static void showNotification(Context context, String title) {	
		
		
		Intent notificationIntent = new Intent(context,	LoginActivity.class);
		PendingIntent contentIntent =  PendingIntent.getActivity(context, 0, notificationIntent, 0);
		
		PackageManager manager = context.getPackageManager();
		Intent appIntent = manager.getLaunchIntentForPackage(context.getPackageName());
		
		 try {
	            ApplicationInfo appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
	            CharSequence notificationTitle = manager.getApplicationLabel(appInfo);
	            int notificationIcon = appInfo.icon;
	            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	            NotificationCompat.Builder mBuilder =
	            		new NotificationCompat.Builder(context)
	            .setSmallIcon(notificationIcon)
	            .setContentTitle(notificationTitle)
	            .setContentText(title+" Download Complete")
	            .setContentIntent(contentIntent);
	            Notification notification = mBuilder.build();
	            notification.flags = Notification.FLAG_AUTO_CANCEL;
	            nm.notify(1, notification);	
	        } catch (NameNotFoundException e) {
	            // In this case, use a blank title and default icon
	        }catch (Exception e) {
			}

	}

	public static String getAppVersionName(Context context){
		
		String version = "";
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(
					context.getPackageName(), 0);
			if(!TextUtils.isEmpty(info.versionName)){
				version = " " + info.versionName;
			}
		} catch (Exception e) {			
			return "";
		}
		return version;
	}

	public static String getAppVersionNumber(Context context){
		
		String versionCode = "";
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(
					context.getPackageName(), 0);
			if(info.versionCode != 0){
				versionCode = info.versionCode+"";
			}
		} catch (Exception e) {			
			return "";
		}
		return versionCode;
	}

	public static boolean isInvalidSession(Context context, String response,KeyRenewListener keyRenewListener) {
		try {
			Util.keyRenewListener = keyRenewListener;
			mContext = context;
			JSONObject json = new JSONObject(response);
			if(json.getString("status").equalsIgnoreCase("ERR_INVALID_SESSION_ID") && json.getInt("code")== 401){
				Log.d(TAG,"ERR_INVALID_SESSION_ID");
				String devId=SharedPrefUtils.getFromSharedPreference(mContext,mContext.getString(R.string.devclientdevid));
				Map<String, String> params = new HashMap<String, String>();
				params.put("deviceId", devId);
				Util.genKeyRequest(mContext,mContext.getString(R.string.genKeyReqPath),params);
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setKeyListener(KeyRenewListener listener){
		Util.keyRenewListener = listener;
	}
	
	public interface KeyRenewListener {
        public void onKeyRenewed();
        public void onKeyRenewFailed(String message);
    }
	
	public static boolean isExpiredResponseAllowed(Request<?> request) {

		if (request == null || request.getUrl() == null)
			return false;

		if (request.getUrl().contains(ConsumerApi.CONTENTLIST)
				|| request.getUrl()
						.contains(ConsumerApi.RECOMMENDATIONS_ACTION)) {
			return true;
		}

		return false;
	}

	public static DefaultRetryPolicy getRetryPolicy(int req_type,
			Context context) {

		int timeout = 15;

		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo active_network = manager.getActiveNetworkInfo();

		if (active_network != null) {

			switch (active_network.getType()) {

			case ConnectivityManager.TYPE_WIFI:
				timeout = 7;
				break;
			case ConnectivityManager.TYPE_MOBILE:

				if (active_network.getSubtype() > TelephonyManager.NETWORK_TYPE_EDGE) {
					timeout = 10;
				}

				switch (active_network.getSubtype()) {
				
				case TelephonyManager.NETWORK_TYPE_GPRS:

				case TelephonyManager.NETWORK_TYPE_EDGE:

					timeout = 15;
					break;

				}
			}
		}

		DefaultRetryPolicy defaultRetryPolicy = new DefaultRetryPolicy();

		switch (req_type) {

		case CardExplorerData.REQUEST_BROWSE:
		case CardExplorerData.REQUEST_RECOMMENDATION:
			defaultRetryPolicy = new DefaultRetryPolicy(timeout * 1000, 2, 1f);
			break;

		default:
			break;
		}

		return defaultRetryPolicy;
	}

}

