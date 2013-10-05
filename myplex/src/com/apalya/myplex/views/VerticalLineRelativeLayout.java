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
	private Paint paint = new Paint();
	public VerticalLineRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		createPaint();
		// TODO Auto-generated constructor stub
	}

	public VerticalLineRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		createPaint();
		// TODO Auto-generated constructor stub
	}

	public VerticalLineRelativeLayout(Context context) {
		super(context);
		setWillNotDraw(false);
		createPaint();
		// TODO Auto-generated constructor stub
	}
	private void createPaint(){
		paint.setColor(Color.parseColor("#efefef"));
		paint.setStrokeWidth(6f);
		paint.setStyle(Paint.Style.FILL);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int startX =(int)getContext().getResources().getDimension(R.dimen.margin_gap_4);
		View child = getChildAt(0);
		Rect rect = new Rect();
		child.getGlobalVisibleRect(rect);
		
		canvas.drawLine((startX+(rect.width()/2)), rect.height(), (startX+(rect.width()/2)), getHeight(), paint);
		super.onDraw(canvas);
	}

}
