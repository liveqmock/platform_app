package com.apalya.myplex.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.apalya.myplex.R;

public class StrikeTextView extends TextView
{
    private int dividerColor;
    private Paint paint;

    public StrikeTextView(Context context)
    {
        super(context);
        init(context);
    }

    public StrikeTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public StrikeTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        Resources resources = context.getResources();
        //replace with your color
        dividerColor = resources.getColor(R.color.black);

        paint = new Paint();
        paint.setColor(Color.RED);
        //replace with your desired width
        paint.setStrokeWidth(3);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawLine(0, getHeight(), getWidth(), 0, paint);
    }
}