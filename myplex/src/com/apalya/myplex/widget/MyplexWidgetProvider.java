package com.apalya.myplex.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.apalya.myplex.LoginActivity;
import com.apalya.myplex.MainActivity;
import com.apalya.myplex.R;
import com.apalya.myplex.utils.ConsumerApi;

public class MyplexWidgetProvider extends AppWidgetProvider 
{
	private static String LIVE_TV = "live_tv";
	private static String MOVIE = "movie";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) 
	{
		RemoteViews remoteViews;
		ComponentName watchWidget;

		remoteViews = new RemoteViews(context.getPackageName(), R.layout.myplex_widget_layout);
		watchWidget = new ComponentName(context, MyplexWidgetProvider.class);

		remoteViews.setOnClickPendingIntent(R.id.livetv_image, getPendingSelfIntent(context, LIVE_TV));
		remoteViews.setOnClickPendingIntent(R.id.movie_image, getPendingSelfIntent(context, MOVIE));
		appWidgetManager.updateAppWidget(watchWidget, remoteViews);

	}
	 @Override
	    public void onReceive(Context context, Intent intent) {
	        // TODO Auto-generated method stub
	        super.onReceive(context, intent);

	        if (LIVE_TV.equals(intent.getAction())) {
	            Log.d("amlan",intent.getAction()+" clicked");
	            Intent liveTvIntent = new Intent(context, MainActivity.class);
	            liveTvIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            liveTvIntent.putExtra(context.getString(R.string.page), ConsumerApi.VIDEO_TYPE_LIVE);
	        	context.startActivity(liveTvIntent);

	        }else if(MOVIE.equalsIgnoreCase(intent.getAction())){
	        	Log.d("amlan",intent.getAction()+" clicked");
	        	Intent movieIntent = new Intent(context, MainActivity.class);
	        	movieIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	movieIntent.putExtra(context.getString(R.string.page), ConsumerApi.VIDEO_TYPE_MOVIE);
	        	context.startActivity(movieIntent);
	        }
	    }

	    protected PendingIntent getPendingSelfIntent(Context context, String action) {
	        Intent intent = new Intent(context, getClass());
	        intent.setAction(action);
	        return PendingIntent.getBroadcast(context, 0, intent, 0);
	    }
	}

