package com.apalya.myplex.utils;


import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.MainActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.TwitterWebView;
import com.apalya.myplex.data.myplexapplication;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

public class Twitter11{
	private final String TWITTER_CONSUMER_KEY;
	private final String TWITTER_CONSUMER_SECRET;
	private final String TWITTER_CALLBACK_URL;
	//public static String COM_REPLY= "com.twitter11.reply";
	//
	public static String COM_REPLY= "http://www.myplex.tv/";
	static final String PREF_KEY_OAUTH_TOKEN= "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET= "oauth_token_secret";
	static final String URL_TWITTER_AUTH= "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER= "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN= "oauth_token";

	ProgressDialog pDialog;
	
	private static Twitter twitter= null;
	private static RequestToken requestToken;
	private SharedPreferences mSharedPreferences;
	Activity activity;
	private TwitterAuthenticateCallback mCallback;
	
	public static interface TwitterAuthenticateCallback {
       public void onTwitterLogin(String token,String secret);
    }
	
	public Twitter11(Activity act,TwitterAuthenticateCallback callback, int appname, SharedPreferences pref, String consumerkey, String consumersecret){
		//super();
		this.activity= act;
		this.mCallback=callback;
		this.mSharedPreferences= pref;
		this.TWITTER_CONSUMER_KEY= consumerkey;
		this.TWITTER_CONSUMER_SECRET= consumersecret;
		this.TWITTER_CALLBACK_URL= "oauth://" + act.getString(appname);
	}
	public void login(){
		new TwitterLoginTask("").execute();
	}
	public void logout(){
		Editor e= mSharedPreferences.edit();
		e.putString(PREF_KEY_OAUTH_TOKEN, null);
		e.putString(PREF_KEY_OAUTH_SECRET, null);
		e.commit();
	}
	public void sendtweet(String msg){
		if(msg!=null && msg.trim().length() > 0)
				new updateTwitterStatus().execute(msg);
	}
	public boolean isloggedin(){
		String access_token= mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null);
		String access_token_secret= mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null);
		return access_token!=null && access_token_secret!=null;
	}
	private ProgressDialog progressor(String title){
		ProgressDialog p= new ProgressDialog(activity, ProgressDialog.THEME_HOLO_LIGHT);
		p.setTitle(title);
		p.setIndeterminate(true);
		p.setCancelable(false);
		p.show();
		return p;
	}
	public void logincallback(Intent i, Runnable r){
		final Runnable postloginrunnable= r;
		Uri uri= i.getData();
		if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)){
			new AsyncTask<Uri, Void, String>(){
				String errmsg= null;
				String access_token= null, access_token_secret= null;
				ProgressDialog progress;
				
				@Override
				protected void onPreExecute(){
					super.onPreExecute();
					progress= progressor("Logging in...");
				}
				@Override
				protected String doInBackground(Uri... uris){
					String verifier= uris[0].getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
					try{
						AccessToken accessToken= twitter.getOAuthAccessToken(requestToken, verifier);
						// Shared Preferences
						Editor ed= mSharedPreferences.edit();
						ed.putString(PREF_KEY_OAUTH_TOKEN, access_token= accessToken.getToken());
						ed.putString(PREF_KEY_OAUTH_SECRET, access_token_secret= accessToken.getTokenSecret());

						Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

						long userID= accessToken.getUserId();
						User user= twitter.showUser(userID);
						String username= user.getName();
						myplexapplication.getUserProfileInstance().setUserId(String.valueOf(userID));
						myplexapplication.getUserProfileInstance().setName(user.getName());
						myplexapplication.getUserProfileInstance().setUserEmail(user.getScreenName());
						myplexapplication.getUserProfileInstance().setProfilePic(user.getBiggerProfileImageURL());
						
						ed.putString("twitter_name", username);
						ed.commit();
						Log.e("UserID: ", "userID: " + userID + "" + username);
						Log.v("Welcome:", "Thanks:" + Html.fromHtml("<b>Welcome " + username + "</b>"));
					}catch(Exception e){
						errmsg= e.getMessage();
						Log.e("Twitter Login Error", "> " +errmsg);
					}
					return null;
				}
				@Override
				protected void onPostExecute(String result){
					if(errmsg!=null){
//						Toast.makeText(activity, "Twitter Login Error: "+errmsg, Toast.LENGTH_SHORT).show();
						Util.showToast(activity,"Twitter Login Error: "+errmsg,Util.TOAST_TYPE_ERROR);
					}
					progress.dismiss();
					if(postloginrunnable != null)
						postloginrunnable.run();
					String access_token= mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null);
					String access_token_secret= mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null);
					mCallback.onTwitterLogin(access_token, access_token_secret);
						/*activity.finish();
						Intent intent = new Intent(activity, MainActivity.class);
						activity.startActivity(intent);*/
					
				}
				@Override
				protected void onCancelled(String result){
					if(errmsg==null)
						errmsg= "Cancelled";
					onPostExecute(result);
				}
			}.execute(uri);
		}
	}
	private class TwitterLoginTask extends AsyncTask<String, Void, String>{
		ConfigurationBuilder builder= new ConfigurationBuilder();
		ProgressDialog progress;
		String errmsg= null;
		
		public TwitterLoginTask(String bmImage) {
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration= builder.build();

			TwitterFactory factory= new TwitterFactory(configuration);
			twitter= factory.getInstance();
		}
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			progress= progressor("Login...");
		}
		protected String doInBackground(String... urls){
			try{
				requestToken= twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				Uri urv= Uri.parse(requestToken.getAuthenticationURL() + "&force_login=true");
				Log.i("TAG", urv.toString());
				Intent intent= new Intent(activity, TwitterWebView.class)
					.setData(urv)
					.putExtra("callback", TWITTER_CALLBACK_URL);
				activity.startActivityForResult(intent, LoginActivity.TWITTER_CALLBACK);
			}catch(TwitterException e){
				errmsg= e.getMessage();
				Log.e("Twitter Login Error", "> " +errmsg);
			}
			return "result";
		}
		@Override
		protected void onPostExecute(String result){
			progress.dismiss();
			if(errmsg!=null){
//				Toast.makeText(activity, "Twitter Login Error: "+errmsg, Toast.LENGTH_SHORT).show();
				Util.showToast(activity,"Twitter Login Error: "+errmsg,Util.TOAST_TYPE_ERROR);
			}
		}
		@Override
		protected void onCancelled(String result){
			if(errmsg==null)
				errmsg= "Cancelled";
			progress.dismiss();
		}
	}
	
	private class updateTwitterStatus extends AsyncTask<String, String, String>{
		String errmsg= null;
		ProgressDialog progress;
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			progress= progressor("Tweeting...");
		}
		protected String doInBackground(String... args){
			Log.d("Tweet Text", "> " + args[0]);
			String status= args[0];
			try{
				ConfigurationBuilder builder= new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
				String access_token= mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				String access_token_secret= mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken= new AccessToken(access_token, access_token_secret);
				Twitter twitter= new TwitterFactory(builder.build()).getInstance(accessToken);

				// Update status
				twitter4j.Status response= twitter.updateStatus(status);
				Log.d("Status", "> " + response.getText());
			}catch(TwitterException e){
				errmsg= e.getMessage();
				Log.d("Twitter Update Error", errmsg);
			}
			return null;
		}
		@Override
		protected void onPostExecute(String file_url){
			progress.dismiss();
			if(errmsg==null)
				errmsg= "Tweet Succeeded";
			Toast.makeText(activity, errmsg, Toast.LENGTH_SHORT).show();
		}
		@Override
		protected void onCancelled(String result){
			if(errmsg==null)
				errmsg= "Cancelled";
			onPostExecute(result);
		}
	}
}
