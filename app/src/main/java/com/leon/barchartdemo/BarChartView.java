package com.leon.barchartdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class BarChartView extends View {

    private static final String TAG = "BarChartView";

    private final Paint mbarPaint;
    private int mWidth;
    private int mHeight;

    private float mMax;
    private float mHeightRatio;

    private float[] mDataList;
    private float[] mTransformDataList;
    private String[] mHorizontalAxis;
    private String[] mVerticalAxis;
    private float mBarWidth;
    private int mGap;

    private int mStep;
    private Paint mAxisPaint;

    private Rect mTextRect;
    private RectF mTemp;

    public BarChartView(Context context) {
        this(context, null);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setTextSize(20);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        mbarPaint = new Paint();
        mbarPaint.setColor(Color.BLUE);
        mbarPaint.setAntiAlias(true);

        mTextRect = new Rect();
        mTemp = new RectF();
        mBarWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - getPaddingLeft() - getPaddingRight();
        mHeight = h - getPaddingTop() - getPaddingBottom();
        Log.d(TAG, "onSizeChanged: " + w + " " + h + " " + mWidth + " " + mHeight);
        mStep = mWidth / mDataList.length;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int axisStart = mStep / 2;
        int radius = (int) (mBarWidth / 2);
        int barLeft = (int) (axisStart - radius);
        mAxisPaint.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length(), mTextRect);

        int barHeight = mHeight - mTextRect.height() - mGap;
        mHeightRatio = barHeight / mMax;

        for (int i = 0; i < mDataList.length; i++) {
            String axis = mHorizontalAxis[i];
            canvas.drawText(axis, axisStart, mHeight, mAxisPaint);
            mTransformDataList[i] = mDataList[i] * mHeightRatio;
            int top = (int) (barHeight - mTransformDataList[i] + getPaddingTop());
            int right = (int) (barLeft + mBarWidth);
//            canvas.drawRect(barLeft, top, right, barHeight, mbarPaint);
            mTemp.set(barLeft, top, right, barHeight);
            canvas.drawRoundRect(mTemp, radius, radius, mbarPaint);
            axisStart += mStep;
            barLeft = (int) (axisStart - mBarWidth / 2);;

        }
    }

    public void setHorizontalAxis(String[] horizontalAxis) {
        mHorizontalAxis = horizontalAxis;
    }

    public void setDataList(float[] dataList, int max) {
        mDataList = dataList;
        mTransformDataList = new float[mDataList.length];
        mMax = max;
    }

    public void setMax(float max) {
        mMax = max;
    }
}
