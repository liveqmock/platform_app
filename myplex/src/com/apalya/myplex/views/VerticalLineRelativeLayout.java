package com.apalya.myplex.views;

import com.apalya.myplex.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class VerticalLineRelativeLayout extends RelativeLayout {
	public VerticalLineRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);

		// TODO Auto-generated constructor stub
	}

	public VerticalLineRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);

		// TODO Auto-generated constructor stub
	}

	public VerticalLineRelativeLayout(Context context) {
		super(context);
		setWillNotDraw(false);

		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int startX =(int)getContext().getResources().getDimension(R.dimen.margin_gap_4);
		View child = getChildAt(0);
		Rect rect = new Rect();
		child.getGlobalVisibleRect(rect);
		rect.width();
		rect.height();
		Paint paint = new Paint();
		paint.setColor(Color.parseColor("#efefef"));
		paint.setStrokeWidth(6f);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawLine((startX+(rect.width()/2)), rect.height(), (startX+(rect.width()/2)), getHeight(), paint);
		super.onDraw(canvas);
	}

}
