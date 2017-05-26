package com.leon.barchartdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {

    private static final String TAG = "BarChartView";

    private final Paint mBarPaint;

    private float mMax;

    private float[] mDataList;
    private String[] mHorizontalAxis;
    private String[] mVerticalAxis;
    private float mBarWidth;
    private int mGap;
    private Paint mAxisPaint;

    private Rect mTextRect;
    private RectF mTemp;
    private int mRadius;

    private List<Bar> mBars = new ArrayList<Bar>();

    private int mBarGrowStep = 10;
    private boolean enableGrowAnimation = true;
    private static final int DELAY = 50;

    private int mSelectedIndex = -1;

    public BarChartView(Context context) {
        this(context, null);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setTextSize(20);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        mBarPaint = new Paint();
        mBarPaint.setColor(Color.BLUE);
        mBarPaint.setAntiAlias(true);

        mTextRect = new Rect();
        mTemp = new RectF();
        mBarWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBars.clear();
        int width = w - getPaddingLeft() - getPaddingRight();
        int height = h - getPaddingTop() - getPaddingBottom();
        int step = width / mDataList.length;

        int axisStart = step / 2;
        mRadius = (int) (mBarWidth / 2);
        int barLeft = axisStart - mRadius;
        mAxisPaint.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length(), mTextRect);
        int barHeight = height - mTextRect.height() - mGap;
        float heightRatio = barHeight / mMax;

        for (int i = 0; i < mDataList.length; i++) {
            Bar bar = new Bar();
            bar.value = mDataList[i];
            bar.transformedValue = bar.value * heightRatio;

            bar.left = barLeft;
            bar.top = (int) (getPaddingTop() + barHeight - bar.transformedValue);
            bar.right = (int) (barLeft + mBarWidth);
            bar.bottom = getPaddingTop() + barHeight;
            bar.currentTop = bar.bottom;

            mBars.add(bar);
            axisStart = axisStart + step;
            barLeft = (int) (axisStart - mBarWidth / 2);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (enableGrowAnimation) {
            drawBarsWidthAnimation(canvas);
        } else {
            drawBars(canvas);
        }
    }


    private void drawBarsWidthAnimation(Canvas canvas) {
        int minTop = getHeight();
        for (int i = 0; i < mDataList.length; i++) {
            Bar bar = mBars.get(i);
            String axis = mHorizontalAxis[i];
            canvas.drawText(axis, bar.left + mBarWidth / 2, getHeight() - getPaddingBottom(), mAxisPaint);

            bar.currentTop -= mBarGrowStep;
            bar.done = bar.currentTop <= bar.top;
            if (bar.done) {
                bar.currentTop = bar.top;
                if (bar.top < minTop) {
                    minTop = bar.top;
                }
            }
            mTemp.set(bar.left, bar.currentTop, bar.right, bar.bottom);
            canvas.drawRoundRect(mTemp, mRadius, mRadius, mBarPaint);
        }

        if (minTop > getPaddingTop()) {
            postInvalidateDelayed(DELAY);
        } else {
            //动画结束
            enableGrowAnimation = false;
        }

    }


    private void drawBars(Canvas canvas) {
        for (int i = 0; i < mDataList.length; i++) {
            Bar bar = mBars.get(i);
            String axis = mHorizontalAxis[i];
            canvas.drawText(axis, bar.left + mBarWidth / 2, getHeight() - getPaddingBottom(), mAxisPaint);
            mTemp.set(bar.left, bar.top, bar.right, bar.bottom);
            if (i == mSelectedIndex) {
                mBarPaint.setColor(Color.RED);
                float x = bar.left + mBarWidth / 2;
                float y = bar.top - mGap;
                canvas.drawText(String.valueOf(bar.value), x, y, mAxisPaint);
            } else {
                mBarPaint.setColor(Color.BLUE);
            }
            canvas.drawRoundRect(mTemp, mRadius, mRadius, mBarPaint);
        }
    }

    public void setHorizontalAxis(String[] horizontalAxis) {
        mHorizontalAxis = horizontalAxis;
    }

    public void setDataList(float[] dataList, int max) {
        mDataList = dataList;
        mMax = max;
    }

    private class Bar {
        int left;
        int top;
        int right;
        int bottom;
        int currentTop;
        float value;
        float transformedValue;
        boolean done = false;

        boolean isInside(float x, float y) {
            return x > left && x < right && y > top && y < bottom;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enableGrowAnimation) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mBars.size(); i++) {
                    if (mBars.get(i).isInside(event.getX(), event.getY())) {
                        enableGrowAnimation = false;
                        mSelectedIndex = i;
                        invalidate();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mSelectedIndex = -1;
                enableGrowAnimation = false;
                invalidate();
                break;
        }
        return true;
    }
}
