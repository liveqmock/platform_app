package com.mixpanel.android.mpmetrics;

import com.mixpanel.android.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI.InstanceProcessor;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

/**
* BroadcastReciever for handling Google Cloud Messaging intents.
*
* <p>You can use GCMReciever to report Google Cloud Messaging registration identifiers
* to Mixpanel, and to display incoming notifications from Mixpanel to
* the device status bar. Together with {@link MixpanelAPI.People#initPushHandling(String) }
* this is the simplest way to get up and running with notifications from Mixpanel.
*
* <p>To enable GCMReciever in your application, add a clause like the following
* to the &lt;application&gt; tag of your AndroidManifest.xml. (Be sure to replace "YOUR APPLICATION PACKAGE NAME"
* in the snippet with the actual package name of your app.)
*
*<pre>
*{@code
*
* <receiver android:name="com.mixpanel.android.mpmetrics.GCMReceiver"
* android:permission="com.google.android.c2dm.permission.SEND" >
* <intent-filter>
* <action android:name="com.google.android.c2dm.intent.RECEIVE" />
* <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
* <category android:name="YOUR APPLICATION PACKAGE NAME" />
* </intent-filter>
* </receiver>
*
*}
*</pre>
*
* <p>In addition, GCMReciever will also need the following permissions configured
* in your AndroidManifest.xml file:
*
* <pre>
* {@code
*
* <!-- Be sure to change YOUR_PACKAGE_NAME to the real name of your application package -->
* <permission android:name="YOUR_PACKAGE_NAME.permission.C2D_MESSAGE" android:protectionLevel="signature" />
* <uses-permission android:name="YOUR_PACKAGE_NAME.permission.C2D_MESSAGE" />
*
* <uses-permission android:name="android.permission.INTERNET" />
* <uses-permission android:name="android.permission.GET_ACCOUNTS" />
* <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
* <uses-permission android:name="android.permission.WAKE_LOCK" />
*
* }
* </pre>
*
* <p>Once the GCMReciever is configured, the only thing you have to do to
* get set up Mixpanel messages is call {@link MixpanelAPI.People#identify(String) }
* with a distinct id for your user, and call {@link MixpanelAPI.People#initPushHandling(String) }
* with the your Google API project identifier.
* <pre>
* {@code
*
* MixpanelAPI.People people = mMixpanelAPI.getPeople();
* people.identify("A USER DISTINCT ID");
* people.initPushHandling("123456789123");
*
* }
* </pre>
*
* <p>If you would prefer to handle either sending a registration id to Mixpanel yourself
* but allow GCMReciever to handle displaying Mixpanel messages, remove the
* REGISTRATION intent from the GCMReciever {@code <reciever> } tag, and call
* {@link MixpanelAPI.People#setPushRegistrationId(String)}
* in your own REGISTRATION handler.
*
* @see MixpanelAPI#getPeople()
* @see MixpanelAPI.People#initPushHandling(String)
* @see <a href="https://mixpanel.com/docs/people-analytics/android-push">Getting Started with Android Push Notifications</a>
*/
public class GCMReceiver extends BroadcastReceiver {
    String LOGTAG = "MPGCMReceiver";
//    public static final String PLAYSTORE_DOWNLOAD_PATH="market://details?id=com.apalya.myplex&referrer=utm_source%3Dgoogle%26utm_medium%3Dcpc%26utm_term%3Drunning%252Bshoes%26utm_content%3DdisplayAd1%26utm_campaign%3Dshoe%252Bcampaign";    
    public static final String PLAYSTORE_DOWNLOAD_PATH="market://details?id=";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
            handleRegistrationIntent(intent);
        } else if ("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
            handleNotificationIntent(context, intent);
        }
    }

    private void handleRegistrationIntent(Intent intent) {
        final String registration = intent.getStringExtra("registration_id");
        if (intent.getStringExtra("error") != null) {
            Log.e(LOGTAG, "Error when registering for GCM: " + intent.getStringExtra("error"));
        } else if (registration != null) {
            if (MPConfig.DEBUG) Log.d(LOGTAG, "registering GCM ID: " + registration);
            MixpanelAPI.allInstances(new InstanceProcessor() {
                @Override
                public void process(MixpanelAPI api) {
                    api.getPeople().setPushRegistrationId(registration);
                }
            });
        } else if (intent.getStringExtra("unregistered") != null) {
            if (MPConfig.DEBUG) Log.d(LOGTAG, "unregistering from GCM");
            MixpanelAPI.allInstances(new InstanceProcessor() {
                @Override
                public void process(MixpanelAPI api) {
                    api.getPeople().clearPushRegistrationId();
                }
            });
        }
    }

    private void handleNotificationIntent(Context context, Intent intent) {
        final String message = intent.getExtras().getString("mp_message");
        final String _id = intent.getExtras().getString("_id");
        final String latestVersion = intent.getExtras().getString("ver");
        
        final String page = intent.getExtras().getString("page");
        
        if (message == null) return;
        if (MPConfig.DEBUG) Log.d(LOGTAG, "MP GCM notification received: " + message);
        if (MPConfig.DEBUG) Log.d(LOGTAG, "MP GCM notification _id received: " + _id);

        final PackageManager manager = context.getPackageManager();
         Intent appIntent = manager.getLaunchIntentForPackage(context.getPackageName());
        CharSequence notificationTitle = "";
        int notificationIcon = android.R.drawable.sym_def_app_icon;
        try {
            final ApplicationInfo appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
            notificationTitle = manager.getApplicationLabel(appInfo);
            notificationIcon = appInfo.icon;
            
        } catch (final NameNotFoundException e) {
            // In this case, use a blank title and default icon
        }
        
        if(!TextUtils.isEmpty(_id)){
        	appIntent.putExtra("_id", _id);
        }else if (!TextUtils.isEmpty(page)){
        	appIntent.putExtra("page", page);
        }else if (!TextUtils.isEmpty(latestVersion)){        	
        	
        	if (MPConfig.DEBUG) Log.d(LOGTAG, "MP GCM notification latestVer received: " + latestVersion);
        	
    		PackageInfo info;
    		try {
    			int latVer = Integer.valueOf(latestVersion);
    			info = manager.getPackageInfo(
    					context.getPackageName(), 0);
    			 				
				if (latVer <= info.versionCode) {
					// Ignore , app is already up to date.
					if (MPConfig.DEBUG)
						Log.d(LOGTAG, "Ignore , app is already up to date");
					return;
				}
    			
    		} catch (Throwable e) {			
    			return ;
    		}
    		
        	appIntent = new Intent(Intent.ACTION_VIEW);
        	appIntent.setData( Uri.parse(PLAYSTORE_DOWNLOAD_PATH+context.getPackageName()));
        }
        
        final PendingIntent contentIntent = PendingIntent.getActivity(
            context.getApplicationContext(),
            0,
            appIntent, // add this pass null to intent
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (Build.VERSION.SDK_INT < 11) {
            showNotificationSDKLessThan11(context, contentIntent, notificationIcon, notificationTitle, message);
        } else {
        	 RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.push_notification);
      
        	 remoteViews.setTextViewText(R.id.push_subject,notificationTitle);
             remoteViews.setTextViewText(R.id.push_message,message);
             remoteViews.setImageViewResource(R.id.push_icon,notificationIcon);
             
            showNotificationSDK11OrHigher(context, contentIntent, notificationIcon, notificationTitle, message, remoteViews);
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(8)
    private void showNotificationSDKLessThan11(Context context, PendingIntent intent, int notificationIcon, CharSequence title, CharSequence message) {
        final NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification n = new Notification(notificationIcon, message, System.currentTimeMillis());
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        n.setLatestEventInfo(context, title, message, intent);
        nm.notify(0, n);
    }

    @SuppressWarnings("deprecation")
	private void showNotificationSDK11OrHigher(Context context, PendingIntent intent, int notificationIcon, CharSequence title, CharSequence message, RemoteViews remoteViews) {
        final NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context).
                setSmallIcon(notificationIcon).
                setTicker(message).
                setWhen(System.currentTimeMillis()).
                setContentTitle(title).
                setContentText(message).
                setContentIntent(intent).setContent(remoteViews);
        Notification n;
        if (Build.VERSION.SDK_INT < 16) {
            n = builder.getNotification();
        } else {
            n = builder.build();
        }

        n.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(0, n);
    }
}