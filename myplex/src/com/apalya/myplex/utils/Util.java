package com.apalya.myplex.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.apalya.myplex.R;
import com.apalya.myplex.data.ApplicationConfig;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;
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

public class Util {
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
			ConsumerApi.DOMAIN = activity.getString(R.string.domain_name);
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
					v.setBackgroundColor(Color.CYAN);
					break;
				case MotionEvent.ACTION_UP:
					v.setBackgroundColor(Color.TRANSPARENT);
					break;
				default:
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
	public void startDownload(String aUrl,String aMovieName,Context mContext)
	{
		if(isDownloadManagerAvailable(mContext))
		{
			
			//String url = "http://220.226.22.120:9090/aptv3-downloads/appdevclip.wvm";
			String url=aUrl;
			
			int val=url.length()-url.lastIndexOf("/");
			String filename="";
			if(val>0)
				filename= url.substring(val, url.length()-1);
			else
				filename= aMovieName+".wvm";
			
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription(aMovieName);
			request.setTitle("Myplex Downloads");
			// in order for this if to run, you must use the android 3.2 to compile your app
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			    request.allowScanningByMediaScanner();
			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
		}
		else
		{
			//Download Manager is not available
		}
	}
	public static void InviteFriends(final Context mContext) {
		
		if(Session.getActiveSession()!=null)
		{
			if(Session.getActiveSession().isOpened())
			{
			Bundle params = new Bundle();

			params.putString("message", "Hey, Check out this cool app where we can watch movies and live tv shows.");
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
											Toast.makeText(mContext, 
													"Network Error", 
													Toast.LENGTH_SHORT).show();
										}
									} else {
										final String requestId = values.getString("request");
										if (requestId != null) {
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
	public static void shareData(Context mContext,int aType,String aPath,String aCaption){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		//sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
		sendIntent.putExtra(Intent.EXTRA_TEXT, aCaption);
		if(aType==1)
		{
			//Uri picUri = Uri.fromFile(new File("/storage/sdcard0/DCIM/Camera/IMG_20131002_161648.jpg"));
			Uri picUri = Uri.fromFile(new File(aPath));
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
		Intent i = new Intent();
		i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
		mContext.startActivity(i);
	}
}
