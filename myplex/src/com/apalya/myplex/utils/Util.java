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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
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
import android.database.Cursor;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.MainActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.R.color;
import com.apalya.myplex.adapters.CacheManagerCallback;
import com.apalya.myplex.cache.CacheManager;
import com.apalya.myplex.cache.IndexHandler;
import com.apalya.myplex.data.ApplicationConfig;
import com.apalya.myplex.data.CardData;
import com.apalya.myplex.data.myplexapplication;
import com.apalya.myplex.fragments.CardExplorer;
import com.apalya.myplex.tablet.MultiPaneActivity;
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
			myplexapplication.getApplicationConfig().downloadCardsPath =  activity.getFilesDir()+"/"+"downloadlist.bin";
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
	public static long startDownload(String aUrl,String aMovieName,Context mContext)
	{
		long lastDownloadId=-1L;
		if(isDownloadManagerAvailable(mContext))
		{
			try{
			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);


			//Uri uri=Uri.parse("http://commonsware.com/misc/test.mp4");
			Uri uri=Uri.parse(aUrl);

			Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			.mkdirs();

			lastDownloadId=
					manager.enqueue(new DownloadManager.Request(uri)
					.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
							.setAllowedOverRoaming(false)
							.setTitle("myplex")
							.setDescription(aMovieName)
							.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
									aMovieName+".wvm"));
			if(lastDownloadId>0)
			Util.showToast("Your Download has been started, Please check progress in Downloads Section",mContext);
			
			
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
		return lastDownloadId;
	}
	public static int checkDownloadStatus(String cardId,Context mContext){
		
		
		Map<String, Long> ids=myplexapplication.getUserProfileInstance().downloadMap;
		if(ids.get(cardId)==null){
			return 0;
		}
		
		/*final long dwnlId=ids.get(cardId);
		
		final DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

		new Thread(new Runnable() {
			
			@Override
			public void run() {
					int dStatus;
					DownloadManager.Query q = new DownloadManager.Query();
					q.setFilterById(dwnlId);

					Cursor cursor = manager.query(q);
					cursor.moveToFirst();
					dStatus=cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
					cursor.close();
					
			}
			
		}).start();*/
		return 1;
		
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
	public static void shareData(Context mContext,int aType,String aPath,String aTitle){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		//sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
		String msg="Watching "+aTitle+" on myplex \n check it out on http://portal.myplex.in/ \n https://play.google.com/store/apps/details?id=tv.myplex.android";
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
				//Analytics.endTimedEvent("NEW-CLIENT-KEY-GENERATION");
				//Analytics.trackEvent("NEW-CLIENT-KEY-GENERATION-ERROR");
				Log.d(TAG,"Error: "+error.toString());
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
						//Analytics.trackEvent("NEW-CLIENT-KEY-GENERATION-Success");
						Log.d(TAG, "status: "+jsonResponse.getString("status"));
						Log.d(TAG, "expiresAt: "+jsonResponse.getString("expiresAt"));
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Log.d(TAG, "clientKey: "+jsonResponse.getString("clientKey"));
						Log.d(TAG, "########################################################");
						myplexapplication.getDevDetailsInstance().setClientKey(jsonResponse.getString("clientKey"));
						myplexapplication.getDevDetailsInstance().setClientKeyExp(jsonResponse.getString("expiresAt"));
						Log.d(TAG, "---------------------------------------------------------");



						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkey), jsonResponse.getString("clientKey"));
						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkeyexp), jsonResponse.getString("expiresAt"));


					}
					else
					{

						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkey), "");
						SharedPrefUtils.writeToSharedPref(mContext,
								mContext.getString(R.string.devclientkeyexp), "");

						//Analytics.trackEvent("NEW-CLIENT-KEY-GENERATION-SERVER-ERROR");
						Log.d(TAG, "code: "+jsonResponse.getString("code"));
						Log.d(TAG, "message: "+jsonResponse.getString("message"));
						Util.showToast("Code: "+jsonResponse.getString("code")+" Msg: "+jsonResponse.getString("message"),mContext);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
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
		
		ConnectivityManager cm =
		        (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		
		
		boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		return isWiFi;
	}
	public static void saveObject(Object obj,String path) {		try {			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path))); 			oos.writeObject(obj); 			oos.flush(); 			oos.close();		} catch (Exception ex) {			Log.v("Util", ex.getMessage());			ex.printStackTrace();		}	}	public static Object loadObject(String path) {		try {			File f = new File(path);			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));			Object o = ois.readObject();			return o;		} catch (Exception ex) {			Log.v("Util", ex.getMessage());			ex.printStackTrace();		}		return null;	}
}
