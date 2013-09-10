package com.apalya.myplex.views;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

public class CustomDialog extends Dialog{

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public CustomDialog(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	protected CustomDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}
}
