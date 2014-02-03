package com.apalya.myplex.widget;

import android.app.Activity;
import android.os.Bundle;

import com.apalya.myplex.R;

public class WidgetHandlerActivity extends Activity 
{
	private int mAppWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myplex_widget_layout);
	}

}
