package com.apalya.myplex.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.R;

public class ReminderReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
		
	
		 
				Bundle extras=intent.getExtras();
				if(extras == null || 
						extras.getString("_id")==null ||
							extras.getString("_id").length() <1)
					return;
					
				Intent notificationIntent = new Intent(context, LoginActivity.class);
				notificationIntent.putExtra("_id", extras.getString("_id"));
				PendingIntent contentIntent = PendingIntent.getActivity(context,
				                0, notificationIntent,
				                PendingIntent.FLAG_CANCEL_CURRENT);

				NotificationManager nm = (NotificationManager) context
				                .getSystemService(Context.NOTIFICATION_SERVICE);

				Resources res = context.getResources();
				NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
				
				builder.setContentIntent(contentIntent)
				       .setSmallIcon(R.drawable.myplexicon)
				       .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.myplexicon))
				       .setTicker("myplex")
				       .setWhen(System.currentTimeMillis())
				       .setAutoCancel(true)
				       .setContentTitle(extras.getString("title"))
				       .setContentText(extras.getString("note"));
				Notification n = builder.getNotification();

				n.defaults |= Notification.DEFAULT_ALL;
				nm.notify(0, n);		

	}

}
