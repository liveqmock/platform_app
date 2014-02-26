package com.apalya.myplex.views;

import java.util.Timer;
import java.util.TimerTask;

import com.apalya.myplex.R;
import com.apalya.myplex.data.ApplicationSettings;
import com.apalya.myplex.utils.SharedPrefUtils;
import com.apalya.myplex.utils.Util;
import com.crashlytics.android.Crashlytics;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;

public class SensorScrollUtil implements SensorEventListener {

	private ScrollView mScrollView;
	private boolean mEnabled = true;
	private boolean mSensorActivated = false;

	private SensorManager sensorManager = null;
	private Context mContext;
	private Timer timer = null;
	private static final String TAG = "SensorScrollUtil";

	public static final int Y_MIN = 0;

	public static final int Y_SCROLL_START = 4;
	public static final int Y_SCROLL_END = 7;

	private float mInitialYPosition = -1;
	private float lastYPosition = 0;

	public void register(Context context) {

		if (context == null) {
			return;
		}
		
		if (!ApplicationSettings.ENABLE_SENSOR_SCROLL) {
			return;
		}

		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);

		this.mContext = context;

	}

	public void init(ScrollView scrollView) {

		if (mContext == null || scrollView == null) {
			return;
		}

		this.mScrollView = scrollView;

		try {
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					sensorManager.SENSOR_DELAY_UI);
		} catch (Throwable e) {
			Crashlytics.logException(e);
			mScrollView = null;
			return;
		}

		mScrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				Log.d(TAG, "onTouch");
				if (!mSensorActivated) {
					return false;
				}

				mEnabled = false;
				mSensorActivated = false;
				mInitialYPosition = -1;
				lastYPosition = 0;

				Util.showToast(mContext, "Sensor deactivated",
						Util.TOAST_TYPE_INFO);

				if (timer != null) {
					timer.cancel();
				}

				timer = new Timer();

				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						Util.showToast(mContext, "Sensor activated",
								Util.TOAST_TYPE_INFO);
						mEnabled = true;
					}
				}, 3000);

				return false;
			}
		});
		setEnabled(true);
	}

	public void unregister() {

		if (sensorManager == null) {
			return;
		}

		try {
			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		} catch (Throwable e) {
			Crashlytics.logException(e);
		}

	}

	public void setEnabled(boolean enabled) {
		this.mEnabled = enabled;

	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		if (!mEnabled) {
			return;
		}

		switch (event.sensor.getType()) {

		case Sensor.TYPE_ACCELEROMETER:

			// Log.d("TAG", "x:" + Float.toString(event.values[0]));
			// Log.d("TAG", "y:" + Float.toString(event.values[1]));
			// Log.d("TAG", "z:" + Float.toString(event.values[2]));

			if (mScrollView == null) {
				return;
			}

			float y = event.values[1];

			if (mSensorActivated && event.values[1] < Y_MIN) {
				mSensorActivated = false;
				Util.showToast(mContext, "Sensor deactivated",
						Util.TOAST_TYPE_INFO);
				return;
			}

			if (!mSensorActivated && y <= Y_SCROLL_END && y >= Y_SCROLL_START) {
				mSensorActivated = true;
				mInitialYPosition = y;
				Util.showToast(mContext, "Sensor activated",
						Util.TOAST_TYPE_INFO);
				return;
			}

			if (mSensorActivated) {
				int fling = Math.round((mInitialYPosition - y) * 40);

				if (fling != 0 && lastYPosition != fling) {
					lastYPosition = fling;

					mScrollView.smoothScrollTo(mScrollView.getScrollX(),
							mScrollView.getScrollY() + fling);
				}
			}

			break;

		case Sensor.TYPE_ORIENTATION:

			break;

		}

	}
}
